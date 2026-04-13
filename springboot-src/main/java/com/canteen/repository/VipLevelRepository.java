package com.canteen.repository;

import com.canteen.entity.VipLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VipLevelRepository extends JpaRepository<VipLevel, Long> {

    List<VipLevel> findAllByOrderByLevelAsc();
}
