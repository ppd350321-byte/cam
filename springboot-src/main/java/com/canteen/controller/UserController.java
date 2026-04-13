package com.canteen.controller;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.*;
import com.canteen.dto.response.UserVO;
import com.canteen.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('users:view')")
    public Result<PageResult<UserVO>> listUsers(UserPageQuery query) {
        return userService.listUsers(query);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users:edit')")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request);
        return Result.ok();
    }

    @PostMapping("/{id}/recharge")
    @PreAuthorize("hasAuthority('users:recharge')")
    public Result<Void> rechargeUser(@PathVariable Long id,
                                     @Valid @RequestBody RechargeRequest request) {
        userService.rechargeBalance(id, request.getAmount());
        return Result.ok();
    }

    @PostMapping("/{id}/points")
    @PreAuthorize("hasAuthority('users:points')")
    public Result<Void> addPoints(@PathVariable Long id,
                                  @Valid @RequestBody AddPointsRequest request) {
        userService.addPoints(id, request.getPoints());
        return Result.ok();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('users:status')")
    public Result<Void> toggleUserStatus(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        String status = body.get("status");
        userService.toggleUserStatus(id, status);
        return Result.ok();
    }
}
