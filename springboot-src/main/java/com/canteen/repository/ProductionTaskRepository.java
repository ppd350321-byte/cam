package com.canteen.repository;

import com.canteen.entity.ProductionTask;
import com.canteen.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionTaskRepository extends JpaRepository<ProductionTask, Long> {

    @Query("SELECT t FROM ProductionTask t " +
            "WHERE (:status IS NULL OR t.status = :status) " +
            "AND (:keyword IS NULL OR t.dishName LIKE CONCAT('%',:keyword,'%') " +
            "     OR t.taskNo LIKE CONCAT('%',:keyword,'%') " +
            "     OR t.chefName LIKE CONCAT('%',:keyword,'%')) " +
            "AND (:dateStart IS NULL OR t.createdAt >= :dateStart) " +
            "AND (:dateEnd IS NULL OR t.createdAt <= :dateEnd) " +
            "ORDER BY t.createdAt DESC")
    Page<ProductionTask> findByFilters(@Param("status") TaskStatus status,
                                       @Param("keyword") String keyword,
                                       @Param("dateStart") LocalDateTime dateStart,
                                       @Param("dateEnd") LocalDateTime dateEnd,
                                       Pageable pageable);

    long countByStatus(TaskStatus status);

    Optional<ProductionTask> findByOrderId(Long orderId);

    @Query("SELECT AVG(t.order.rating) FROM ProductionTask t WHERE t.chef.id = :chefId AND t.order.rating IS NOT NULL")
    Double findAverageRatingByChefId(@Param("chefId") Long chefId);

    @Query("SELECT t.dishName, COUNT(t), SUM(CASE WHEN t.status = com.canteen.entity.enums.TaskStatus.COMPLETED THEN 1 ELSE 0 END) " +
            "FROM ProductionTask t WHERE t.createdAt BETWEEN :start AND :end " +
            "GROUP BY t.dishName ORDER BY COUNT(t) DESC")
    List<Object[]> countTasksGroupedByDish(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
