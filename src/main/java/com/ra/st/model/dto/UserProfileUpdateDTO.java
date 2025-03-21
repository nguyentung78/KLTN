package com.ra.st.model.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileUpdateDTO {
    private String username;
    private String email;
    private String fullname;
    private String phone;
    private String address;
    private MultipartFile avatar;
}

