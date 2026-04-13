package com.canteen.service;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.CreateProcurementRequest;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.ProcurementVO;

public interface ProcurementService {

    Result<PageResult<ProcurementVO>> listProcurements(PageQuery query);

    ProcurementVO createProcurement(CreateProcurementRequest request, Long operatorId);

    void approveProcurement(Long id, Long approverId);

    void receiveProcurement(Long id);

    void completeProcurement(Long id);

    ProcurementVO getProcurementById(Long id);
}
