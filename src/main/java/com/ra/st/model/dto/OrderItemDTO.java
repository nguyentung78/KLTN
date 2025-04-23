package com.ra.st.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemDTO {
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer orderQuantity;
    private boolean isReviewed;
}
