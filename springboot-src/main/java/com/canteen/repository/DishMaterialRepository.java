package com.canteen.repository;

import com.canteen.entity.DishMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishMaterialRepository extends JpaRepository<DishMaterial, Long> {

    @Query("SELECT dm FROM DishMaterial dm JOIN FETCH dm.material WHERE dm.dish.id = :dishId")
    List<DishMaterial> findByDishId(@Param("dishId") Long dishId);

    void deleteByDishId(Long dishId);
}
