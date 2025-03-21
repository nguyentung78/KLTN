package com.ra.st.model.dto;

import lombok.*;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductResponseDTO {
    private Long id;
    private String productName;
    private String description;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private String image;
    private Long categoryId;
    private Boolean featured;
}
