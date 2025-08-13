package com.musicapp.backend.exception;

import com.musicapp.backend.dto.BaseResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; // THÊM IMPORT NÀY
import org.springframework.http.converter.HttpMessageNotReadableException; // THÊM IMPORT NÀY
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError; // THÊM IMPORT NÀY
import org.springframework.web.bind.MethodArgumentNotValidException; // THÊM IMPORT NÀY
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap; // THÊM IMPORT NÀY
import java.util.Map; // THÊM IMPORT NÀY

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- BẮT ĐẦU PHẦN THÊM MỚI ---

    /**
     * Bắt lỗi validation (@Valid) và trả về 400 Bad Request.
     * Đây là lỗi khi dữ liệu gửi lên không hợp lệ (ví dụ: content trống).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Lấy thông báo lỗi đầu tiên để hiển thị cho người dùng
        String firstErrorMessage = errors.values().stream().findFirst().orElse("Dữ liệu không hợp lệ.");

        BaseResponse<Object> response = new BaseResponse<>(false, firstErrorMessage, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Bắt lỗi khi request body bị thiếu hoặc sai định dạng JSON.
     * Trả về 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        BaseResponse<Object> response = new BaseResponse<>(false, "Yêu cầu không hợp lệ hoặc bị thiếu nội dung.", null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // --- KẾT THÚC PHẦN THÊM MỚI ---


    // --- CÁC TRÌNH XỬ LÝ EXCEPTION CŨ CỦA BẠN (GIỮ NGUYÊN) ---

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // Status 409
    public BaseResponse<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Dữ liệu gửi lên bị trùng lặp hoặc vi phạm ràng buộc.";
        if (ex.getRootCause() != null) {
            message = ex.getRootCause().getMessage();
        }
        return new BaseResponse<>(false, message, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // Status 401
    public BaseResponse<Object> handleBadCredentialsException(BadCredentialsException ex) {
        return new BaseResponse<>(false, "Tài khoản hoặc mật khẩu không chính xác.", null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Status 404
    public BaseResponse<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new BaseResponse<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // Status 409
    public BaseResponse<Object> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        return new BaseResponse<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Status 400
    public BaseResponse<Object> handleBadRequestException(BadRequestException ex) {
        return new BaseResponse<>(false, ex.getMessage(), null);
    }

    /**
     * Trình xử lý lỗi chung cho các exception không được xử lý cụ thể.
     * Trả về status 500 Internal Server Error.
     * ĐẶT Ở CUỐI CÙNG
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Status 500
    public BaseResponse<Object> handleGeneralException(Exception ex) {
        // Ghi log lỗi ở đây để debug
        ex.printStackTrace(); // In stack trace ra console để dễ debug
        return new BaseResponse<>(false, "Đã có lỗi không mong muốn xảy ra.", null);
    }
}