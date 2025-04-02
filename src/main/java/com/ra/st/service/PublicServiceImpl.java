package com.ra.st.service;

import com.ra.st.model.dto.CategoryResponseDTO;
import com.ra.st.model.dto.ProductResponseDTO;
import com.ra.st.model.entity.Category;
import com.ra.st.model.entity.Product;
import com.ra.st.repository.CategoryRepository;
import com.ra.st.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicServiceImpl implements PublicService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // ✅ Lấy danh sách danh mục
    @Override
    public ResponseEntity<?> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponseDTO(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getDescription(),
                        category.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    // ✅ Lấy danh sách sản phẩm (có phân trang & sắp xếp)
    @Override
    public Page<ProductResponseDTO> getAllProducts(int page, int size, String sortBy, String order) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(product -> {
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setId(product.getId());
            dto.setProductName(product.getProductName());
            dto.setDescription(product.getDescription());
            dto.setUnitPrice(product.getUnitPrice());
            dto.setStockQuantity(product.getStockQuantity());
            dto.setImage(product.getImage());

            // Thêm thông tin category
            if (product.getCategory() != null) {
                CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
                categoryDTO.setCategoryId(product.getCategory().getCategoryId());
                categoryDTO.setCategoryName(product.getCategory().getCategoryName());
                categoryDTO.setDescription(product.getCategory().getDescription());
                categoryDTO.setStatus(product.getCategory().getStatus());
                dto.setCategory(categoryDTO);
            }

            dto.setFeatured(product.getFeatured());
            return dto;
        });
    }

    // ✅ Tìm kiếm sản phẩm theo tên/mô tả
    @Override
    public Page<ProductResponseDTO> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

        return products.map(product -> {
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setId(product.getId());
            dto.setProductName(product.getProductName());
            dto.setDescription(product.getDescription());
            dto.setUnitPrice(product.getUnitPrice());
            dto.setStockQuantity(product.getStockQuantity());
            dto.setImage(product.getImage());

            // Thêm thông tin category
            if (product.getCategory() != null) {
                CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
                categoryDTO.setCategoryId(product.getCategory().getCategoryId());
                categoryDTO.setCategoryName(product.getCategory().getCategoryName());
                categoryDTO.setDescription(product.getCategory().getDescription());
                categoryDTO.setStatus(product.getCategory().getStatus());
                dto.setCategory(categoryDTO);
            }

            dto.setFeatured(product.getFeatured());
            return dto;
        });
    }

    // ✅ Lấy danh sách sản phẩm nổi bật
    @Override
    public ResponseEntity<?> getFeaturedProducts() {
        List<ProductResponseDTO> products = productRepository.findByFeaturedTrue()
                .stream()
                .map(product -> {
                    ProductResponseDTO dto = new ProductResponseDTO();
                    dto.setId(product.getId());
                    dto.setProductName(product.getProductName());
                    dto.setDescription(product.getDescription());
                    dto.setUnitPrice(product.getUnitPrice());
                    dto.setStockQuantity(product.getStockQuantity());
                    dto.setImage(product.getImage());

                    // Thêm thông tin category
                    if (product.getCategory() != null) {
                        CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
                        categoryDTO.setCategoryId(product.getCategory().getCategoryId());
                        categoryDTO.setCategoryName(product.getCategory().getCategoryName());
                        categoryDTO.setDescription(product.getCategory().getDescription());
                        categoryDTO.setStatus(product.getCategory().getStatus());
                        dto.setCategory(categoryDTO);
                    }

                    dto.setFeatured(product.getFeatured());
                    return dto;
                })
                .collect(Collectors.toList());

        if (products.isEmpty()) {
            // Nếu không có sản phẩm nổi bật, lấy 10 sản phẩm mới nhất
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            products = productRepository.findNewProducts(pageable)
                    .stream()
                    .map(product -> {
                        ProductResponseDTO dto = new ProductResponseDTO();
                        dto.setId(product.getId());
                        dto.setProductName(product.getProductName());
                        dto.setDescription(product.getDescription());
                        dto.setUnitPrice(product.getUnitPrice());
                        dto.setStockQuantity(product.getStockQuantity());
                        dto.setImage(product.getImage());

                        // Thêm thông tin category
                        if (product.getCategory() != null) {
                            CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
                            categoryDTO.setCategoryId(product.getCategory().getCategoryId());
                            categoryDTO.setCategoryName(product.getCategory().getCategoryName());
                            categoryDTO.setDescription(product.getCategory().getDescription());
                            categoryDTO.setStatus(product.getCategory().getStatus());
                            dto.setCategory(categoryDTO);
                        }

                        dto.setFeatured(product.getFeatured());
                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(products);
    }

    // ✅ Lấy danh sách sản phẩm mới nhất
    @Override
    public ResponseEntity<?> getNewProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductResponseDTO> products = productRepository.findNewProducts(pageable)
                .stream()
                .map(product -> {
                    ProductResponseDTO dto = new ProductResponseDTO();
                    dto.setId(product.getId());
                    dto.setProductName(product.getProductName());
                    dto.setDescription(product.getDescription());
                    dto.setUnitPrice(product.getUnitPrice());
                    dto.setStockQuantity(product.getStockQuantity());
                    dto.setImage(product.getImage());

                    if (product.getCategory() != null) {
                        CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
                        categoryDTO.setCategoryId(product.getCategory().getCategoryId());
                        categoryDTO.setCategoryName(product.getCategory().getCategoryName());
                        categoryDTO.setDescription(product.getCategory().getDescription());
                        categoryDTO.setStatus(product.getCategory().getStatus());
                        dto.setCategory(categoryDTO);
                    }

                    dto.setFeatured(product.getFeatured());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // ✅ Lấy danh sách sản phẩm bán chạy
    @Override
    public ResponseEntity<?> getBestSellerProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> results = productRepository.findBestSellingProducts(pageable);

        List<ProductResponseDTO> products = results.stream()
                .map(result -> {
                    Product product = (Product) result[0];
                    ProductResponseDTO dto = new ProductResponseDTO();
                    dto.setId(product.getId());
                    dto.setProductName(product.getProductName());
                    dto.setDescription(product.getDescription());
                    dto.setUnitPrice(product.getUnitPrice());
                    dto.setStockQuantity(product.getStockQuantity());
                    dto.setImage(product.getImage());

                    if (product.getCategory() != null) {
                        CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
                        categoryDTO.setCategoryId(product.getCategory().getCategoryId());
                        categoryDTO.setCategoryName(product.getCategory().getCategoryName());
                        categoryDTO.setDescription(product.getCategory().getDescription());
                        categoryDTO.setStatus(product.getCategory().getStatus());
                        dto.setCategory(categoryDTO);
                    }

                    dto.setFeatured(product.getFeatured());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // ✅ Lấy danh sách sản phẩm theo danh mục
    @Override
    public ResponseEntity<?> getProductsByCategory(Long categoryId) {
        List<ProductResponseDTO> products = productRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(product -> {
                    ProductResponseDTO dto = new ProductResponseDTO();
                    dto.setId(product.getId());
                    dto.setProductName(product.getProductName());
                    dto.setDescription(product.getDescription());
                    dto.setUnitPrice(product.getUnitPrice());
                    dto.setStockQuantity(product.getStockQuantity());
                    dto.setImage(product.getImage());

                    // Thêm thông tin category
                    if (product.getCategory() != null) {
                        CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
                        categoryDTO.setCategoryId(product.getCategory().getCategoryId());
                        categoryDTO.setCategoryName(product.getCategory().getCategoryName());
                        categoryDTO.setDescription(product.getCategory().getDescription());
                        categoryDTO.setStatus(product.getCategory().getStatus());
                        dto.setCategory(categoryDTO);
                    }

                    dto.setFeatured(product.getFeatured());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // ✅ Lấy chi tiết sản phẩm theo ID
    @Override
    public ResponseEntity<?> getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setUnitPrice(product.getUnitPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImage(product.getImage());

        // Thêm thông tin category
        if (product.getCategory() != null) {
            CategoryResponseDTO categoryDTO = new CategoryResponseDTO();
            categoryDTO.setCategoryId(product.getCategory().getCategoryId());
            categoryDTO.setCategoryName(product.getCategory().getCategoryName());
            categoryDTO.setDescription(product.getCategory().getDescription());
            categoryDTO.setStatus(product.getCategory().getStatus());
            dto.setCategory(categoryDTO);
        }

        dto.setFeatured(product.getFeatured());
        return ResponseEntity.ok(dto);
    }
}
