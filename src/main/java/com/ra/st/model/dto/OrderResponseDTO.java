package com.ra.st.model.dto;

import com.ra.st.model.entity.Order;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderResponseDTO {
    private Long id;
    private String serialNumber;
    private String username;
    private BigDecimal totalPrice;
    private Order.OrderStatus status;
    private String receiveName;
    private String receiveAddress;
    private String receivePhone;
    private Date createdAt;
    private Date receivedAt;
    private List<OrderItemDTO> items;
}
