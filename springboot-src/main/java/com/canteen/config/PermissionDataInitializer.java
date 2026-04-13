package com.canteen.config;

import com.canteen.entity.Admin;
import com.canteen.entity.Permission;
import com.canteen.repository.AdminRepository;
import com.canteen.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionDataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    private static final List<Map<String, String>> REQUIRED_PERMISSIONS = List.of(
            Map.of("code", "menu:view", "label", "查看菜品", "module", "menu"),
            Map.of("code", "menu:add", "label", "新增菜品", "module", "menu"),
            Map.of("code", "menu:edit", "label", "编辑菜品", "module", "menu"),
            Map.of("code", "menu:delete", "label", "删除菜品", "module", "menu")
    );

    @Override
    @Transactional
    public void run(String... args) {
        initPermissions();
        initAdminPassword();
    }

    private void initPermissions() {
        Set<String> existingCodes = permissionRepository.findAll().stream()
                .map(Permission::getCode).collect(Collectors.toSet());

        for (Map<String, String> perm : REQUIRED_PERMISSIONS) {
            String code = perm.get("code");
            if (!existingCodes.contains(code)) {
                Permission p = new Permission();
                p.setCode(code);
                p.setLabel(perm.get("label"));
                p.setModule(perm.get("module"));
                permissionRepository.save(p);
                log.info("Created missing permission: {}", code);
            } else {
                // Update label if null
                permissionRepository.findByCode(code).ifPresent(p -> {
                    if (p.getLabel() == null || "?".equals(p.getLabel())) {
                        p.setLabel(perm.get("label"));
                        p.setModule(perm.get("module"));
                        permissionRepository.save(p);
                        log.info("Updated permission label: {}", code);
                    }
                });
            }
        }
    }

    private void initAdminPassword() {
        adminRepository.findByUsername("admin").ifPresent(admin -> {
            if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                adminRepository.save(admin);
                log.info("Admin default password reset to admin123");
            }
        });

        // 确保 test1 测试账号存在（无角色、无权限）
        if (adminRepository.findByUsername("test1").isEmpty()) {
            Admin test1 = new Admin();
            test1.setUsername("test1");
            test1.setPasswordHash(passwordEncoder.encode("123456"));
            test1.setRealName("测试用户");
            test1.setStatus("active");
            adminRepository.save(test1);
            log.info("Created test1 account with no roles");
        }
    }
}
