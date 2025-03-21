package com.ra.st.model.dto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDTO {
    private Long id;
    private String fullAddress;
    private String phone;
    private String receiveName;
}
