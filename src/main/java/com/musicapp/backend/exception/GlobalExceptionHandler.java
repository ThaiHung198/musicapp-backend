// src/main/java/com/musicapp/backend/exception/GlobalExceptionHandler.java
package com.musicapp.backend.exception;

import com.musicapp.backend.dto.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

// ... các import khác nếu có ...

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // Status 409
    public BaseResponse<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        // Lấy thông điệp lỗi gốc để xem chi tiết hơn
        String message = "Dữ liệu gửi lên bị trùng lặp hoặc vi phạm ràng buộc.";
        if (ex.getRootCause() != null) {
            message = ex.getRootCause().getMessage();
        }
        return BaseResponse.error(message);
    }

    // <<< THÊM PHƯƠNG THỨC MỚI NÀY VÀO >>>
    /**
     * Xử lý các lỗi liên quan đến thông tin đăng nhập không hợp lệ (sai email/mật khẩu).
     * Trả về status 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // Status 401
    public BaseResponse<Void> handleBadCredentialsException(BadCredentialsException ex) {
        // Trả về một thông báo lỗi chung chung, thân thiện và an toàn
        return BaseResponse.error("Tài khoản hoặc mật khẩu không chính xác.");
    }

    // --- CÁC TRÌNH XỬ LÝ EXCEPTION KHÁC CỦA BẠN (GIỮ NGUYÊN) ---

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Status 404
    public BaseResponse<Void> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return BaseResponse.error(ex.getMessage());
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // Status 409
    public BaseResponse<Void> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        return BaseResponse.error(ex.getMessage());
    }

    // ... các handler khác ...

    /**
     * Trình xử lý lỗi chung cho các exception không được xử lý cụ thể.
     * Trả về status 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Status 500
    public BaseResponse<Void> handleGeneralException(Exception ex) {
        // Ghi log lỗi ở đây để debug
        // log.error("An unexpected error occurred: ", ex);
        return BaseResponse.error("Đã có lỗi không mong muốn xảy ra: " + ex.getMessage());
    }
    /**
     * Xử lý các lỗi yêu cầu không hợp lệ chung (ví dụ: mật khẩu không khớp).
     * Trả về status 400 Bad Request.
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Status 400
    public BaseResponse<Void> handleBadRequestException(BadRequestException ex) {
        return BaseResponse.error(ex.getMessage());
    }


}