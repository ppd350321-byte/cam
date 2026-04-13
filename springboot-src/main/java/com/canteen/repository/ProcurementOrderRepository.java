package com.canteen.repository;

import com.canteen.entity.ProcurementOrder;
import com.canteen.entity.enums.ProcurementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcurementOrderRepository extends JpaRepository<ProcurementOrder, Long> {

    @Query("SELECT p FROM ProcurementOrder p " +
            "WHERE (:keyword IS NULL OR p.procNo LIKE CONCAT('%',:keyword,'%')) " +
            "AND (:status IS NULL OR p.status = :status)")
    Page<ProcurementOrder> findByFilters(@Param("keyword") String keyword,
                                          @Param("status") ProcurementStatus status,
                                          Pageable pageable);
}
