package com.ra.st.model.dto;

import lombok.Data;

@Data
public class PaypalRequest {
    private String total;
    private String currency = "VND"; // Mặc định là VND
}