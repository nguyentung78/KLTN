package com.ra.st.model.dto;

import lombok.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReportRequestDTO {
    private Date fromDate;
    private Date toDate;
}
