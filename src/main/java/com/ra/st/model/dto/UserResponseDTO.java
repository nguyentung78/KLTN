package com.ra.st.model.dto;

import lombok.*;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private String address;
    private String avatar;
    private boolean status;
    private Set<String> roles;
}
