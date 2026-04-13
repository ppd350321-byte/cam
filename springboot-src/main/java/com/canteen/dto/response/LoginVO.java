package com.canteen.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class LoginVO {
    private String token;
    private UserLoginVO user;
    private String expiresAt;

    @Data
    public static class UserLoginVO {
        private Long id;
        private String username;
        private String name;
        private Boolean isAdmin;
        private List<String> roleCodes;
        private List<Long> roleIds;
        private List<String> permissions;
    }
}
