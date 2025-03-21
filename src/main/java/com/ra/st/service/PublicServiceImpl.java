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
                        category.getId(),
                        category.getCategoryName(),
                        category.getDescription(),
                        category.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    // ✅ Lấy danh sách sản phẩm (có phân trang & sắp xếp)
    @Override
    public Page<ProductResponseDTO> getAllProducts(int page, int size, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(product -> new ProductResponseDTO(
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getImage(),
                product.getCategory().getId(),
                product.getFeatured()
        ));
    }

    // ✅ Tìm kiếm sản phẩm theo tên/mô tả
    @Override
    public Page<ProductResponseDTO> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

        return products.map(product -> new ProductResponseDTO(
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getImage(),
                product.getCategory().getId(),
                product.getFeatured()
        ));
    }

    // ✅ Lấy danh sách sản phẩm nổi bật
    @Override
    public ResponseEntity<?> getFeaturedProducts() {
        List<ProductResponseDTO> products = productRepository.findByFeaturedTrue()
                .stream()
                .map(product -> new ProductResponseDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getUnitPrice(),
                        product.getStockQuantity(),
                        product.getImage(),
                        product.getCategory().getId(),
                        product.getFeatured()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // ✅ Lấy danh sách sản phẩm mới nhất
    @Override
    public ResponseEntity<?> getNewProducts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ProductResponseDTO> products = productRepository.findAll(pageable).stream()
                .map(product -> new ProductResponseDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getUnitPrice(),
                        product.getStockQuantity(),
                        product.getImage(),
                        product.getCategory().getId(),
                        product.getFeatured()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // ✅ Lấy danh sách sản phẩm bán chạy
    @Override
    public ResponseEntity<?> getBestSellerProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductResponseDTO> products = productRepository.findTopBestSellingProducts(pageable).stream()
                .map(product -> new ProductResponseDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getUnitPrice(),
                        product.getStockQuantity(),
                        product.getImage(),
                        product.getCategory().getId(),
                        product.getFeatured()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // ✅ Lấy danh sách sản phẩm theo danh mục
    @Override
    public ResponseEntity<?> getProductsByCategory(Long categoryId) {
        List<ProductResponseDTO> products = productRepository.findByCategoryId(categoryId)
                .stream()
                .map(product -> new ProductResponseDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getUnitPrice(),
                        product.getStockQuantity(),
                        product.getImage(),
                        product.getCategory().getId(),
                        product.getFeatured()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // ✅ Lấy chi tiết sản phẩm theo ID
    @Override
    public ResponseEntity<?> getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        return ResponseEntity.ok(new ProductResponseDTO(
                product.getId(),
                product.getProductName(),
                product.getDescription(),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getImage(),
                product.getCategory().getId(),
                product.getFeatured()
        ));
    }
}
