package com.ra.st.model.dto;

import lombok.*;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserLoginResponse {
    private String username;
    private String typeToken;
    private String accessToken;
    private Set<RoleResponseDTO> roles;
}