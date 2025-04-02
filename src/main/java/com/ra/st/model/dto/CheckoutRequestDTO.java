package com.ra.st.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequestDTO {
    private Long addressId; // Không bắt buộc nếu cung cấp thông tin nhận hàng thủ công

    @Size(max = 255, message = "Địa chỉ nhận hàng không được vượt quá 255 ký tự")
    private String receiveAddress;

    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại nhận hàng phải có 10 chữ số")
    private String receivePhone;

    @Size(max = 50, message = "Tên người nhận không được vượt quá 50 ký tự")
    private String receiveName;

    @Size(max = 100, message = "Ghi chú không được vượt quá 100 ký tự")
    private String note;
}