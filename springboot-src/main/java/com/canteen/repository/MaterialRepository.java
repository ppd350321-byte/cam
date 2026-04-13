package com.canteen.repository;

import com.canteen.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Query("SELECT m FROM Material m " +
            "WHERE (:keyword IS NULL OR m.name LIKE CONCAT('%',:keyword,'%') OR m.sku LIKE CONCAT('%',:keyword,'%')) " +
            "AND (:category IS NULL OR m.category = :category) " +
            "ORDER BY m.id DESC")
    Page<Material> findByFilters(@Param("keyword") String keyword,
                                 @Param("category") String category,
                                 Pageable pageable);

    @Query("SELECT DISTINCT m.category FROM Material m WHERE m.category IS NOT NULL ORDER BY m.category")
    List<String> findDistinctCategories();
}
