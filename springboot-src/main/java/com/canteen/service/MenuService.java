package com.canteen.service;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.request.SaveCategoryRequest;
import com.canteen.dto.request.SaveDishRequest;
import com.canteen.dto.response.DishVO;
import com.canteen.dto.response.MenuCategoryVO;

import java.util.List;

public interface MenuService {

    List<MenuCategoryVO> listCategories();

    List<DishVO> listDishesByCategoryId(Long categoryId);

    DishVO getDishById(Long id);

    // ── Admin CRUD ──

    List<MenuCategoryVO> listAllCategories();

    MenuCategoryVO createCategory(SaveCategoryRequest request);

    MenuCategoryVO updateCategory(Long id, SaveCategoryRequest request);

    void deleteCategory(Long id);

    Result<PageResult<DishVO>> listDishesAdmin(PageQuery query, Long categoryId);

    DishVO createDish(SaveDishRequest request);

    DishVO updateDish(Long id, SaveDishRequest request);

    void deleteDish(Long id);
}
