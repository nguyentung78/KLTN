package com.ra.st.controller;

import com.ra.st.model.dto.*;
import com.ra.st.model.entity.Category;
import com.ra.st.model.entity.Order;
import com.ra.st.model.entity.Product;
import com.ra.st.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    //=======================REVIEW=========================
    @GetMapping("/reviews/products")
    public ResponseEntity<Page<ProductReviewSummaryDTO>> getProductsWithAverageRating(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(adminService.getProductsWithAverageRating(page, size, sortBy, order, keyword));
    }
    @PostMapping("/reviews/{reviewId}/reply")
    public ResponseEntity<?> replyToReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReplyRequestDTO replyRequest) {
        return adminService.replyToReview(reviewId, replyRequest.getReply());
    }
    //=======================USER=========================

    // Lấy danh sách người dùng (phân trang, sắp xếp)
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size, sortBy, order));
    }

    // Tìm kiếm người dùng theo username
    @GetMapping("/users/search")
    public ResponseEntity<Page<UserResponseDTO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.searchUsers(keyword, page, size));
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long userId, @RequestBody RoleRequestDTO roleRequest) {
        return adminService.updateUserRoles(userId, roleRequest);
    }

    @DeleteMapping("/users/{userId}/roles")
    public ResponseEntity<?> removeUserRoles(@PathVariable Long userId, @RequestBody RoleRequestDTO roleRequest) {
        return adminService.removeUserRoles(userId, roleRequest);
    }

    // ✅ Khóa / Mở khóa tài khoản
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        return adminService.toggleUserStatus(userId);
    }

    //=======================ROLE=========================

    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles() {
        return adminService.getAllRoles();
    }

    //=======================CATEGORIES=========================

    // Lấy danh sách danh mục (phân trang, sắp xếp)
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "categoryId") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        return adminService.getAllCategories(page, size, sortBy, order);
    }

    // Lấy thông tin danh mục theo ID
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long categoryId) {
        return adminService.getCategoryById(categoryId);
    }

    // Thêm mới danh mục
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequestDTO categoryRequest) {
        return adminService.createCategory(categoryRequest);
    }

    // Chỉnh sửa thông tin danh mục
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId,@Valid @RequestBody CategoryRequestDTO categoryRequest) {
        return adminService.updateCategory(categoryId, categoryRequest);
    }

    //Xóa danh mục
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        return adminService.deleteCategory(categoryId);
    }

    //Tìm kiếm danh mục
    @GetMapping("/categories/search")
    public ResponseEntity<?> searchCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminService.searchCategories(keyword, page, size);
    }

    //=======================PRODUCTS=========================

    // Lấy danh sách sản phẩm (phân trang, sắp xếp)
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        return adminService.getAllProducts(page, size, sortBy, order);
    }
    // Tìm kiếm sản phẩm theo keyword
    @GetMapping("/products/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return adminService.searchProducts(keyword, page, size);
    }
    // Lấy chi tiết sản phẩm theo ID
    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        return adminService.getProductById(productId);
    }

    // Thêm mới
    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestParam("productName") String productName,
            @RequestParam("description") String description,
            @RequestParam("unitPrice") BigDecimal unitPrice,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        ProductRequestDTO productRequest = ProductRequestDTO.builder()
                .productName(productName)
                .description(description)
                .unitPrice(unitPrice)
                .stockQuantity(stockQuantity)
                .categoryId(categoryId)
                .image(image)
                .build();

        return adminService.createProduct(productRequest);
    }

    // Chỉnh sửa sản phẩm
    @PutMapping(value = "/products/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "unitPrice", required = false) BigDecimal unitPrice,
            @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "featured", required = false) Boolean featured
    )

    {
        if (productName == null && description == null && unitPrice == null
                && stockQuantity == null && categoryId == null && image == null && featured == null) {
            return ResponseEntity.badRequest().body("Không có dữ liệu để cập nhật!");
        }
        ProductRequestDTO productRequest = ProductRequestDTO.builder()
                .productName(productName)
                .description(description)
                .unitPrice(unitPrice)
                .stockQuantity(stockQuantity)
                .categoryId(categoryId)
                .image(image)
                .featured(featured)
                .build();

        return adminService.updateProduct(productId, productRequest);
    }


    // Xóa sản phẩm
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        return adminService.deleteProduct(productId);
    }

    // ======================= ORDERS =========================

    // ấy danh sách tất cả đơn hàng (phân trang, sắp xếp)
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Page<OrderResponseDTO> orders = adminService.getAllOrders(page, size, direction, sortBy);
        return ResponseEntity.ok(orders);
    }

    // Lấy danh sách đơn hàng theo trạng thái (phân trang)
    @GetMapping("/orders/status/{orderStatus}")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByStatus(
            @PathVariable Order.OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderResponseDTO> orders = adminService.getOrdersByStatus(orderStatus, page, size);
        return ResponseEntity.ok(orders); // ✅ Bọc trong ResponseEntity
    }

    // Lấy chi tiết đơn hàng theo ID
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId) {
        OrderResponseDTO order = adminService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    // Cập nhật trạng thái đơn hàng
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusRequest request) {
        if (request.getOrderStatus() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        OrderResponseDTO updatedOrder = adminService.updateOrderStatus(orderId, request.getOrderStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    // DTO để nhận trạng thái đơn hàng từ request
    private static class OrderStatusRequest {
        private Order.OrderStatus orderStatus;

        public Order.OrderStatus getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(Order.OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
        }
    }

    // ======================= REPORT =========================

    // Doanh thu bán hàng theo thời gian
    @GetMapping("/reports/sales-revenue-over-time")
    public ResponseEntity<ReportResponseDTO> getSalesRevenueOverTime(
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {

        ReportRequestDTO request = new ReportRequestDTO(fromDate, toDate);
        return adminService.getSalesRevenueOverTime(request);
    }


    // Sản phẩm bán chạy nhất
    @GetMapping("/reports/best-seller-products")
    public ResponseEntity<ReportResponseDTO> getBestSellerProducts() {
        return adminService.getBestSellerProducts();
    }

    // Sản phẩm yêu thích nhất
    @GetMapping("/reports/most-liked-products")
    public ResponseEntity<ReportResponseDTO> getMostLikedProducts() {
        return adminService.getMostLikedProducts();
    }

    // Doanh thu theo danh mục
    @GetMapping("/reports/revenue-by-category")
    public ResponseEntity<?> getRevenueByCategory() {
        return adminService.getRevenueByCategory();
    }

    // Khách hàng chi tiêu nhiều nhất
    @GetMapping("/reports/top-spending-customer")
    public ResponseEntity<ReportResponseDTO> getTopSpendingCustomer() {
        return adminService.getTopSpendingCustomers();
    }

    // Tài khoản mới trong tháng
    @GetMapping("/reports/top-new-accounts-this-month")
    public ResponseEntity<ReportResponseDTO> getNewAccountsThisMonth() {
        return adminService.getNewAccountsThisMonth();
    }

    // Số lượng hóa đơn theo thời gian
    @GetMapping("/reports/invoices-over-time")
    public ResponseEntity<ReportResponseDTO> getInvoicesOverTime(
            @RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate) {

        ReportRequestDTO request = new ReportRequestDTO(fromDate, toDate);
        return adminService.getInvoicesOverTime(request);
    }
}
