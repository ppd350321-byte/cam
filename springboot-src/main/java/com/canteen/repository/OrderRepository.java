package com.canteen.repository;

import com.canteen.entity.Order;
import com.canteen.entity.enums.OrderStatus;
import com.canteen.entity.enums.PaymentMethod;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT o FROM Order o WHERE o.isDeleted = false " +
            "AND (:keyword IS NULL OR o.customerName LIKE CONCAT('%',:keyword,'%') OR o.orderNo LIKE CONCAT('%',:keyword,'%')) " +
            "AND (:status IS NULL OR o.orderStatus = :status) " +
            "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
            "ORDER BY o.createdAt DESC")
    Page<Order> findByFilters(@Param("keyword") String keyword,
                              @Param("status") OrderStatus status,
                              @Param("paymentMethod") PaymentMethod paymentMethod,
                              Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.isDeleted = false ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status AND o.isDeleted = false")
    long countByOrderStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.actualAmount), 0) FROM Order o WHERE o.orderStatus = com.canteen.entity.enums.OrderStatus.COMPLETED " +
            "AND o.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal sumRevenueByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = com.canteen.entity.enums.OrderStatus.COMPLETED " +
            "AND o.createdAt BETWEEN :start AND :end")
    long countCompletedByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 按日期分组统计每日营收
     */
    @Query("SELECT FUNCTION('DATE', o.createdAt), COALESCE(SUM(o.actualAmount), 0) " +
            "FROM Order o WHERE o.orderStatus = com.canteen.entity.enums.OrderStatus.COMPLETED " +
            "AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> sumRevenueDailyByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 按菜品名统计销量
     */
    @Query("SELECT oi.dishName, SUM(oi.quantity) FROM OrderItem oi " +
            "JOIN oi.order o WHERE o.orderStatus = com.canteen.entity.enums.OrderStatus.COMPLETED " +
            "AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY oi.dishName ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> countSalesByDish(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 查询用户已完成订单中的菜品消费统计（菜品名 + 总数量），用于 AI 个性化推荐
     */
    @Query("SELECT oi.dishName, SUM(oi.quantity) FROM OrderItem oi " +
            "JOIN oi.order o WHERE o.user.id = :userId " +
            "AND o.orderStatus = com.canteen.entity.enums.OrderStatus.COMPLETED " +
            "AND o.isDeleted = false " +
            "GROUP BY oi.dishName ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> countUserDishHistory(@Param("userId") Long userId);
}
