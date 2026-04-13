package com.canteen.controller;

import com.canteen.common.result.PageResult;
import com.canteen.common.result.Result;
import com.canteen.dto.request.PageQuery;
import com.canteen.dto.response.ProductionTaskVO;
import com.canteen.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionController {

    private final ProductionService productionService;

    @GetMapping("/tasks")
    @PreAuthorize("hasAuthority('production:view')")
    public Result<PageResult<ProductionTaskVO>> listTasks(PageQuery query,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) String date) {
        return productionService.listTasks(query, status, date);
    }

    @PostMapping("/tasks/{id}/action")
    @PreAuthorize("hasAnyAuthority('production:task:start','production:task:complete')")
    public Result<Void> updateTaskStatus(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        String action = body.get("action");
        if ("start".equals(action)) {
            productionService.startTask(id);
        } else if ("complete".equals(action)) {
            productionService.completeTask(id);
        }
        return Result.ok();
    }

}
