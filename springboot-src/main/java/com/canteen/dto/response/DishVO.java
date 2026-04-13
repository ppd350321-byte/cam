package com.canteen.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DishVO {
    private Long id;
    private Long categoryId;
    private String category;
    private String name;
    private String description;
    private String imageUrl;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal vipPrice;
    private Integer stock;
    private Integer sales;
    private Boolean available;
    private Boolean isSetMeal;
    private String tags;
    private Boolean useCustomVipPrice;
    private List<DishMaterialVO> materials;

    @Data
    public static class DishMaterialVO {
        private Long materialId;
        private String materialName;
        private BigDecimal quantityPerServing;
        private String unit;
    }
}
