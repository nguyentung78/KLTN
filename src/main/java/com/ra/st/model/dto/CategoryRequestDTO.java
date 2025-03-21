package com.ra.st.model.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryRequestDTO {
    private String categoryName;
    private String description;
    private Boolean status;
}
