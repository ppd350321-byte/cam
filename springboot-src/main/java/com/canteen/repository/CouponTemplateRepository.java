package com.canteen.repository;

import com.canteen.entity.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {

    List<CouponTemplate> findByIsActiveTrue();

    List<CouponTemplate> findByIsActiveTrueAndPointsCostNotNull();
}
