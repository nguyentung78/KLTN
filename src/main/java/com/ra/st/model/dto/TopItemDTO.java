package com.ra.st.model.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TopItemDTO {
    private String name; // Tên sản phẩm, khách hàng, hoặc danh mục
    private BigDecimal value; // Số lượng bán ra, doanh thu, hoặc số lượng yêu thích
}