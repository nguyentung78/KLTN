// src/main/java/com/ra/st/model/dto/WishListResponseDTO.java
package com.ra.st.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishListResponseDTO {
    private List<WishListDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}