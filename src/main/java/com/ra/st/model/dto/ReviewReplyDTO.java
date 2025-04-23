package com.ra.st.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ReviewReplyDTO {
    private Long id;
    private String reply;
    private Date createdAt;
    private String adminUsername;
    private String adminAvatar;
}