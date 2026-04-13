package com.canteen.common.exception;

import com.canteen.common.result.Result;
import com.canteen.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException ex) {
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccess(AccessDeniedException ex) {
        return Result.fail(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            if (msg.contains("phone")) {
                return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), "该手机号已被使用");
            }
            return Result.fail(ResultCode.BUSINESS_ERROR.getCode(), "数据重复，请检查输入");
        }
        log.error("Data integrity violation", ex);
        return Result.fail(ResultCode.SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleUnknown(Exception ex) {
        log.error("Unhandled exception", ex);
        return Result.fail(ResultCode.SERVER_ERROR);
    }
}
