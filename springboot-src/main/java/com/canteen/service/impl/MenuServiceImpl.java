package com.canteen.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.request.SaveCategoryRequest;
import com.canteen.dto.request.SaveDishRequest;
import com.canteen.dto.response.DishVO;
import com.canteen.dto.response.MenuCategoryVO;
import com.canteen.entity.Dish;
import com.canteen.entity.DishMaterial;
import com.canteen.entity.Inventory;
import com.canteen.entity.Material;
import com.canteen.entity.MenuCategory;
import com.canteen.repository.DishMaterialRepository;
import com.canteen.repository.DishRepository;
import com.canteen.repository.InventoryRepository;
import com.canteen.repository.MaterialRepository;
import com.canteen.repository.MenuCategoryRepository;
import com.canteen.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final DishRepository dishRepository;
    private final DishMaterialRepository dishMaterialRepository;
    private final MaterialRepository materialRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Cacheable(value = "menuCategories", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<MenuCategoryVO> listCategories() {
        Set<String> seen = new LinkedHashSet<>();
        return menuCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream().map(this::toCategoryVO)
                .filter(vo -> seen.add(vo.getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishVO> listDishesByCategoryId(Long categoryId) {
        return dishRepository.findAvailableByCategoryId(categoryId)
                .stream().map(this::toDishVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DishVO getDishById(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "菜品不存在"));
        return toDishVO(dish);
    }

    private MenuCategoryVO toCategoryVO(MenuCategory cat) {
        MenuCategoryVO vo = new MenuCategoryVO();
        vo.setId(cat.getId());
        vo.setName(cat.getName());
        vo.setIcon(cat.getIcon());
        vo.setSortOrder(cat.getSortOrder());
        vo.setIsActive(cat.getIsActive());
        return vo;
    }

    private DishVO toDishVO(Dish dish) {
        DishVO vo = new DishVO();
        vo.setId(dish.getId());
        vo.setCategoryId(dish.getCategory() != null ? dish.getCategory().getId() : null);
        vo.setCategory(dish.getCategory() != null ? dish.getCategory().getName() : null);
        vo.setName(dish.getName());
        vo.setDescription(dish.getDescription());
        vo.setImageUrl(dish.getImageUrl());
        vo.setImage(dish.getImageUrl());
        vo.setPrice(dish.getPrice());
        vo.setOriginalPrice(dish.getOriginalPrice());
        vo.setUseCustomVipPrice(Boolean.TRUE.equals(dish.getUseCustomVipPrice()));
        if (Boolean.TRUE.equals(dish.getUseCustomVipPrice()) && dish.getVipPrice() != null) {
            vo.setVipPrice(dish.getVipPrice());
        } else {
            vo.setVipPrice(dish.getPrice().multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP));
        }
        vo.setStock(dish.getStock());
        vo.setSales(dish.getSales());
        vo.setAvailable(dish.getAvailable());
        vo.setIsSetMeal(dish.getIsSetMeal());
        vo.setTags(dish.getTags());
        // Load materials
        List<DishMaterial> dms = dishMaterialRepository.findByDishId(dish.getId());
        if (dms != null && !dms.isEmpty()) {
            vo.setMaterials(dms.stream().map(dm -> {
                DishVO.DishMaterialVO mv = new DishVO.DishMaterialVO();
                mv.setMaterialId(dm.getMaterial().getId());
                mv.setMaterialName(dm.getMaterial().getName());
                mv.setQuantityPerServing(dm.getQuantityPerServing());
                mv.setUnit(dm.getUnit());
                return mv;
            }).toList());
        }
        return vo;
    }

    // ── Admin CRUD ──

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategoryVO> listAllCategories() {
        return menuCategoryRepository.findAllByOrderBySortOrderAsc()
                .stream().map(this::toCategoryVO).toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "menuCategories", allEntries = true)
    public MenuCategoryVO createCategory(SaveCategoryRequest request) {
        MenuCategory cat = new MenuCategory();
        cat.setName(request.getName());
        cat.setIcon(request.getIcon());
        cat.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        cat.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return toCategoryVO(menuCategoryRepository.save(cat));
    }

    @Override
    @Transactional
    @CacheEvict(value = "menuCategories", allEntries = true)
    public MenuCategoryVO updateCategory(Long id, SaveCategoryRequest request) {
        MenuCategory cat = menuCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "分类不存在"));
        if (request.getName() != null) cat.setName(request.getName());
        if (request.getIcon() != null) cat.setIcon(request.getIcon());
        if (request.getSortOrder() != null) cat.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) cat.setIsActive(request.getIsActive());
        return toCategoryVO(menuCategoryRepository.save(cat));
    }

    @Override
    @Transactional
    @CacheEvict(value = "menuCategories", allEntries = true)
    public void deleteCategory(Long id) {
        if (!menuCategoryRepository.existsById(id)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        menuCategoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<DishVO>> listDishesAdmin(PageQuery query, Long categoryId) {
        String keyword = (query.getKeyword() == null || query.getKeyword().isBlank()) ? null : query.getKeyword();
        Page<Dish> page = dishRepository.findByFiltersAdmin(keyword, categoryId, query.toPageable());
        List<DishVO> list = page.getContent().stream().map(this::toDishVO).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public DishVO createDish(SaveDishRequest request) {
        Dish dish = new Dish();
        applyDishFields(dish, request);
        dish = dishRepository.save(dish);
        saveDishMaterials(dish, request.getMaterials());
        return toDishVO(dish);
    }

    @Override
    @Transactional
    public DishVO updateDish(Long id, SaveDishRequest request) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "菜品不存在"));

        // Check material availability when stock is increasing
        int oldStock = dish.getStock() != null ? dish.getStock() : 0;
        int newStock = request.getStock() != null ? request.getStock() : oldStock;
        int increase = newStock - oldStock;
        if (increase > 0) {
            checkAndDeductMaterials(dish.getId(), increase);
        }

        applyDishFields(dish, request);
        dish = dishRepository.save(dish);
        dishMaterialRepository.deleteByDishId(dish.getId());
        saveDishMaterials(dish, request.getMaterials());
        return toDishVO(dish);
    }

    @Override
    @Transactional
    public void deleteDish(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "菜品不存在"));
        dish.setIsDeleted(true);
        dishRepository.save(dish);
    }

    private void applyDishFields(Dish dish, SaveDishRequest req) {
        if (req.getCategoryId() != null) {
            MenuCategory cat = menuCategoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "分类不存在"));
            dish.setCategory(cat);
        }
        dish.setName(req.getName());
        dish.setDescription(req.getDescription());
        dish.setImageUrl(req.getImageUrl());
        dish.setPrice(req.getPrice());
        dish.setOriginalPrice(req.getOriginalPrice());
        dish.setVipPrice(req.getVipPrice());
        if (req.getStock() != null) dish.setStock(req.getStock());
        if (req.getAvailable() != null) dish.setAvailable(req.getAvailable());
        if (req.getIsSetMeal() != null) dish.setIsSetMeal(req.getIsSetMeal());
        dish.setTags(req.getTags());
        if (req.getSortOrder() != null) dish.setSortOrder(req.getSortOrder());
        if (req.getUseCustomVipPrice() != null) dish.setUseCustomVipPrice(req.getUseCustomVipPrice());
    }

    private void saveDishMaterials(Dish dish, List<SaveDishRequest.DishMaterialItem> items) {
        if (items == null || items.isEmpty()) return;
        List<DishMaterial> dms = new ArrayList<>();
        for (SaveDishRequest.DishMaterialItem item : items) {
            Material material = materialRepository.findById(item.getMaterialId())
                    .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "原料不存在: " + item.getMaterialId()));
            DishMaterial dm = new DishMaterial();
            dm.setDish(dish);
            dm.setMaterial(material);
            dm.setQuantityPerServing(item.getQuantityPerServing());
            dm.setUnit(item.getUnit() != null ? item.getUnit() : material.getUnit());
            dms.add(dm);
        }
        dishMaterialRepository.saveAll(dms);
    }

    private void checkAndDeductMaterials(Long dishId, int increase) {
        List<DishMaterial> dms = dishMaterialRepository.findByDishId(dishId);
        if (dms == null || dms.isEmpty()) return;

        BigDecimal qty = BigDecimal.valueOf(increase);
        for (DishMaterial dm : dms) {
            BigDecimal needed = dm.getQuantityPerServing().multiply(qty);
            Inventory inv = inventoryRepository.findByMaterialId(dm.getMaterial().getId()).orElse(null);
            BigDecimal available = inv != null ? inv.getCurrentStock() : BigDecimal.ZERO;
            if (available.compareTo(needed) < 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR,
                        "原料「" + dm.getMaterial().getName() + "」库存不足，需要 " + needed.toPlainString()
                                + " " + (dm.getUnit() != null ? dm.getUnit() : "") + "，当前库存 " + available.toPlainString());
            }
        }
        // Deduct materials
        for (DishMaterial dm : dms) {
            BigDecimal needed = dm.getQuantityPerServing().multiply(qty);
            Inventory inv = inventoryRepository.findByMaterialId(dm.getMaterial().getId()).orElseThrow();
            inv.setCurrentStock(inv.getCurrentStock().subtract(needed));
            inventoryRepository.save(inv);
        }
    }
}
