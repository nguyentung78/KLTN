package com.ra.st.model.dto;

import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class NewAccountDTO {
    private String username;
    private String email;
    private Date createdAt;
}