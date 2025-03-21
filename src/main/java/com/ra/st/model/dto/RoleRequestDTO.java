package com.ra.st.model.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class RoleRequestDTO {
    private Set<String> roles;
}
