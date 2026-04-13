package com.canteen.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import com.canteen.dto.request.CreateRoleRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.RoleVO;
import com.canteen.entity.Permission;
import com.canteen.entity.Role;
import com.canteen.repository.PermissionRepository;
import com.canteen.repository.RoleRepository;
import com.canteen.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<RoleVO>> listRoles(PageQuery query, boolean excludeSuperAdmin) {
        Page<Role> page;
        if (excludeSuperAdmin) {
            page = roleRepository.findByIsSystemFalseOrIsSystemIsNull(query.toPageable());
        } else {
            page = roleRepository.findAll(query.toPageable());
        }
        List<RoleVO> list = page.getContent().stream().map(this::toRoleVO).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public RoleVO createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getCode())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "角色编码已存在");
        }

        Role role = new Role();
        role.setName(request.getCode());
        role.setLabel(request.getName());
        role.setDescription(request.getDescription());

        if (request.getPermissionCodes() != null && !request.getPermissionCodes().isEmpty()) {
            List<Permission> perms = permissionRepository.findByCodeIn(request.getPermissionCodes());
            role.setPermissions(new HashSet<>(perms));
        }

        roleRepository.save(role);
        return toRoleVO(role);
    }

    @Override
    @Transactional
    public RoleVO updateRole(Long id, CreateRoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "角色不存在"));

        role.setLabel(request.getName());
        role.setDescription(request.getDescription());

        if (request.getPermissionCodes() != null) {
            List<Permission> perms = permissionRepository.findByCodeIn(request.getPermissionCodes());
            role.setPermissions(new HashSet<>(perms));
        }

        roleRepository.save(role);
        return toRoleVO(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "角色不存在"));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "系统内置角色不可删除");
        }

        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public void grantPermissions(Long roleId, List<String> permissionCodes) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "角色不存在"));

        List<Permission> perms = permissionRepository.findByCodeIn(permissionCodes);
        role.setPermissions(new HashSet<>(perms));
        roleRepository.save(role);
    }

    private RoleVO toRoleVO(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setName(role.getLabel());
        vo.setCode(role.getName());
        vo.setLabel(role.getLabel());
        vo.setDescription(role.getDescription());
        vo.setIsSystem(role.getIsSystem());
        vo.setPermissionCodes(role.getPermissions().stream()
                .map(Permission::getCode).toList());
        return vo;
    }
}
