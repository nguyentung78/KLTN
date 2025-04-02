package com.ra.st.model.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryResponseDTO {
    private Long categoryId;
    private String categoryName;
    private String description;
    private Boolean status;
}
