package com.canteen.dto.response;

import lombok.Data;

@Data
public class MenuCategoryVO {
    private Long id;
    private String name;
    private String icon;
    private Integer sortOrder;
    private Boolean isActive;
}
