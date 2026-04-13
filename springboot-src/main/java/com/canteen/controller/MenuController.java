package com.canteen.controller;

import com.canteen.common.result.Result;
import com.canteen.dto.response.DishVO;
import com.canteen.dto.response.MenuCategoryVO;
import com.canteen.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * Mobile: GET /api/menu returns { categories: [...], dishes: [...] }
     */
    @GetMapping
    public Result<Map<String, Object>> getMenu() {
        List<MenuCategoryVO> categories = menuService.listCategories();
        List<DishVO> allDishes = new ArrayList<>();
        for (MenuCategoryVO cat : categories) {
            allDishes.addAll(menuService.listDishesByCategoryId(cat.getId()));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categories", categories);
        data.put("dishes", allDishes);
        return Result.ok(data);
    }
}
