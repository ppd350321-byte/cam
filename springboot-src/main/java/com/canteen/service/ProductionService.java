package com.canteen.service;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.ProductionTaskVO;

public interface ProductionService {

    Result<PageResult<ProductionTaskVO>> listTasks(PageQuery query, String status, String date);

    void startTask(Long taskId);

    void completeTask(Long taskId);
}
