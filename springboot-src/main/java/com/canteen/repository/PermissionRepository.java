package com.canteen.repository;

import com.canteen.entity.Admin;
import com.canteen.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByCodeIn(List<String> codes);

    Optional<Permission> findByCode(String code);

    @Query("SELECT DISTINCT p.code FROM Admin a JOIN a.roles r JOIN r.permissions p WHERE a.id = :adminId")
    Set<String> findPermissionCodesByAdminId(@Param("adminId") Long adminId);

    @Query("SELECT DISTINCT p FROM Admin a JOIN a.roles r JOIN r.permissions p WHERE a.id = :adminId")
    Set<Permission> findByAdminId(@Param("adminId") Long adminId);
}
