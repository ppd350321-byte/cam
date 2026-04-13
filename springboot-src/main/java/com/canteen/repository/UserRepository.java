package com.canteen.repository;

import com.canteen.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false " +
            "AND (:keyword IS NULL OR u.realName LIKE CONCAT('%',:keyword,'%') OR u.phone LIKE CONCAT('%',:keyword,'%')) " +
            "AND (:department IS NULL OR u.department = :department) " +
            "AND (:isVip IS NULL OR u.isVip = :isVip) " +
            "AND (:status IS NULL OR u.status = :status)")
    Page<User> findByFilters(@Param("keyword") String keyword,
                             @Param("department") String department,
                             @Param("isVip") Boolean isVip,
                             @Param("status") String status,
                             Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.points = u.points + :points WHERE u.id = :userId")
    void incrementPoints(@Param("userId") Long userId, @Param("points") int points);

    @Query("SELECT COALESCE(SUM(o.actualAmount), 0) FROM Order o WHERE o.user.id = :userId AND o.orderStatus = com.canteen.entity.enums.OrderStatus.COMPLETED")
    BigDecimal sumActualAmountByUserId(@Param("userId") Long userId);
}
