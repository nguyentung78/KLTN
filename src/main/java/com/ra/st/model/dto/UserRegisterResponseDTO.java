package com.ra.st.model.dto;

import lombok.*;

import java.util.Date;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRegisterResponseDTO {
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private String address;
    private String avatar;
    private Set<RoleResponseDTO> roles;
    private Boolean status;
    private Date createdAt;
}
