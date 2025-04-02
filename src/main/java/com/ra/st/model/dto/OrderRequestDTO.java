package com.ra.st.model.dto;

import com.ra.st.model.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private Order.OrderStatus orderStatus;
}