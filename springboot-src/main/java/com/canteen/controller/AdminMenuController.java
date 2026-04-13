package com.canteen.controller;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.request.SaveCategoryRequest;
import com.canteen.dto.request.SaveDishRequest;
import com.canteen.dto.response.DishVO;
import com.canteen.dto.response.MenuCategoryVO;
import com.canteen.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

    // ── Categories ──

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('menu:view')")
    public Result<List<MenuCategoryVO>> listCategories() {
        return Result.ok(menuService.listAllCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('menu:add')")
    public Result<MenuCategoryVO> createCategory(@Valid @RequestBody SaveCategoryRequest request) {
        return Result.ok(menuService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('menu:edit')")
    public Result<MenuCategoryVO> updateCategory(@PathVariable Long id,
                                                  @Valid @RequestBody SaveCategoryRequest request) {
        return Result.ok(menuService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('menu:delete')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return Result.ok();
    }

    // ── Dishes ──

    @GetMapping("/dishes")
    @PreAuthorize("hasAuthority('menu:view')")
    public Result<PageResult<DishVO>> listDishes(PageQuery query,
                                                  @RequestParam(required = false) Long categoryId) {
        return menuService.listDishesAdmin(query, categoryId);
    }

    @PostMapping("/dishes")
    @PreAuthorize("hasAuthority('menu:add')")
    public Result<DishVO> createDish(@Valid @RequestBody SaveDishRequest request) {
        return Result.ok(menuService.createDish(request));
    }

    @PutMapping("/dishes/{id}")
    @PreAuthorize("hasAuthority('menu:edit')")
    public Result<DishVO> updateDish(@PathVariable Long id,
                                     @Valid @RequestBody SaveDishRequest request) {
        return Result.ok(menuService.updateDish(id, request));
    }

    @DeleteMapping("/dishes/{id}")
    @PreAuthorize("hasAuthority('menu:delete')")
    public Result<Void> deleteDish(@PathVariable Long id) {
        menuService.deleteDish(id);
        return Result.ok();
    }
}
