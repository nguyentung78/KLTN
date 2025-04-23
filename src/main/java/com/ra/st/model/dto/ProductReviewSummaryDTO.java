package com.ra.st.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewSummaryDTO {
    private Long id;
    private String productName;
    private Double averageRating;
    private Long reviewCount;
}
