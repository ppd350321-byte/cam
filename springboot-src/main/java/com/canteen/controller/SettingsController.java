package com.canteen.controller;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateRoleRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.RoleVO;
import com.canteen.entity.Permission;
import com.canteen.repository.PermissionRepository;
import com.canteen.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final RoleService roleService;
    private final PermissionRepository permissionRepository;

    // ── System Settings ──
    @GetMapping
    @PreAuthorize("hasAuthority('settings:view')")
    public Result<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("siteName", "社区智慧食堂");
        settings.put("businessHours", "07:00-21:00");
        settings.put("maxOrdersPerDay", 500);
        settings.put("vipDiscount", 0.9);
        return Result.ok(settings);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('settings:save')")
    public Result<Void> saveSettings(@RequestBody Map<String, Object> data) {
        // Save settings placeholder
        return Result.ok();
    }

    // ── Roles ──
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('permission-config:view')")
    public Result<PageResult<RoleVO>> listRoles(PageQuery query,
                                                 @RequestParam(required = false, defaultValue = "false") boolean excludeSuperAdmin) {
        return roleService.listRoles(query, excludeSuperAdmin);
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('permission-config:role:create')")
    public Result<RoleVO> addRole(@Valid @RequestBody CreateRoleRequest request) {
        return Result.ok(roleService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('permission-config:role:update')")
    public Result<RoleVO> updateRole(@PathVariable Long id,
                                     @Valid @RequestBody CreateRoleRequest request) {
        return Result.ok(roleService.updateRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('permission-config:role:delete')")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok();
    }

    @PutMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('permission-config:role:grant')")
    public Result<Void> saveRolePermissions(@PathVariable Long roleId,
                                            @RequestBody Map<String, List<String>> body) {
        List<String> permissionCodes = body.get("permissionCodes");
        roleService.grantPermissions(roleId, permissionCodes);
        return Result.ok();
    }

    // ── Permissions Tree ──
    @GetMapping("/permissions/tree")
    @PreAuthorize("hasAuthority('permission-config:view')")
    public Result<List<Map<String, Object>>> listPermissionTree() {
        List<Permission> allPerms = permissionRepository.findAll();

        Map<String, String> moduleLabels = Map.ofEntries(
                Map.entry("dashboard", "仪表盘"),
                Map.entry("users", "用户管理"),
                Map.entry("orders", "订单管理"),
                Map.entry("menu", "菜品管理"),
                Map.entry("supply", "供应链"),
                Map.entry("production", "生产调度"),
                Map.entry("reports", "报表中心"),
                Map.entry("settings", "系统设置"),
                Map.entry("vip-coupon", "VIP与优惠券"),
                Map.entry("permission-config", "权限配置")
        );

        Map<String, List<Permission>> byModule = allPerms.stream()
                .collect(Collectors.groupingBy(p -> p.getModule() != null ? p.getModule() : "other"));

        List<Map<String, Object>> tree = new ArrayList<>();
        byModule.forEach((module, perms) -> {
            Map<String, Object> node = new HashMap<>();
            node.put("module", module);
            node.put("label", moduleLabels.getOrDefault(module, module));
            node.put("children", perms.stream().map(p -> Map.of(
                    "code", p.getCode(),
                    "label", p.getLabel() != null ? p.getLabel() : p.getCode()
            )).toList());
            tree.add(node);
        });

        return Result.ok(tree);
    }

    // ── Recharge Rules ──
    @GetMapping("/recharge-rules")
    @PreAuthorize("hasAuthority('settings:view')")
    public Result<Map<String, Object>> listRechargeRules(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(Map.of("list", List.of(), "total", 0, "page", page, "pageSize", pageSize));
    }
}
