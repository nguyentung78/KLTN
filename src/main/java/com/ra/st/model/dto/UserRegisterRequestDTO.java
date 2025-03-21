package com.ra.st.model.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRegisterRequestDTO {
    private String username;
    private String email;
    private String fullname;
    private String password;
    private MultipartFile avatar;
    private String phone;
    private String address;

}
