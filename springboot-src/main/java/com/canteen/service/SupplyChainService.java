package com.canteen.service;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateSupplierRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.SupplierVO;

public interface SupplyChainService {

    Result<PageResult<SupplierVO>> listSuppliers(PageQuery query);

    SupplierVO addSupplier(CreateSupplierRequest request);

    SupplierVO getSupplierById(Long id);
}
