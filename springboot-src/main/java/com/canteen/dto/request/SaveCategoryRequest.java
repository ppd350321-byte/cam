package com.canteen.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveCategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private String icon;
    private Integer sortOrder;
    private Boolean isActive;
}
