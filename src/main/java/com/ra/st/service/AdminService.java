package com.ra.st.service;

import com.ra.st.model.dto.*;
import com.ra.st.model.entity.Order;
import com.ra.st.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface AdminService {
    ResponseEntity<?> updateUserRoles(Long userId, RoleRequestDTO roleRequest);
    Page<UserResponseDTO> getAllUsers(int page, int size, String sortBy, String order);
    Page<UserResponseDTO> searchUsers(String keyword, int page, int size);
    ResponseEntity<?> removeUserRoles(Long userId, RoleRequestDTO roleRequest);
    ResponseEntity<?> toggleUserStatus(Long userId);

    ResponseEntity<?> getAllRoles();

    ResponseEntity<?> searchCategories(String keyword, int page, int size);
    ResponseEntity<?> getAllCategories(int page, int size, String sortBy, String order);
    ResponseEntity<?> getCategoryById(Long categoryId);
    ResponseEntity<?> createCategory(CategoryRequestDTO categoryRequest);
    ResponseEntity<?> updateCategory(Long categoryId, CategoryRequestDTO categoryRequest);
    ResponseEntity<?> deleteCategory(Long categoryId);

    ResponseEntity<?> getAllProducts(int page, int size, String sortBy, String order);
    ResponseEntity<?> getProductById(Long productId);
    ResponseEntity<?> createProduct(ProductRequestDTO productRequest);
    ResponseEntity<?> updateProduct(Long productId, ProductRequestDTO productRequest);
    ResponseEntity<?> deleteProduct(Long productId);
    ResponseEntity<?> searchProducts(String keyword, int page, int size);

    Page<OrderResponseDTO> getAllOrders(int page, int size, String sortBy, String order);
    Page<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus orderStatus, int page, int size);
    OrderResponseDTO getOrderById(Long orderId);
    OrderResponseDTO updateOrderStatus(Long orderId, Order.OrderStatus orderStatus);
    ResponseEntity<ReportResponseDTO> getSalesRevenueOverTime(ReportRequestDTO request);
    ResponseEntity<ReportResponseDTO> getBestSellerProducts();
    ResponseEntity<ReportResponseDTO> getMostLikedProducts();
    ResponseEntity<?> getRevenueByCategory();
    ResponseEntity<ReportResponseDTO> getTopSpendingCustomers();
    ResponseEntity<ReportResponseDTO> getNewAccountsThisMonth();
    ResponseEntity<ReportResponseDTO> getInvoicesOverTime(ReportRequestDTO request);
}
