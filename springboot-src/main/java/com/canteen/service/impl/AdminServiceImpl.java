package com.canteen.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import com.canteen.dto.request.CreateAdminRequest;
import com.canteen.dto.request.LoginRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.request.UpdateAdminRequest;
import com.canteen.dto.response.AdminVO;
import com.canteen.dto.response.LoginVO;
import com.canteen.entity.Admin;
import com.canteen.entity.Permission;
import com.canteen.entity.Role;
import com.canteen.repository.AdminRepository;
import com.canteen.repository.PermissionRepository;
import com.canteen.repository.RoleRepository;
import com.canteen.security.JwtTokenProvider;
import com.canteen.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional
    public LoginVO login(LoginRequest request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (!"active".equals(admin.getStatus()) || Boolean.TRUE.equals(admin.getIsDeleted())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        admin.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(admin);

        String token = jwtTokenProvider.generateToken(admin.getId(), admin.getUsername(), "admin");

        // 删除该用户旧的 token 缓存（确保同一用户只有一个有效 token）
        String userTokenKey = "jwt:user-token:admin:" + admin.getId();
        Object oldToken = redisTemplate.opsForValue().get(userTokenKey);
        if (oldToken != null) {
            redisTemplate.delete("jwt:token:" + oldToken);
        }

        // 将新 token 缓存到 Redis，TTL 与 JWT 过期时间一致
        long expirationSeconds = jwtTokenProvider.getRemainingExpiration(token);
        redisTemplate.opsForValue().set("jwt:token:" + token, admin.getId(), expirationSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(userTokenKey, token, expirationSeconds, TimeUnit.SECONDS);

        Set<Permission> permissions = permissionRepository.findByAdminId(admin.getId());
        List<String> permCodes = permissions.stream().map(Permission::getCode).toList();

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresAt(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        LoginVO.UserLoginVO userVO = new LoginVO.UserLoginVO();
        userVO.setId(admin.getId());
        userVO.setUsername(admin.getUsername());
        userVO.setName(admin.getRealName());
        userVO.setIsAdmin("admin".equals(admin.getUsername()));
        userVO.setRoleCodes(admin.getRoles().stream().map(Role::getName).toList());
        userVO.setRoleIds(admin.getRoles().stream().map(Role::getId).toList());
        // admin 用户拥有全权限，非 admin 返回实际角色权限（可能为空）
        userVO.setPermissions("admin".equals(admin.getUsername()) ? getAllPermissionCodes() : permCodes);
        vo.setUser(userVO);

        return vo;
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            long remaining = jwtTokenProvider.getRemainingExpiration(token);
            redisTemplate.opsForValue().set("jwt:blacklist:" + token, "1", remaining, TimeUnit.SECONDS);
            // 移除缓存的 token 及用户反向映射
            redisTemplate.delete("jwt:token:" + token);
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            redisTemplate.delete("jwt:user-token:admin:" + userId);
        }
    }

    private List<String> getAllPermissionCodes() {
        return permissionRepository.findAll().stream().map(Permission::getCode).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<AdminVO>> listAdmins(PageQuery query) {
        String keyword = "all".equals(query.getKeyword()) ? null : query.getKeyword();
        String status = "all".equals(query.getStatus()) ? null : query.getStatus();

        Page<Admin> page = adminRepository.findByFilters(keyword, status, query.toPageable());
        List<AdminVO> list = page.getContent().stream().map(this::toAdminVO).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public void assignRoles(Long adminId, List<Long> roleIds) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "管理员不存在"));
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        admin.setRoles(roles);
        adminRepository.save(admin);
    }

    private AdminVO toAdminVO(Admin admin) {
        AdminVO vo = new AdminVO();
        vo.setId(admin.getId());
        vo.setName(admin.getRealName());
        vo.setUsername(admin.getUsername());
        vo.setPhone(admin.getPhone());
        vo.setEmail(admin.getEmail());
        vo.setDepartment(admin.getDepartment());
        vo.setTitle(admin.getTitle());
        vo.setStatus(admin.getStatus());
        vo.setRoleIds(admin.getRoles().stream().map(Role::getId).toList());
        vo.setRoleNames(admin.getRoles().stream().map(Role::getName).toList());
        vo.setLastLoginAt(admin.getLastLoginAt() != null ? admin.getLastLoginAt().format(FMT) : null);
        return vo;
    }

    @Override
    @Transactional
    public AdminVO createAdmin(CreateAdminRequest request) {
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名已存在");
        }
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRealName(request.getRealName());
        admin.setPhone(request.getPhone() != null && request.getPhone().isBlank() ? null : request.getPhone());
        admin.setEmail(request.getEmail());
        admin.setDepartment(request.getDepartment());
        admin.setTitle(request.getTitle());
        adminRepository.save(admin);
        return toAdminVO(admin);
    }

    @Override
    @Transactional
    public AdminVO updateAdmin(Long id, UpdateAdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "员工不存在"));
        if (request.getRealName() != null) admin.setRealName(request.getRealName());
        if (request.getPhone() != null) admin.setPhone(request.getPhone().isBlank() ? null : request.getPhone());
        if (request.getEmail() != null) admin.setEmail(request.getEmail());
        if (request.getDepartment() != null) admin.setDepartment(request.getDepartment());
        if (request.getTitle() != null) admin.setTitle(request.getTitle());
        if (request.getStatus() != null) admin.setStatus(request.getStatus());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        adminRepository.save(admin);
        return toAdminVO(admin);
    }

    @Override
    @Transactional
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "员工不存在"));
        if ("admin".equals(admin.getUsername())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能删除超级管理员账号");
        }
        admin.setIsDeleted(true);
        adminRepository.save(admin);
    }
}
