package com.ra.st.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemRequestDTO {
    private Long productId;
    private Integer quantity;
}
