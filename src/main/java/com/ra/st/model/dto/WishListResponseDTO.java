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
    private List<WishListDTO> content; // Danh sách sản phẩm yêu thích
    private int page;                  // Trang hiện tại
    private int size;                  // Số phần tử mỗi trang
    private long totalElements;        // Tổng số phần tử
    private int totalPages;            // Tổng số trang
}