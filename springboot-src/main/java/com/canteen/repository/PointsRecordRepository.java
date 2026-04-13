package com.canteen.repository;

import com.canteen.entity.PointsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsRecordRepository extends JpaRepository<PointsRecord, Long> {

    List<PointsRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}
