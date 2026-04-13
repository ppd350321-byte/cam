package com.canteen.service;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.*;
import com.canteen.dto.response.LoginVO;
import com.canteen.dto.response.UserProfileVO;
import com.canteen.dto.response.UserVO;

public interface UserService {

    LoginVO login(LoginRequest request);

    LoginVO register(RegisterRequest request);

    void logout(String token);

    Result<PageResult<UserVO>> listUsers(UserPageQuery query);

    UserVO getUserById(Long id);

    UserVO updateUser(Long id, UpdateUserRequest request);

    void toggleUserStatus(Long id, String status);

    void rechargeBalance(Long id, java.math.BigDecimal amount);

    void addPoints(Long id, int points);

    void deductBalance(Long userId, java.math.BigDecimal amount, Long refId);

    void checkAndUpgradeVip(Long userId);

    void recalculateAllVipLevels();

    UserProfileVO getUserProfile(Long userId);
}
