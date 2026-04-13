package com.canteen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaveDishRequest {
    private Long categoryId;
    @NotBlank(message = "菜品名称不能为空")
    private String name;
    private String description;
    private String imageUrl;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal vipPrice;
    private Integer stock;
    private Boolean available;
    private Boolean isSetMeal;
    private String tags;
    private Integer sortOrder;
    private Boolean useCustomVipPrice;

    private List<DishMaterialItem> materials;

    @Data
    public static class DishMaterialItem {
        private Long materialId;
        private BigDecimal quantityPerServing;
        private String unit;
    }
}
