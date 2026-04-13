package com.canteen.service.impl;

import com.canteen.common.exception.BusinessException;
import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import com.canteen.dto.request.CreateSupplierRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.SupplierVO;
import com.canteen.entity.Supplier;
import com.canteen.repository.SupplierRepository;
import com.canteen.service.SupplyChainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupplyChainServiceImpl implements SupplyChainService {

    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public Result<PageResult<SupplierVO>> listSuppliers(PageQuery query) {
        String keyword = "all".equals(query.getKeyword()) ? null : query.getKeyword();
        String status = "all".equals(query.getStatus()) ? null : query.getStatus();

        Page<Supplier> page = supplierRepository.findByFilters(keyword, null, status, query.toPageable());
        List<SupplierVO> list = page.getContent().stream().map(this::toSupplierVO).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public SupplierVO addSupplier(CreateSupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setName(request.getName());
        supplier.setCategory(request.getCategory());
        supplier.setContact(request.getContact());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplierRepository.save(supplier);
        return toSupplierVO(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierVO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "供货商不存在"));
        return toSupplierVO(supplier);
    }

    private SupplierVO toSupplierVO(Supplier s) {
        SupplierVO vo = new SupplierVO();
        vo.setId(s.getId());
        vo.setName(s.getName());
        vo.setCategory(s.getCategory());
        vo.setContact(s.getContact());
        vo.setPhone(s.getPhone());
        vo.setAddress(s.getAddress());
        vo.setRating(s.getRating());
        vo.setStatus(s.getStatus());
        return vo;
    }
}
