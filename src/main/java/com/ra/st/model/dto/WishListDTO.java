// src/main/java/com/ra/st/model/dto/WishListDTO.java
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
    private String image;
    private String description;
    private BigDecimal unitPrice;
}