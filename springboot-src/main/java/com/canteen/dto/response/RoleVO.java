package com.canteen.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class RoleVO {
    private Long id;
    private String name;
    private String code;
    private String label;
    private String description;
    private Boolean isSystem;
    private List<String> permissionCodes;
}
