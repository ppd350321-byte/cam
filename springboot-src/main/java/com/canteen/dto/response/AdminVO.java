package com.canteen.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class AdminVO {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private String email;
    private String department;
    private String title;
    private String status;
    private List<Long> roleIds;
    private List<String> roleNames;
    private String lastLoginAt;
}
