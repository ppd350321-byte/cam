package com.canteen.repository;

import com.canteen.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    @Query("SELECT d FROM Dish d LEFT JOIN FETCH d.category WHERE d.isDeleted = false AND d.available = true " +
            "AND (:categoryId IS NULL OR d.category.id = :categoryId) " +
            "ORDER BY d.sortOrder ASC")
    List<Dish> findAvailableByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT d FROM Dish d WHERE d.isDeleted = false " +
            "AND (:keyword IS NULL OR d.name LIKE CONCAT('%',:keyword,'%'))")
    Page<Dish> findByFilters(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT d FROM Dish d LEFT JOIN FETCH d.category WHERE d.isDeleted = false " +
            "AND (:keyword IS NULL OR d.name LIKE CONCAT('%',:keyword,'%')) " +
            "AND (:categoryId IS NULL OR d.category.id = :categoryId) " +
            "ORDER BY d.sortOrder ASC")
    Page<Dish> findByFiltersAdmin(@Param("keyword") String keyword,
                                  @Param("categoryId") Long categoryId,
                                  Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Dish d WHERE d.id = :id")
    Optional<Dish> findByIdWithLock(@Param("id") Long id);
}
