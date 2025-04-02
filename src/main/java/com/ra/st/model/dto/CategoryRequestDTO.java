package com.ra.st.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryRequestDTO {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;
    private String description;
    private Boolean status;
}
