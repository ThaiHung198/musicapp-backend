package com.musicapp.backend.dto.tag;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTagRequest {
    @NotBlank(message = "Tên tag không được để trống")
    private String name;
}