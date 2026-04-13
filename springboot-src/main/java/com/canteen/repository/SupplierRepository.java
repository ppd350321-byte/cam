package com.canteen.repository;

import com.canteen.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("SELECT s FROM Supplier s WHERE s.isDeleted = false " +
            "AND (:keyword IS NULL OR s.name LIKE CONCAT('%',:keyword,'%')) " +
            "AND (:category IS NULL OR s.category = :category) " +
            "AND (:status IS NULL OR s.status = :status)")
    Page<Supplier> findByFilters(@Param("keyword") String keyword,
                                  @Param("category") String category,
                                  @Param("status") String status,
                                  Pageable pageable);
}
