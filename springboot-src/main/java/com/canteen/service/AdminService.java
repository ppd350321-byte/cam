package com.canteen.service;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateAdminRequest;
import com.canteen.dto.request.LoginRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.request.UpdateAdminRequest;
import com.canteen.dto.response.AdminVO;
import com.canteen.dto.response.LoginVO;

import java.util.List;

public interface AdminService {

    LoginVO login(LoginRequest request);

    void logout(String token);

    Result<PageResult<AdminVO>> listAdmins(PageQuery query);

    void assignRoles(Long adminId, List<Long> roleIds);

    AdminVO createAdmin(CreateAdminRequest request);

    AdminVO updateAdmin(Long id, UpdateAdminRequest request);

    void deleteAdmin(Long id);
}
