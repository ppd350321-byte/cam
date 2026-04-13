package com.canteen.controller;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateProcurementRequest;
import com.canteen.dto.request.CreateSupplierRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.ProcurementVO;
import com.canteen.dto.response.SupplierVO;
import com.canteen.entity.Inventory;
import com.canteen.entity.Material;
import com.canteen.entity.Supplier;
import com.canteen.repository.InventoryRepository;
import com.canteen.repository.MaterialRepository;
import com.canteen.repository.SupplierRepository;
import com.canteen.security.SecurityUser;
import com.canteen.service.ProcurementService;
import com.canteen.service.SupplyChainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supply")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyChainService supplyChainService;
    private final ProcurementService procurementService;
    private final MaterialRepository materialRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;

    // ── Materials ──
    @GetMapping("/materials")
    @PreAuthorize("hasAuthority('supply:view')")
    @Transactional(readOnly = true)
    public Result<PageResult<Map<String, Object>>> listMaterials(PageQuery query,
                                                                  @RequestParam(required = false) String category) {
        String keyword = (query.getKeyword() != null && !query.getKeyword().isBlank()) ? query.getKeyword().trim() : null;
        String cat = (category != null && !"all".equals(category) && !category.isBlank()) ? category : null;
        Page<Material> page = materialRepository.findByFilters(keyword, cat,
                PageRequest.of(query.getPage() - 1, query.getPageSize()));
        List<Map<String, Object>> list = page.getContent().stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("sku", m.getSku());
            map.put("category", m.getCategory());
            map.put("unit", m.getUnit());
            map.put("unitCost", m.getUnitCost());
            map.put("safetyStock", m.getSafetyStock());
            map.put("supplierName", m.getSupplier() != null ? m.getSupplier().getName() : null);
            map.put("supplierId", m.getSupplier() != null ? m.getSupplier().getId() : null);
            BigDecimal stock = inventoryRepository.findByMaterialId(m.getId())
                    .map(Inventory::getCurrentStock).orElse(BigDecimal.ZERO);
            map.put("currentStock", stock);
            return map;
        }).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @GetMapping("/materials/all")
    @PreAuthorize("hasAuthority('supply:view')")
    @Transactional(readOnly = true)
    public Result<List<Map<String, Object>>> listAllMaterials() {
        List<Map<String, Object>> list = materialRepository.findAll().stream().map(m -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("unit", m.getUnit());
            map.put("unitCost", m.getUnitCost());
            return map;
        }).toList();
        return Result.ok(list);
    }

    @GetMapping("/materials/categories")
    @PreAuthorize("hasAuthority('supply:view')")
    public Result<List<String>> listMaterialCategories() {
        return Result.ok(materialRepository.findDistinctCategories());
    }

    @PostMapping("/materials")
    @PreAuthorize("hasAuthority('supply:view')")
    public Result<Map<String, Object>> addMaterial(@RequestBody Map<String, Object> body) {
        Material m = new Material();
        m.setName((String) body.get("name"));
        m.setSku((String) body.get("sku"));
        m.setCategory((String) body.get("category"));
        m.setUnit((String) body.get("unit"));
        if (body.get("unitCost") != null) m.setUnitCost(new BigDecimal(body.get("unitCost").toString()));
        if (body.get("safetyStock") != null) m.setSafetyStock(new BigDecimal(body.get("safetyStock").toString()));
        if (body.get("supplierId") != null) {
            supplierRepository.findById(Long.valueOf(body.get("supplierId").toString()))
                    .ifPresent(m::setSupplier);
        }
        materialRepository.save(m);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getName());
        return Result.ok(map);
    }

    @PutMapping("/materials/{id}")
    @PreAuthorize("hasAuthority('supply:view')")
    public Result<Void> updateMaterial(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Material m = materialRepository.findById(id)
                .orElseThrow(() -> new com.canteen.common.exception.BusinessException(
                        com.canteen.common.result.ResultCode.NOT_FOUND, "原料不存在"));
        if (body.containsKey("name")) m.setName((String) body.get("name"));
        if (body.containsKey("sku")) m.setSku((String) body.get("sku"));
        if (body.containsKey("category")) m.setCategory((String) body.get("category"));
        if (body.containsKey("unit")) m.setUnit((String) body.get("unit"));
        if (body.get("unitCost") != null) m.setUnitCost(new BigDecimal(body.get("unitCost").toString()));
        if (body.get("safetyStock") != null) m.setSafetyStock(new BigDecimal(body.get("safetyStock").toString()));
        if (body.get("supplierId") != null) {
            supplierRepository.findById(Long.valueOf(body.get("supplierId").toString()))
                    .ifPresent(m::setSupplier);
        }
        materialRepository.save(m);
        return Result.ok();
    }

    @DeleteMapping("/materials/{id}")
    @PreAuthorize("hasAuthority('supply:view')")
    public Result<Void> deleteMaterial(@PathVariable Long id) {
        materialRepository.deleteById(id);
        return Result.ok();
    }

    // ── Suppliers ──
    @GetMapping("/suppliers")
    @PreAuthorize("hasAuthority('supply:view')")
    public Result<PageResult<SupplierVO>> listSuppliers(PageQuery query) {
        return supplyChainService.listSuppliers(query);
    }

    @PostMapping("/suppliers")
    @PreAuthorize("hasAuthority('supply:supplier:add')")
    public Result<SupplierVO> addSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return Result.ok(supplyChainService.addSupplier(request));
    }

    // ── Procurements ──
    @GetMapping("/procurements")
    @PreAuthorize("hasAuthority('supply:view')")
    public Result<PageResult<ProcurementVO>> listProcurements(PageQuery query) {
        return procurementService.listProcurements(query);
    }

    @PostMapping("/procurements")
    @PreAuthorize("hasAuthority('supply:procurement:add')")
    public Result<ProcurementVO> addProcurement(@AuthenticationPrincipal SecurityUser user,
                                                @Valid @RequestBody CreateProcurementRequest request) {
        return Result.ok(procurementService.createProcurement(request, user.getId()));
    }

    @PatchMapping("/procurements/{id}/status")
    @PreAuthorize("hasAnyAuthority('supply:procurement:approve','supply:procurement:receive')")
    public Result<Void> updateProcurementStatus(@AuthenticationPrincipal SecurityUser user,
                                                @PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if ("approved".equals(status)) {
            procurementService.approveProcurement(id, user.getId());
        } else if ("received".equals(status)) {
            procurementService.receiveProcurement(id);
        } else if ("completed".equals(status)) {
            procurementService.completeProcurement(id);
        }
        return Result.ok();
    }
}
