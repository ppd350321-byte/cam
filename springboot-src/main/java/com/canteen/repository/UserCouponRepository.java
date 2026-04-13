package com.canteen.repository;

import com.canteen.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserIdAndStatus(Long userId, String status);

    List<UserCoupon> findByUserId(Long userId);

    long countByUserIdAndTemplateId(Long userId, Long templateId);
}
