package com.ra.st.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishListDTO {
    private Long productId;
    private String productName;
    private String productImage;
    private String productDescription;
    private BigDecimal productPrice;
}
