package com.ra.st.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReportRequestDTO {
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Date fromDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private Date toDate;
}