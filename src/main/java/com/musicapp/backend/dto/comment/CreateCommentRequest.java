package com.musicapp.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 500, message = "Bình luận không được vượt quá 300 ký tự.")
    private String content;

}