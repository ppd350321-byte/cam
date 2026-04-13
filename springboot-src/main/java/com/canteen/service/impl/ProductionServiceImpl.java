package com.canteen.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.ProductionTaskVO;
import com.canteen.entity.Admin;
import com.canteen.entity.AdminPerformance;
import com.canteen.entity.Order;
import com.canteen.entity.ProductionTask;
import com.canteen.entity.enums.OrderStatus;
import com.canteen.entity.enums.TaskStatus;
import com.canteen.repository.AdminPerformanceRepository;
import com.canteen.repository.OrderRepository;
import com.canteen.repository.ProductionTaskRepository;
import com.canteen.service.ProductionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductionServiceImpl implements ProductionService {

    private final ProductionTaskRepository taskRepository;
    private final OrderRepository orderRepository;
    private final AdminPerformanceRepository performanceRepository;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<ProductionTaskVO>> listTasks(PageQuery query, String status, String date) {
        TaskStatus taskStatus = null;
        if (status != null && !"all".equals(status)) {
            try {
                taskStatus = TaskStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        LocalDateTime dateStart = null;
        LocalDateTime dateEnd = null;
        if (date != null && !date.isBlank()) {
            LocalDate ld = LocalDate.parse(date);
            dateStart = ld.atStartOfDay();
            dateEnd = ld.atTime(LocalTime.MAX);
        }

        String keyword = (query.getKeyword() != null && !query.getKeyword().isBlank()) ? query.getKeyword().trim() : null;
        Page<ProductionTask> page = taskRepository.findByFilters(taskStatus, keyword, dateStart, dateEnd, query.toPageable());
        List<ProductionTaskVO> list = page.getContent().stream().map(this::toTaskVO).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public void startTask(Long taskId) {
        ProductionTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "任务不存在"));

        if (task.getStatus() != TaskStatus.PENDING) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只能开始待处理的任务");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setActualStart(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void completeTask(Long taskId) {
        ProductionTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "任务不存在"));

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只能完成进行中的任务");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setProgress(100);
        task.setActualEnd(LocalDateTime.now());
        taskRepository.save(task);

        // 记录雇员绩效
        if (task.getChef() != null) {
            AdminPerformance perf = new AdminPerformance();
            perf.setAdmin(task.getChef());
            perf.setTask(task);
            perf.setDishName(task.getDishName());
            // 计算绩效分：优先使用菜品价格，否则从关联订单金额计算
            java.math.BigDecimal score;
            if (task.getDish() != null && task.getDish().getPrice() != null) {
                perf.setDishPrice(task.getDish().getPrice());
                score = task.getDish().getPrice()
                        .multiply(java.math.BigDecimal.valueOf(task.getQuantity() != null ? task.getQuantity() : 1));
            } else if (task.getOrder() != null && task.getOrder().getActualAmount() != null) {
                perf.setDishPrice(task.getOrder().getActualAmount());
                score = task.getOrder().getActualAmount();
            } else {
                score = java.math.BigDecimal.valueOf(task.getQuantity() != null ? task.getQuantity() : 1);
            }
            perf.setScore(score);
            perf.setRemark("完成任务 " + task.getTaskNo());
            performanceRepository.save(perf);
            log.info("记录绩效: 雇员={}, 任务={}, 分数={}", task.getChefName(), task.getTaskNo(), score);
        }

        // 任务完成时，将关联订单状态更新为待取餐
        if (task.getOrder() != null) {
            Order order = task.getOrder();
            if (order.getOrderStatus() == OrderStatus.PREPARING) {
                order.setOrderStatus(OrderStatus.PENDING_PICKUP);
                orderRepository.save(order);
            }
        }
    }

    private static final java.util.Map<String, String> TASK_STATUS_LABELS = java.util.Map.of(
            "pending", "待开始",
            "in_progress", "进行中",
            "completed", "已完成",
            "cancelled", "已取消"
    );

    private ProductionTaskVO toTaskVO(ProductionTask task) {
        ProductionTaskVO vo = new ProductionTaskVO();
        vo.setId(task.getId());
        vo.setName(task.getDishName());
        vo.setStartTime(task.getPlannedStart() != null ? task.getPlannedStart().format(DATETIME_FMT) : null);
        vo.setEndTime(task.getPlannedEnd() != null ? task.getPlannedEnd().format(DATETIME_FMT) : null);
        String statusVal = task.getStatus().getValue();
        vo.setStatus(TASK_STATUS_LABELS.getOrDefault(statusVal, statusVal));
        vo.setProgress(task.getProgress());
        vo.setChef(task.getChefName());
        if (task.getOrder() != null) {
            vo.setOrderId(task.getOrder().getId());
            vo.setOrderNo(task.getOrder().getOrderNo());
        }
        return vo;
    }
}
