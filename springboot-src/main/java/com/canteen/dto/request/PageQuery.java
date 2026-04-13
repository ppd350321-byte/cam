package com.canteen.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
public class PageQuery {
    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int pageSize = 10;

    private String keyword;
    private String status = "all";

    public Pageable toPageable() {
        return PageRequest.of(page - 1, pageSize);
    }
}
