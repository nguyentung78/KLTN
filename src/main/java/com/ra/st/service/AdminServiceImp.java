package com.ra.st.service;

import com.ra.st.model.dto.*;
import com.ra.st.model.entity.*;
import com.ra.st.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminServiceImp implements AdminService {
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
    private ReportRepository reportRepository;
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

    // Tìm kiếm người dùng theo username
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


    // Xóa quyền của người dùng
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


    // Khóa / Mở khóa tài khoản
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
                new CategoryResponseDTO(category.getId(), category.getCategoryName(), category.getDescription(), category.getStatus()));

        return ResponseEntity.ok(categoryResponse);
    }

    @Override
    public ResponseEntity<?> getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
        return ResponseEntity.ok(new CategoryResponseDTO(category.getId(), category.getCategoryName(), category.getDescription(), category.getStatus()));
    }

    @Override
    public ResponseEntity<?> createCategory(CategoryRequestDTO categoryRequest) {
        if (categoryRepository.existsByCategoryName(categoryRequest.getCategoryName())) {
            return ResponseEntity.badRequest().body("Danh mục đã tồn tại!");
        }

        Category newCategory = new Category();
        newCategory.setCategoryName(categoryRequest.getCategoryName());
        newCategory.setDescription(categoryRequest.getDescription());

        categoryRepository.save(newCategory);
        return ResponseEntity.ok("Thêm danh mục thành công!");
    }

    @Override
    public ResponseEntity<?> updateCategory(Long categoryId, CategoryRequestDTO categoryRequest) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

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
        return ResponseEntity.ok("Cập nhật danh mục thành công!");
    }


    @Override
    public ResponseEntity<?> deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        categoryRepository.delete(category);
        return ResponseEntity.ok("Xóa danh mục thành công!");
    }

    @Override
    public ResponseEntity<?> searchCategories(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories;

        if (keyword == null || keyword.trim().isEmpty()) {
            categories = categoryRepository.findAll(pageable); // Nếu không có keyword, trả về tất cả danh mục
        } else {
            categories = categoryRepository.findByCategoryNameContainingIgnoreCase(keyword, pageable);
        }

        Page<CategoryResponseDTO> response = categories.map(category ->
                new CategoryResponseDTO(category.getId(), category.getCategoryName(), category.getDescription(), category.getStatus()));

        return ResponseEntity.ok(response);
    }

    //=======================PRODUCTS=========================

    @Override
    public ResponseEntity<?> getAllProducts(int page, int size, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Product> products = productRepository.findAll(pageable);

        Page<ProductResponseDTO> productResponse = products.map(product ->
                new ProductResponseDTO(product.getId(), product.getProductName(), product.getDescription(),
                        product.getUnitPrice(), product.getStockQuantity(), product.getImage(),
                        product.getCategory().getId(), product.getFeatured()));

        return ResponseEntity.ok(productResponse);
    }


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

    @Override
    public ResponseEntity<?> createProduct(ProductRequestDTO productRequest) {
        if (productRepository.existsByProductName(productRequest.getProductName())) {
            return ResponseEntity.badRequest().body("Sản phẩm đã tồn tại!");
        }

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        String sku = (productRequest.getSku() != null && !productRequest.getSku().isEmpty())
                ? productRequest.getSku()
                : "SKU-" + UUID.randomUUID().toString().substring(0, 8);

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
        newProduct.setSku(sku);
        newProduct.setProductName(productRequest.getProductName());
        newProduct.setDescription(productRequest.getDescription());
        newProduct.setUnitPrice(productRequest.getUnitPrice());
        newProduct.setStockQuantity(productRequest.getStockQuantity());
        newProduct.setImage(imageUrl);
        newProduct.setCategory(category);
        newProduct.setFeatured(false);

        productRepository.save(newProduct);

        return ResponseEntity.ok("Thêm sản phẩm thành công!");
    }


    @Override
    public ResponseEntity<?> updateProduct(Long productId, ProductRequestDTO productRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // Cập nhật nếu có giá trị, nếu không giữ nguyên
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

        // Nếu có ảnh mới, cập nhật
        if (productRequest.getImage() != null && !productRequest.getImage().isEmpty()) {
            try {
                String imageUrl = uploadService.uploadFile(productRequest.getImage());
                product.setImage(imageUrl);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload ảnh: " + e.getMessage());
            }
        }

        productRepository.save(product);
        return ResponseEntity.ok("Cập nhật sản phẩm thành công!");
    }

    @Override
    public ResponseEntity<?> deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        productRepository.delete(product);
        return ResponseEntity.ok("Xóa sản phẩm thành công!");
    }

    //=======================ORDERS=========================
    // Lấy danh sách tất cả đơn hàng (phân trang, sắp xếp)
    @Override
    public Page<OrderResponseDTO> getAllOrders(int page, int size, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Order> orders = orderRepository.findAll(pageable);

        return orders.map(or -> new OrderResponseDTO(
                or.getId(),
                or.getSerialNumber(),
                or.getUser().getUsername(),
                or.getTotalPrice(),
                or.getStatus(),
                or.getReceiveName(),
                or.getReceiveAddress(),
                or.getReceivePhone(),
                or.getCreatedAt(),
                or.getReceivedAt()
        ));
    }

    // Lấy danh sách đơn hàng theo trạng thái
    @Override
    public Page<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus orderStatus, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByStatus(orderStatus, pageable);

        return orders.map(or -> new OrderResponseDTO(
                or.getId(),
                or.getSerialNumber(),
                or.getUser().getUsername(),
                or.getTotalPrice(),
                or.getStatus(),
                or.getReceiveName(),
                or.getReceiveAddress(),
                or.getReceivePhone(),
                or.getCreatedAt(),
                or.getReceivedAt()
        ));
    }

    // Lấy chi tiết đơn hàng theo ID
    @Override
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        return new OrderResponseDTO(
                order.getId(),
                order.getSerialNumber(),
                order.getUser().getUsername(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getReceiveName(),
                order.getReceiveAddress(),
                order.getReceivePhone(),
                order.getCreatedAt(),
                order.getReceivedAt()
        );
    }

    // Cập nhật trạng thái đơn hàng
    @Override
    public ResponseEntity<?> updateOrderStatus(Long orderId, Order.OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        order.setStatus(orderStatus);
        orderRepository.save(order);

        return ResponseEntity.ok("Cập nhật trạng thái đơn hàng thành công!");
    }

    // ====================== REPORTS & STATISTICS ======================
    // Doanh thu bán hàng theo thời gian
    @Override
    public ResponseEntity<ReportResponseDTO> getSalesRevenueOverTime(ReportRequestDTO request) {
        BigDecimal totalRevenue = reportRepository.getSalesRevenueOverTime(request.getFromDate(), request.getToDate());
        Long totalOrders = reportRepository.getTotalOrdersOverTime(request.getFromDate(), request.getToDate());

        return ResponseEntity.ok(
                ReportResponseDTO.builder()
                        .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                        .totalOrders(totalOrders != null ? totalOrders.intValue() : 0)
                        .build()
        );
    }

    // Sản phẩm bán chạy nhất
    @Override
    public ResponseEntity<ReportResponseDTO> getBestSellerProducts() {
        List<String> topProducts = reportRepository.getBestSellerProducts();
        return ResponseEntity.ok(ReportResponseDTO.builder().topItems(topProducts).build());
    }

    // Sản phẩm yêu thích nhất
    @Override
    public ResponseEntity<ReportResponseDTO> getMostLikedProducts() {
        List<String> mostLikedProducts = reportRepository.getMostLikedProducts();
        return ResponseEntity.ok(ReportResponseDTO.builder().topItems(mostLikedProducts).build());
    }

    // Doanh thu theo danh mục
    @Override
    public ResponseEntity<?> getRevenueByCategory() {
        return ResponseEntity.ok(reportRepository.getRevenueByCategory());
    }

    // Khách hàng chi tiêu nhiều nhất
    @Override
    public ResponseEntity<ReportResponseDTO> getTopSpendingCustomers() {
        List<Object[]> topCustomers = reportRepository.getTopSpendingCustomers();
        List<String> topCustomerNames = topCustomers.stream().map(obj -> (String) obj[0]).collect(Collectors.toList());
        BigDecimal totalRevenue = topCustomers.stream().map(obj -> (BigDecimal) obj[1]).reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(
                ReportResponseDTO.builder()
                        .totalRevenue(totalRevenue)
                        .topItems(topCustomerNames)
                        .build()
        );
    }

    // Tài khoản mới trong tháng
    @Override
    public ResponseEntity<ReportResponseDTO> getNewAccountsThisMonth() {
        Long totalNewAccounts = reportRepository.getNewAccountsThisMonth();
        List<String> newAccountsList = reportRepository.getNewAccountListThisMonth();

        return ResponseEntity.ok(
                ReportResponseDTO.builder()
                        .totalOrders(totalNewAccounts.intValue())
                        .topItems(newAccountsList)
                        .build()
        );
    }

    // Số lượng hóa đơn theo thời gian
    @Override
    public ResponseEntity<ReportResponseDTO> getInvoicesOverTime(ReportRequestDTO request) {
        Long totalOrders = reportRepository.getTotalOrdersOverTime(request.getFromDate(), request.getToDate());

        return ResponseEntity.ok(
                ReportResponseDTO.builder()
                        .totalOrders(totalOrders.intValue())
                        .build()
        );
    }

}
