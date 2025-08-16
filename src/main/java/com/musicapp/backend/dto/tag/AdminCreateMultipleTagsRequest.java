package com.musicapp.backend.dto.tag;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AdminCreateMultipleTagsRequest {

    @NotEmpty(message = "Tag names list cannot be empty")
    private List<String> names;
}