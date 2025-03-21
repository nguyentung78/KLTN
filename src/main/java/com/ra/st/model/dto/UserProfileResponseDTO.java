package com.ra.st.model.dto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponseDTO {
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private String address;
    private String avatar;
}

