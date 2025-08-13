package com.musicapp.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

}