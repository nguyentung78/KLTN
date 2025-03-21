package com.ra.st.model.dto;

import lombok.*;

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
    private Double productPrice;
}
