package com.ra.st.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class ReviewResponseDTO {
    private Long id;
    private String username;
    private Integer rating;
    private String comment;
    private Date createdAt;
    private List<ReviewReplyDTO> replies;
}