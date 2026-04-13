package com.canteen.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateAdminRequest {
    private String realName;
    private String phone;
    private String email;
    private String department;
    private String title;
    private String status;

    @Size(min = 6, max = 64, message = "密码长度在6-64之间")
    private String password;
}
