package com.ra.st.service;

import com.ra.st.model.dto.*;
import com.ra.st.model.entity.*;
import com.ra.st.repository.*;
import com.ra.st.security.UserPrinciple;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminServiceImp implements AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImp.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReplyRepository reviewReplyRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    //=======================REVIEW=========================
    @Override
    public Page<ProductReviewSummaryDTO> getProductsWithAverageRating(int page, int size, String sortBy, String order, String keyword) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Object[]> productSummaries;

        if (keyword == null || keyword.trim().isEmpty()) {
            productSummaries = reportRepository.getProductsWithAverageRating(pageable);
        } else {
            productSummaries = reportRepository.searchProductsWithAverageRating(keyword, pageable);
        }

        return productSummaries.map(result -> new ProductReviewSummaryDTO(
                ((Number) result[0]).longValue(), // productId
                (String) result[1], // productName
                result[2] != null ? ((Number) result[2]).doubleValue() : 0.0, // averageRating
                ((Number) result[3]).longValue() // reviewCount
        ));
    }
    @Override
    @Transactional
    public ResponseEntity<?> replyToReview(Long reviewId, String reply) {
        // Kiểm tra đánh giá tồn tại
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại!"));

        // Kiểm tra nội dung phản hồi
        if (reply == null || reply.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Phản hồi không được để trống!");
        }

        // Lấy thông tin người dùng hiện tại từ Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập để thực hiện hành động này!");
        }

        // Lấy UserPrinciple từ authentication
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
        Users currentUser = userPrinciple.getUser();

        // Kiểm tra vai trò ADMIN
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == Role.RoleName.ADMIN);
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Chỉ admin mới có thể trả lời đánh giá!");
        }

        // Tạo phản hồi đánh giá
        ReviewReply reviewReply = new ReviewReply();
        reviewReply.setReview(review);
        reviewReply.setAdmin(currentUser); // Sử dụng admin hiện tại
        reviewReply.setReply(reply);
        reviewReply.setCreatedAt(new Date()); // Thêm thời gian tạo nếu cần
        reviewReplyRepository.save(reviewReply);

        return ResponseEntity.ok("Phản hồi đánh giá thành công!");
    }

    //=======================USER=========================
    @Override
    public Page<UserResponseDTO> getAllUsers(int page, int size, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Users> usersPage = userRepository.findAll(pageable);

        return usersPage.map(user -> new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullname(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatar(),
                user.isStatus(),
                user.getRoles().stream().map(role -> role.getRoleName().name()).collect(Collectors.toSet())
        ));
    }

    @Override
    public Page<UserResponseDTO> searchUsers(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Users> usersPage = userRepository.searchUsersByUsername(username, pageable);

        return usersPage.map(user -> new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullname(),
                user.getPhone(),
                user.getAddress(),
                user.getAvatar(),
                user.isStatus(),
                user.getRoles().stream().map(role -> role.getRoleName().name()).collect(Collectors.toSet())
        ));
    }

    @Override
    public ResponseEntity<?> removeUserRoles(Long userId, RoleRequestDTO roleRequest) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        Set<Role> currentRoles = user.getRoles();

        Set<Role> rolesToRemove = roleRequest.getRoles().stream()
                .map(roleName -> roleRepository.findRoleByRoleName(Role.RoleName.valueOf(roleName.toUpperCase())))
                .filter(role -> role != null && currentRoles.contains(role))
                .collect(Collectors.toSet());

        if (rolesToRemove.isEmpty()) {
            return ResponseEntity.badRequest().body("Không có quyền hợp lệ để xóa!");
        }

        currentRoles.removeAll(rolesToRemove);
        user.setRoles(currentRoles);
        userRepository.save(user);

        return ResponseEntity.ok("Xóa quyền thành công cho user: " + user.getUsername());
    }

    @Override
    public ResponseEntity<?> toggleUserStatus(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        user.setStatus(!user.isStatus());
        userRepository.save(user);

        return ResponseEntity.ok("Tài khoản của " + user.getUsername() + " đã " + (user.isStatus() ? "mở khóa" : "bị khóa"));
    }

    @Override
    public ResponseEntity<?> updateUserRoles(Long userId, RoleRequestDTO roleRequest) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        Set<Role> newRoles = roleRequest.getRoles().stream()
                .map(roleName -> roleRepository.findRoleByRoleName(Role.RoleName.valueOf(roleName.toUpperCase())))
                .filter(role -> role != null)
                .collect(Collectors.toSet());

        if (newRoles.isEmpty()) {
            return ResponseEntity.badRequest().body("Không có quyền hợp lệ được cung cấp!");
        }

        user.setRoles(newRoles);
        userRepository.save(user);

        return ResponseEntity.ok("Cập nhật quyền thành công cho user: " + user.getUsername());
    }

    //=======================ROLE=========================
    @Override
    public ResponseEntity<?> getAllRoles() {
        List<RoleResponseDTO> roles = roleRepository.findAll().stream()
                .map(role -> new RoleResponseDTO(role.getRoleName().name()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(roles);
    }

    //=======================CATEGORIES=========================
    @Override
    public ResponseEntity<?> getAllCategories(int page, int size, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Category> categories = categoryRepository.findAll(pageable);

        Page<CategoryResponseDTO> categoryResponse = categories.map(category ->
                new CategoryResponseDTO(category.getCategoryId(), category.getCategoryName(), category.getDescription(), category.getStatus()));

        return ResponseEntity.ok(categoryResponse);
    }

    @Override
    public ResponseEntity<?> getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
        return ResponseEntity.ok(new CategoryResponseDTO(category.getCategoryId(), category.getCategoryName(), category.getDescription(), category.getStatus()));
    }

    @Override
    public ResponseEntity<?> createCategory(CategoryRequestDTO categoryRequest) {
        if (categoryRepository.existsByCategoryName(categoryRequest.getCategoryName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Danh mục đã tồn tại!"));
        }

        Category newCategory = new Category();
        newCategory.setCategoryName(categoryRequest.getCategoryName());
        newCategory.setDescription(categoryRequest.getDescription());
        newCategory.setStatus(true);

        categoryRepository.save(newCategory);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Thêm danh mục thành công!"));
    }

    @Override
    public ResponseEntity<?> updateCategory(Long categoryId, CategoryRequestDTO categoryRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
        if (categoryRepository.existsByCategoryNameAndCategoryIdNot(categoryRequest.getCategoryName(), categoryId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Tên danh mục đã tồn tại. Vui lòng chọn tên khác!"));
        }

        if (categoryRequest.getCategoryName() != null && !categoryRequest.getCategoryName().trim().isEmpty()) {
            category.setCategoryName(categoryRequest.getCategoryName());
        }

        if (categoryRequest.getDescription() != null && !categoryRequest.getDescription().trim().isEmpty()) {
            category.setDescription(categoryRequest.getDescription());
        }

        if (categoryRequest.getStatus() != null) {
            category.setStatus(categoryRequest.getStatus());
        }

        categoryRepository.save(category);
        return ResponseEntity.ok(new SuccessResponse("Cập nhật danh mục thành công!"));
    }

    @Override
    public ResponseEntity<?> deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        if (productRepository.existsByCategoryCategoryId(categoryId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Không thể xóa danh mục vì vẫn còn sản phẩm liên quan!"));
        }

        categoryRepository.delete(category);
        return ResponseEntity.ok(new SuccessResponse("Xóa danh mục thành công!"));
    }

    @Override
    public ResponseEntity<?> searchCategories(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories;

        if (keyword == null || keyword.trim().isEmpty()) {
            categories = categoryRepository.findAll(pageable);
        } else {
            categories = categoryRepository.findByCategoryNameContainingIgnoreCase(keyword, pageable);
        }

        Page<CategoryResponseDTO> response = categories.map(category ->
                new CategoryResponseDTO(category.getCategoryId(), category.getCategoryName(), category.getDescription(), category.getStatus()));

        return ResponseEntity.ok(response);
    }

    //=======================PRODUCTS=========================
    @Override
    public ResponseEntity<?> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            productPage = productRepository.findAll(pageable); // Nếu không có keyword, trả về tất cả
        } else {
            productPage = productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);
        }

        // Chuyển đổi Page<Product> sang Page<ProductResponseDTO> để tránh vòng lặp
        Page<ProductResponseDTO> productDTOs = productPage.map(product -> {
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

        return ResponseEntity.ok(productDTOs);
    }

    @Override
    public ResponseEntity<?> getAllProducts(int page, int size, String sortBy, String order) {

        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findAll(pageable);

        // Chuyển đổi danh sách sản phẩm thành DTO
        Page<ProductResponseDTO> productDTOs = products.map(product -> {
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

        return ResponseEntity.ok(productDTOs);
    }

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

    @Override
    public ResponseEntity<?> createProduct(ProductRequestDTO productRequest) {
        // Validate thủ công
        if (productRequest.getProductName() == null || productRequest.getProductName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên sản phẩm không được để trống");
        }
        if (productRequest.getUnitPrice() == null || productRequest.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Giá sản phẩm phải lớn hơn 0");
        }
        if (productRequest.getStockQuantity() == null || productRequest.getStockQuantity() < 0) {
            return ResponseEntity.badRequest().body("Số lượng tồn kho phải lớn hơn hoặc bằng 0");
        }
        if (productRequest.getCategoryId() == null) {
            return ResponseEntity.badRequest().body("Danh mục không được để trống");
        }

        if (productRepository.existsByProductName(productRequest.getProductName())) {
            return ResponseEntity.badRequest().body("Sản phẩm đã tồn tại!");
        }

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        MultipartFile imageFile = productRequest.getImage();
        String imageUrl = "https://res.cloudinary.com/default-product.png"; // Ảnh mặc định

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = uploadService.uploadFile(imageFile);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload ảnh: " + e.getMessage());
            }
        }

        Product newProduct = new Product();
        newProduct.setProductName(productRequest.getProductName());
        newProduct.setDescription(productRequest.getDescription());
        newProduct.setUnitPrice(productRequest.getUnitPrice());
        newProduct.setStockQuantity(productRequest.getStockQuantity());
        newProduct.setImage(imageUrl);
        newProduct.setCategory(category);
        newProduct.setFeatured(productRequest.getFeatured() != null ? productRequest.getFeatured() : false);

        productRepository.save(newProduct);

        return ResponseEntity.ok("Thêm sản phẩm thành công!");
    }

    @Override
    public ResponseEntity<?> updateProduct(Long productId, ProductRequestDTO productRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (productRequest.getProductName() != null && !productRequest.getProductName().trim().isEmpty()) {
            product.setProductName(productRequest.getProductName());
        }

        if (productRequest.getDescription() != null && !productRequest.getDescription().trim().isEmpty()) {
            product.setDescription(productRequest.getDescription());
        }

        if (productRequest.getUnitPrice() != null) {
            product.setUnitPrice(productRequest.getUnitPrice());
        }

        if (productRequest.getStockQuantity() != null) {
            product.setStockQuantity(productRequest.getStockQuantity());
        }

        if (productRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(productRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
            product.setCategory(category);
        }

        if (productRequest.getFeatured() != null) {
            product.setFeatured(productRequest.getFeatured());
        }

        if (productRequest.getImage() != null && !productRequest.getImage().isEmpty()) {
            try {
                String imageUrl = uploadService.uploadFile(productRequest.getImage());
                product.setImage(imageUrl);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload ảnh: " + e.getMessage());
            }
        }

        productRepository.save(product);
        return ResponseEntity.ok("C Updating sản phẩm thành công!");
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteProduct(Long productId) {
        // Kiểm tra sản phẩm có tồn tại không
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // Xóa các mục trong giỏ hàng liên quan đến sản phẩm
        shoppingCartRepository.deleteByProduct(product);

        // Tìm tất cả các đơn hàng có trạng thái WAITING
        List<Order> waitingOrders = orderRepository.findByStatus(Order.OrderStatus.WAITING);
        for (Order order : waitingOrders) {
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
            boolean containsProduct = orderDetails.stream()
                    .anyMatch(od -> od.getProductId().equals(productId));
            if (containsProduct) {
                // Hủy đơn hàng
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
        }

        // Xóa sản phẩm
        productRepository.delete(product);
        return ResponseEntity.ok("Xóa sản phẩm thành công! Các mục trong giỏ hàng và đơn hàng liên quan (nếu có) đã được xử lý.");
    }

    //=======================ORDERS=========================
    @Override
    public Page<OrderResponseDTO> getAllOrders(int page, int size, String direction, String sortBy) {
        // Xác định hướng sắp xếp
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);

        // Tạo Pageable để phân trang
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lấy danh sách đơn hàng từ repository
        Page<Order> orders = orderRepository.findAll(pageable);

        // Chuyển đổi sang OrderResponseDTO
        return orders.map(order -> OrderResponseDTO.builder()
                .id(order.getId())
                .serialNumber(order.getSerialNumber())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .build());
    }

    @Override
    public Page<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus orderStatus, int page, int size) {
        // Tạo Pageable để phân trang (mặc định sắp xếp theo createdAt giảm dần)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Lấy danh sách đơn hàng theo trạng thái
        Page<Order> orders = orderRepository.findByStatus(orderStatus, pageable);

        // Chuyển đổi sang OrderResponseDTO
        return orders.map(order -> OrderResponseDTO.builder()
                .id(order.getId())
                .serialNumber(order.getSerialNumber())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .build());
    }

    @Override
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        // Lấy danh sách chi tiết đơn hàng
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        List<OrderItemDTO> items = orderDetails.stream()
                .map(item -> OrderItemDTO.builder()
                        .productId(item.getProductId())
                        .productName(item.getName())
                        .unitPrice(item.getUnitPrice())
                        .orderQuantity(item.getOrderQuantity())
                        .build()) // Không cần truyền isReviewed
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(order.getId())
                .serialNumber(order.getSerialNumber())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .items(items) // Thêm danh sách sản phẩm
                .build();
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, Order.OrderStatus orderStatus) {
        // Tìm đơn hàng theo ID
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        // Kiểm tra trạng thái hợp lệ
        if (orderStatus == null) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không được để trống!");
        }

        // Nếu chuyển sang DELIVERED, cập nhật ngày nhận hàng
        if (orderStatus == Order.OrderStatus.DELIVERED && order.getStatus() != Order.OrderStatus.DELIVERED) {
            order.setReceivedAt(new Date());
        }

        // Nếu hủy đơn hàng (CANCELLED), kiểm tra trạng thái hiện tại
        if (orderStatus == Order.OrderStatus.CANCELLED && order.getStatus() != Order.OrderStatus.WAITING) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng ở trạng thái WAITING!");
        }

        // Cập nhật trạng thái
        order.setStatus(orderStatus);
        orderRepository.save(order);

        // Trả về DTO
        return OrderResponseDTO.builder()
                .id(order.getId())
                .serialNumber(order.getSerialNumber())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .build();
    }
    // ====================== REPORTS & STATISTICS ======================
    @Override
    public ResponseEntity<ReportResponseDTO> getSalesRevenueOverTime(ReportRequestDTO request) {
        try {
            // Lấy dữ liệu doanh thu theo ngày
            List<Object[]> revenueData = reportRepository.getSalesRevenueOverTimeByDay(
                    request.getFromDate(), request.getToDate()
            );

            // Chuyển đổi dữ liệu thành danh sách TimeSeriesDTO
            List<TimeSeriesDTO> timeSeriesData = revenueData.stream()
                    .map(result -> new TimeSeriesDTO(
                            ((java.sql.Date) result[0]).toString(), // Ngày
                            result[1] instanceof BigDecimal
                                    ? (BigDecimal) result[1]
                                    : BigDecimal.valueOf(((Number) result[1]).doubleValue()) // Giữ nguyên BigDecimal hoặc chuyển đổi từ Number
                    ))
                    .collect(Collectors.toList());

            // Tính tổng doanh thu
            BigDecimal totalRevenue = timeSeriesData.stream()
                    .map(TimeSeriesDTO::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Trả về ResponseEntity chứa ReportResponseDTO
            ReportResponseDTO response = ReportResponseDTO.builder()
                    .totalRevenue(totalRevenue) // Đảm bảo ReportResponseDTO có trường totalRevenue kiểu BigDecimal
                    .timeSeriesData(timeSeriesData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Override
    public ResponseEntity<ReportResponseDTO> getBestSellerProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> topProducts = reportRepository.getBestSellerProducts(pageable);
        List<TopItemDTO> topItems = topProducts.stream()
                .map(result -> new TopItemDTO(
                        (String) result[0], // productName
                        BigDecimal.valueOf(((Number) result[1]).longValue()) // totalSold
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ReportResponseDTO.builder().topItems(topItems).build());
    }

    @Override
    public ResponseEntity<ReportResponseDTO> getMostLikedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> mostLikedProducts = reportRepository.getMostLikedProducts(pageable);
        List<TopItemDTO> topItems = mostLikedProducts.stream()
                .map(result -> new TopItemDTO(
                        (String) result[0], // productName
                        BigDecimal.valueOf(((Number) result[1]).longValue()) // likeCount
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ReportResponseDTO.builder().topItems(topItems).build());
    }

    @Override
    public ResponseEntity<?> getRevenueByCategory() {
        List<Object[]> revenueByCategory = reportRepository.getRevenueByCategory();
        List<TopItemDTO> topItems = revenueByCategory.stream()
                .map(result -> new TopItemDTO(
                        (String) result[0], // categoryName
                        (BigDecimal) result[1] // totalRevenue
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ReportResponseDTO.builder().topItems(topItems).build());
    }

    @Override
    public ResponseEntity<ReportResponseDTO> getTopSpendingCustomers() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> topCustomers = reportRepository.getTopSpendingCustomers(pageable);
        List<TopItemDTO> topItems = topCustomers.stream()
                .map(result -> new TopItemDTO(
                        (String) result[0], // username
                        (BigDecimal) result[1] // totalSpent
                ))
                .collect(Collectors.toList());
        BigDecimal totalRevenue = topCustomers.stream()
                .map(result -> (BigDecimal) result[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(
                ReportResponseDTO.builder()
                        .totalRevenue(totalRevenue)
                        .topItems(topItems)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ReportResponseDTO> getNewAccountsThisMonth() {
        Pageable pageable = PageRequest.of(0, 10);
        Long totalNewAccounts = reportRepository.getNewAccountsThisMonth();
        List<Object[]> newAccountsList = reportRepository.getNewAccountListThisMonth(pageable);
        List<NewAccountDTO> newAccounts = newAccountsList.stream()
                .map(result -> new NewAccountDTO(
                        (String) result[0], // username
                        (String) result[1], // email
                        (Date) result[2]    // createdAt
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ReportResponseDTO.builder()
                        .totalOrders(totalNewAccounts.intValue())
                        .newAccounts(newAccounts)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ReportResponseDTO> getInvoicesOverTime(ReportRequestDTO request) {
        try {
            // Lấy dữ liệu hóa đơn theo ngày
            List<Object[]> invoiceData = reportRepository.getInvoicesOverTimeByDay(
                    request.getFromDate(), request.getToDate()
            );

            // Chuyển đổi dữ liệu thành danh sách TimeSeriesDTO
            List<TimeSeriesDTO> timeSeriesData = invoiceData.stream()
                    .map(result -> new TimeSeriesDTO(
                            ((java.sql.Date) result[0]).toString(), // Ngày
                            BigDecimal.valueOf(((Number) result[1]).longValue()) // Số lượng hóa đơn, giữ longValue vì đây là số nguyên
                    ))
                    .collect(Collectors.toList());

            // Lấy tổng số đơn hàng
            Long totalOrders = reportRepository.getTotalOrdersOverTime(
                    request.getFromDate(), request.getToDate()
            );

            // Trả về ResponseEntity chứa ReportResponseDTO
            ReportResponseDTO response = ReportResponseDTO.builder()
                    .totalOrders(totalOrders != null ? totalOrders.intValue() : 0) // Kiểm tra null và gán mặc định
                    .timeSeriesData(timeSeriesData)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Xử lý lỗi nếu có
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}