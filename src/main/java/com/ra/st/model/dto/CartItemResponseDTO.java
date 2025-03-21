package com.ra.st.model.dto;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponseDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String image;
    private BigDecimal unitPrice;
    private Integer quantity;
}
