package com.ra.st.model.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TopItemDTO {
    private String name;
    private BigDecimal value;
}