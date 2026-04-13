package com.canteen.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;

    public static <T> Result<PageResult<T>> of(List<T> list, long total, int page, int pageSize) {
        return Result.ok(new PageResult<>(list, total, page, pageSize));
    }
}
