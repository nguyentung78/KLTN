package com.ra.st.model.dto;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordRequestDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
}
