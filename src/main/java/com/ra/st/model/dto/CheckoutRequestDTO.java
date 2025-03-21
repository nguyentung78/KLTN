package com.ra.st.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequestDTO {
    private Long addressId;
    private String receiveAddress;
    private String receivePhone;
    private String receiveName;
}
