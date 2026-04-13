package com.canteen.service;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateRoleRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.RoleVO;

import java.util.List;

public interface RoleService {

    Result<PageResult<RoleVO>> listRoles(PageQuery query, boolean excludeSuperAdmin);

    RoleVO createRole(CreateRoleRequest request);

    RoleVO updateRole(Long id, CreateRoleRequest request);

    void deleteRole(Long id);

    void grantPermissions(Long roleId, List<String> permissionCodes);
}
