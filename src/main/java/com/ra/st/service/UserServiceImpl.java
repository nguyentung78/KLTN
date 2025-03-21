package com.ra.st.service;

import com.ra.st.model.dto.*;
import com.ra.st.model.entity.*;
import com.ra.st.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private WishListRepository wishListRepository;
    //=======================CART=========================

    @Override
    public List<CartItemResponseDTO> getCartItems() {
        Users currentUser = getCurrentUser();
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUser(currentUser);

        return cartItems.stream().map(item -> new CartItemResponseDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getProductName(),
                item.getProduct().getImage(),
                item.getProduct().getUnitPrice(),
                item.getOrderQuantity()
        )).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> addToCart(CartItemRequestDTO request) {
        Users currentUser = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (request.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Số lượng phải lớn hơn 0!");
        }

        ShoppingCart cartItem = shoppingCartRepository.findByUserAndProduct(currentUser, product)
                .orElse(new ShoppingCart(null, currentUser, product, 0));

        cartItem.setOrderQuantity(cartItem.getOrderQuantity() + request.getQuantity());
        shoppingCartRepository.save(cartItem);

        return ResponseEntity.ok("Thêm sản phẩm vào giỏ hàng thành công!");
    }

    @Override
    public ResponseEntity<?> updateCartItem(Long cartItemId, Integer quantity) {
        ShoppingCart cartItem = shoppingCartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm trong giỏ hàng không tồn tại!"));

        if (quantity <= 0) {
            shoppingCartRepository.delete(cartItem);
            return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng.");
        }

        cartItem.setOrderQuantity(quantity);
        shoppingCartRepository.save(cartItem);
        return ResponseEntity.ok("Cập nhật số lượng thành công!");
    }

    @Override
    public ResponseEntity<?> removeCartItem(Long cartItemId) {
        shoppingCartRepository.deleteById(cartItemId);
        return ResponseEntity.ok("Xóa sản phẩm khỏi giỏ hàng thành công!");
    }

    @Override
    @Transactional
    public ResponseEntity<?> clearCart() {
        Users currentUser = getCurrentUser();
        shoppingCartRepository.deleteByUser(currentUser);
        return ResponseEntity.ok("Xóa toàn bộ giỏ hàng thành công!");
    }

    @Override
    @Transactional
    public ResponseEntity<?> checkout(CheckoutRequestDTO request) {
        Users currentUser = getCurrentUser();
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUser(currentUser);

        // ✅ Kiểm tra giỏ hàng có rỗng không
        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống!");
        }

        Address selectedAddress = null;

        // ✅ Nếu user nhập địa chỉ trực tiếp, kiểm tra dữ liệu đầu vào
        if (request.getReceiveAddress() != null && !request.getReceiveAddress().trim().isEmpty()
                && request.getReceivePhone() != null && !request.getReceivePhone().trim().isEmpty()
                && request.getReceiveName() != null && !request.getReceiveName().trim().isEmpty()) {

            selectedAddress = new Address(null, currentUser, request.getReceiveAddress(),
                    request.getReceivePhone(), request.getReceiveName());
        }

        // ✅ Nếu user chọn một địa chỉ từ danh sách
        else if (request.getAddressId() != null) {
            selectedAddress = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));
        }

        // ✅ Nếu user không nhập địa chỉ và không chọn địa chỉ, báo lỗi
        if (selectedAddress == null) {
            return ResponseEntity.badRequest().body("Vui lòng nhập địa chỉ hoặc chọn địa chỉ giao hàng!");
        }

        // ✅ Tạo đơn hàng mới
        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setSerialNumber(UUID.randomUUID().toString());
        newOrder.setTotalPrice(cartItems.stream()
                .map(item -> item.getProduct().getUnitPrice().multiply(BigDecimal.valueOf(item.getOrderQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        newOrder.setStatus(Order.OrderStatus.WAITING);
        newOrder.setCreatedAt(new Date());

        // ✅ Lưu thông tin địa chỉ giao hàng vào đơn hàng
        newOrder.setReceiveAddress(selectedAddress.getFullAddress());
        newOrder.setReceivePhone(selectedAddress.getPhone());
        newOrder.setReceiveName(selectedAddress.getReceiveName());

        orderRepository.save(newOrder);

        // ✅ Lưu chi tiết đơn hàng
        for (ShoppingCart item : cartItems) {
            OrderDetailKey orderDetailKey = new OrderDetailKey(newOrder.getId(), item.getProduct().getId());

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setId(orderDetailKey);
            orderDetail.setOrder(newOrder);
            orderDetail.setProduct(item.getProduct());
            orderDetail.setOrderQuantity(item.getOrderQuantity());
            orderDetail.setUnitPrice(item.getProduct().getUnitPrice());
            orderDetail.setName(item.getProduct().getProductName());

            orderDetailRepository.save(orderDetail);
        }

        // ✅ Xóa giỏ hàng sau khi đặt hàng
        shoppingCartRepository.deleteByUser(currentUser);

        return ResponseEntity.ok("Đặt hàng thành công!");
    }

    //=======================ACCOUNT=========================

    private Users getCurrentUser() {
        return userRepository.findUserByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Override
    public UserProfileResponseDTO getUserProfile() {
        Users user = getCurrentUser();
        return new UserProfileResponseDTO(
                user.getUsername(), user.getEmail(), user.getFullname(),
                user.getPhone(), user.getAddress(), user.getAvatar()
        );
    }

    @Override
    public ResponseEntity<?> updateUserProfile(UserProfileUpdateDTO request) {
        Users user = getCurrentUser();
        boolean isUpdated = false; // Kiểm tra xem có gì được cập nhật không

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            if (userRepository.findUserByUsername(request.getUsername()) != null) {
                return ResponseEntity.badRequest().body("Username đã tồn tại!");
            }
            user.setUsername(request.getUsername());
            isUpdated = true;
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.findUsersByEmail(request.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email đã tồn tại!");
            }
            user.setEmail(request.getEmail());
            isUpdated = true;
        }

        if (request.getFullname() != null && !request.getFullname().trim().isEmpty()) {
            user.setFullname(request.getFullname());
            isUpdated = true;
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone());
            isUpdated = true;
        }

        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            user.setAddress(request.getAddress());
            isUpdated = true;
        }

        // ✅ Nếu có avatar, upload lên Cloudinary
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String imageUrl = uploadService.uploadFile(request.getAvatar());
            user.setAvatar(imageUrl);
            isUpdated = true;
        }

        if (!isUpdated) {
            return ResponseEntity.badRequest().body("Không có dữ liệu nào để cập nhật!");
        }

        userRepository.save(user);
        return ResponseEntity.ok("Cập nhật thông tin thành công!");
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePasswordRequestDTO request) {
        Users user = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không chính xác!");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu mới không khớp!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    @Override
    public ResponseEntity<?> addAddress(AddressDTO request) {
        Users user = getCurrentUser();

        // ✅ Kiểm tra nếu thiếu trường dữ liệu nào thì báo lỗi chi tiết
        if (request.getFullAddress() == null || request.getFullAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập địa chỉ đầy đủ!");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập số điện thoại!");
        }
        if (request.getReceiveName() == null || request.getReceiveName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập tên người nhận!");
        }

        Address address = new Address(null, user, request.getFullAddress(), request.getPhone(), request.getReceiveName());
        addressRepository.save(address);
        return ResponseEntity.ok("Thêm địa chỉ thành công!");
    }


    @Override
    public ResponseEntity<?> deleteAddress(Long addressId) {
        addressRepository.deleteById(addressId);
        return ResponseEntity.ok("Xóa địa chỉ thành công!");
    }

    @Override
    public List<AddressDTO> getUserAddresses() {
        Users user = getCurrentUser();
        return addressRepository.findByUser(user).stream()
                .map(a -> new AddressDTO(a.getId(), a.getFullAddress(), a.getPhone(), a.getReceiveName()))
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại!"));
        return new AddressDTO(address.getId(), address.getFullAddress(), address.getPhone(), address.getReceiveName());
    }

    //=======================HISTORY=========================

    // ✅ Lấy toàn bộ lịch sử mua hàng của user
    @Override
    public List<OrderResponseDTO> getOrderHistory() {
        Users currentUser = getCurrentUser();
        List<Order> orders = orderRepository.findByUser(currentUser);

        return orders.stream().map(order -> new OrderResponseDTO(
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
        )).collect(Collectors.toList());

    }

    // ✅ Lấy chi tiết một đơn hàng theo serialNumber
    @Override
    public OrderDetailResponseDTO getOrderDetail(String serialNumber) {
        Order order = orderRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        List<OrderItemDTO> items = orderDetailRepository.findByOrder(order).stream()
                .map(item -> new OrderItemDTO(
                        item.getProduct().getId(),
                        item.getProduct().getProductName(),
                        item.getUnitPrice(),
                        item.getOrderQuantity()
                )).collect(Collectors.toList());

        return new OrderDetailResponseDTO(
                order.getId(),
                order.getSerialNumber(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getReceiveName(),
                order.getReceivePhone(),
                order.getReceiveAddress(),
                order.getCreatedAt(),
                items
        );
    }

    // ✅ Lọc lịch sử đơn hàng theo trạng thái
    @Override
    public List<OrderResponseDTO> getOrdersByStatus(String orderStatus) {
        Users currentUser = getCurrentUser();
        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.valueOf(orderStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái đơn hàng không hợp lệ!");
        }

        List<Order> orders = orderRepository.findByUserAndStatus(currentUser, status);
        return orders.stream().map(order -> new OrderResponseDTO(
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
        )).collect(Collectors.toList());

    }

    // ✅ Hủy đơn hàng nếu nó đang ở trạng thái `WAITING`
    @Override
    @Transactional
    public ResponseEntity<?> cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        if (!order.getStatus().equals(Order.OrderStatus.WAITING)) {
            return ResponseEntity.badRequest().body("Chỉ có thể hủy đơn hàng khi đang ở trạng thái chờ xác nhận!");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        return ResponseEntity.ok("Hủy đơn hàng thành công!");
    }

    //=======================WISHLIST=========================

    // ✅ Thêm sản phẩm vào danh sách yêu thích
    @Override
    public ResponseEntity<?> addToWishList(Long productId) {
        Users currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (wishListRepository.findByUserAndProductId(currentUser, productId).isPresent()) {
            return ResponseEntity.badRequest().body("Sản phẩm đã có trong danh sách yêu thích!");
        }

        WishList wishList = new WishList();
        wishList.setUser(currentUser);
        wishList.setProduct(product);
        wishListRepository.save(wishList);

        return ResponseEntity.ok("Thêm vào danh sách yêu thích thành công!");
    }

    // ✅ Xóa sản phẩm khỏi danh sách yêu thích
    @Override
    @Transactional
    public ResponseEntity<?> removeFromWishList(Long productId) {
        Users currentUser = getCurrentUser();
        if (!wishListRepository.findByUserAndProductId(currentUser, productId).isPresent()) {
            return ResponseEntity.badRequest().body("Sản phẩm không có trong danh sách yêu thích!");
        }

        wishListRepository.deleteByUserAndProductId(currentUser, productId);
        return ResponseEntity.ok("Xóa sản phẩm khỏi danh sách yêu thích thành công!");
    }

    // ✅ Lấy danh sách sản phẩm yêu thích của người dùng
    @Override
    public List<WishListDTO> getUserWishList() {
        Users currentUser = getCurrentUser();
        List<WishList> wishLists = wishListRepository.findByUser(currentUser);

        return wishLists.stream().map(wishList -> new WishListDTO(
                wishList.getProduct().getId(),
                wishList.getProduct().getProductName(),
                wishList.getProduct().getImage(),
                wishList.getProduct().getDescription(),
                wishList.getProduct().getUnitPrice().doubleValue()
        )).collect(Collectors.toList());
    }
}
