package com.ra.st.model.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReportResponseDTO {
    private BigDecimal totalRevenue; // Doanh thu tổng cộng
    private Integer totalOrders; // Số lượng đơn hàng
    private List<String> topItems; // Danh sách sản phẩm hoặc khách hàng top
}
