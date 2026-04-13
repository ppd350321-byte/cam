package com.canteen.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import com.canteen.dto.request.CreateProcurementRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.ProcurementVO;
import com.canteen.entity.Inventory;
import com.canteen.entity.Material;
import com.canteen.entity.ProcurementOrder;
import com.canteen.entity.Supplier;
import com.canteen.entity.enums.ProcurementStatus;
import com.canteen.repository.*;
import com.canteen.service.ProcurementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcurementServiceImpl implements ProcurementService {

    private final ProcurementOrderRepository procurementOrderRepository;
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;

    private static final AtomicLong PROC_SEQ = new AtomicLong(1);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<ProcurementVO>> listProcurements(PageQuery query) {
        String keyword = "all".equals(query.getKeyword()) ? null : query.getKeyword();
        ProcurementStatus status = null;
        if (query.getStatus() != null && !"all".equals(query.getStatus())) {
            try {
                status = ProcurementStatus.valueOf(query.getStatus().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<ProcurementOrder> page = procurementOrderRepository.findByFilters(keyword, status, query.toPageable());
        List<ProcurementVO> list = page.getContent().stream().map(this::toVO).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public ProcurementVO createProcurement(CreateProcurementRequest request, Long operatorId) {
        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "物料不存在"));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "供货商不存在"));

        ProcurementOrder order = new ProcurementOrder();
        order.setProcNo("PR-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now())
                + "-" + String.format("%02d", PROC_SEQ.getAndIncrement()));
        order.setMaterial(material);
        order.setSupplier(supplier);
        order.setQuantity(request.getQuantity());
        order.setUnit(request.getUnit() != null ? request.getUnit() : material.getUnit());
        order.setUnitPrice(request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO);
        order.setTotalCost(order.getUnitPrice().multiply(order.getQuantity()));
        order.setExpectedDate(request.getExpectedDate());
        order.setOperatorId(operatorId);
        order.setRemark(request.getRemark());

        procurementOrderRepository.save(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public void approveProcurement(Long id, Long approverId) {
        ProcurementOrder order = procurementOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "采购单不存在"));

        if (order.getStatus() != ProcurementStatus.PENDING) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只能审批待审核的采购单");
        }

        order.setStatus(ProcurementStatus.APPROVED);
        order.setApproverId(approverId);
        procurementOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void receiveProcurement(Long id) {
        ProcurementOrder order = procurementOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "采购单不存在"));

        if (order.getStatus() != ProcurementStatus.APPROVED && order.getStatus() != ProcurementStatus.IN_TRANSIT) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只能对已审批或在途的采购单确认收货");
        }

        order.setStatus(ProcurementStatus.RECEIVED);
        order.setReceivedAt(LocalDateTime.now());
        procurementOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void completeProcurement(Long id) {
        ProcurementOrder order = procurementOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "采购单不存在"));

        if (order.getStatus() != ProcurementStatus.RECEIVED) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只能对已收货的采购单执行完成操作");
        }

        order.setStatus(ProcurementStatus.COMPLETED);
        procurementOrderRepository.save(order);

        // Update inventory
        Inventory inventory = inventoryRepository.findByMaterialId(order.getMaterial().getId())
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setMaterial(order.getMaterial());
                    inv.setCurrentStock(BigDecimal.ZERO);
                    return inv;
                });
        inventory.setCurrentStock(inventory.getCurrentStock().add(order.getQuantity()));
        inventory.setLastInboundDate(LocalDate.now());
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public ProcurementVO getProcurementById(Long id) {
        ProcurementOrder order = procurementOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "采购单不存在"));
        return toVO(order);
    }

    private static final java.util.Map<String, String> PROC_STATUS_LABELS = java.util.Map.of(
            "pending", "待审核",
            "approved", "已审批",
            "in_transit", "运输中",
            "received", "已收货",
            "completed", "已完成",
            "cancelled", "已取消"
    );

    private ProcurementVO toVO(ProcurementOrder p) {
        ProcurementVO vo = new ProcurementVO();
        vo.setId(p.getId());
        vo.setProcNo(p.getProcNo());
        String matName = p.getMaterial() != null ? p.getMaterial().getName() : null;
        String supName = p.getSupplier() != null ? p.getSupplier().getName() : null;
        vo.setMaterialName(matName);
        vo.setItem(matName);
        vo.setSupplierName(supName);
        vo.setSupplier(supName);
        vo.setQuantity(p.getQuantity());
        vo.setUnit(p.getUnit());
        vo.setUnitPrice(p.getUnitPrice());
        vo.setTotalCost(p.getTotalCost());
        vo.setCost(p.getTotalCost() != null ? "¥ " + p.getTotalCost().toPlainString() : null);
        String statusVal = p.getStatus().getValue();
        vo.setStatus(statusVal);
        vo.setStatusLabel(PROC_STATUS_LABELS.getOrDefault(statusVal, statusVal));
        vo.setExpectedDate(p.getExpectedDate() != null ? p.getExpectedDate().format(DATE_FMT) : null);
        vo.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        return vo;
    }
}
