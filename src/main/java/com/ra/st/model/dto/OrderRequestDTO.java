package com.ra.st.model.dto;
import com.ra.st.model.entity.Order.OrderStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    private OrderStatus orderStatus;
}
