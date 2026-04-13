package com.canteen.controller;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateAdminRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.request.UpdateAdminRequest;
import com.canteen.dto.response.AdminVO;
import com.canteen.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    @PreAuthorize("hasAuthority('permission-config:view')")
    public Result<PageResult<AdminVO>> listAdmins(PageQuery query) {
        return adminService.listAdmins(query);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('permission-config:view')")
    public Result<AdminVO> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        return Result.ok(adminService.createAdmin(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission-config:view')")
    public Result<AdminVO> updateAdmin(@PathVariable Long id,
                                       @Valid @RequestBody UpdateAdminRequest request) {
        return Result.ok(adminService.updateAdmin(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission-config:view')")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return Result.ok();
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('permission-config:role:grant')")
    public Result<Void> assignRoles(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {
        List<Number> rawIds = (List<Number>) body.get("roleIds");
        List<Long> roleIds = rawIds.stream().map(Number::longValue).toList();
        adminService.assignRoles(id, roleIds);
        return Result.ok();
    }
}
