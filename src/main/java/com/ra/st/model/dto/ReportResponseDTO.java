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
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private List<TopItemDTO> topItems;
    private List<NewAccountDTO> newAccounts;
    private List<TimeSeriesDTO> timeSeriesData;
}