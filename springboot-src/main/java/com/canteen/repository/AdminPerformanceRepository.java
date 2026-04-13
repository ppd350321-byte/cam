package com.canteen.repository;

import com.canteen.entity.AdminPerformance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminPerformanceRepository extends JpaRepository<AdminPerformance, Long> {

    @Query("SELECT ap.admin.id, ap.admin.realName, SUM(ap.score), COUNT(ap) " +
            "FROM AdminPerformance ap " +
            "WHERE ap.createdAt BETWEEN :start AND :end " +
            "GROUP BY ap.admin.id, ap.admin.realName " +
            "ORDER BY SUM(ap.score) DESC")
    List<Object[]> summarizeByDateRange(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Query("SELECT ap.admin.id, ap.admin.realName, SUM(ap.score), COUNT(ap) " +
            "FROM AdminPerformance ap " +
            "WHERE ap.createdAt BETWEEN :start AND :end " +
            "AND (:keyword IS NULL OR ap.admin.realName LIKE CONCAT('%',:keyword,'%') OR ap.admin.username LIKE CONCAT('%',:keyword,'%')) " +
            "GROUP BY ap.admin.id, ap.admin.realName " +
            "ORDER BY SUM(ap.score) DESC")
    Page<Object[]> summarizeByFilters(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("keyword") String keyword,
                                      Pageable pageable);

    @Query("SELECT COALESCE(SUM(ap.score), 0) FROM AdminPerformance ap " +
            "WHERE ap.admin.id = :adminId AND ap.createdAt BETWEEN :start AND :end")
    BigDecimal sumScoreByAdmin(@Param("adminId") Long adminId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(ap) FROM AdminPerformance ap " +
            "WHERE ap.admin.id = :adminId AND ap.createdAt BETWEEN :start AND :end")
    long countByAdminAndDateRange(@Param("adminId") Long adminId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(ap) FROM AdminPerformance ap " +
            "WHERE ap.admin.id = :adminId AND ap.task.id = :taskId")
    long countByAdminAndTask(@Param("adminId") Long adminId,
                             @Param("taskId") Long taskId);
}
