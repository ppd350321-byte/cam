package com.canteen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateSupplierRequest {
    @NotBlank(message = "供应商名称不能为空")
    private String name;
    private String category;
    private String contact;
    @Pattern(regexp = "^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$", message = "电话号码格式不正确")
    private String phone;
    private String address;
}
