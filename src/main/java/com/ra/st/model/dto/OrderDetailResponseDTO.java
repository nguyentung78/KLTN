package com.ra.st.model.dto;

import com.ra.st.model.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponseDTO {
    private Long id;
    private String serialNumber;
    private BigDecimal totalPrice;
    private Order.OrderStatus status;
    private String receiveName;
    private String receivePhone;
    private String receiveAddress;
    private Date createdAt;
    private List<OrderItemDTO> items;
}

