package com.ra.st.model.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
public class RoleRequestDTO {
    @NotEmpty(message = "Danh sách quyền không được để trống")
    private Set<String> roles;
}
