package com.ra.st.model.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductRequestDTO {
    private String sku;
    private String productName;
    private String description;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private MultipartFile image;
    private Long categoryId;
    private Boolean featured;
}
