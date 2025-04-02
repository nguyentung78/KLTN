package com.ra.st.controller;

import com.ra.st.model.dto.*;
import com.ra.st.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    //=======================CART=========================

    // Lấy danh sách sản phẩm trong giỏ hàng
    @GetMapping("/cart/list")
    public ResponseEntity<List<CartItemResponseDTO>> getCartItems() {
        return ResponseEntity.ok(userService.getCartItems());
    }

    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartItemRequestDTO request) {
        return userService.addToCart(request);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @PutMapping("/cart/items/{cartItemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long cartItemId, @RequestBody CartItemRequestDTO request) {
        return userService.updateCartItem(cartItemId, request.getQuantity());
    }

    // Xóa 1 sản phẩm khỏi giỏ hàng
    @DeleteMapping("/cart/items/{cartItemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long cartItemId) {
        return userService.removeCartItem(cartItemId);
    }

    // Xóa toàn bộ giỏ hàng
    @DeleteMapping("/cart/clear")
    public ResponseEntity<?> clearCart() {
        return userService.clearCart();
    }

    // Đặt hàng từ giỏ hàng
    @PostMapping("/cart/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO request) {
        return userService.checkout(request);
    }

    //=======================ACCOUNT=========================

    // Lấy thông tin tài khoản người dùng
    @GetMapping("/account")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    // Cập nhật thông tin cá nhân
    @PutMapping(value = "/account", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updateUserProfile(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "fullname", required = false) String fullname,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        UserProfileUpdateDTO request = new UserProfileUpdateDTO();
        request.setUsername(username);
        request.setEmail(email);
        request.setFullname(fullname);
        request.setPhone(phone);
        request.setAddress(address);
        request.setAvatar(avatar);

        return userService.updateUserProfile(request);
    }

    // Đổi mật khẩu
    @PutMapping("/account/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequestDTO request) {
        return userService.changePassword(request);
    }

    // Thêm địa chỉ mới
    @PostMapping("/account/addresses")
    public ResponseEntity<?> addAddress(@RequestBody AddressDTO request) {
        return userService.addAddress(request);
    }

    // Xóa địa chỉ theo ID
    @DeleteMapping("/account/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        return userService.deleteAddress(addressId);
    }

    // Lấy danh sách địa chỉ của user
    @GetMapping("/account/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses() {
        return ResponseEntity.ok(userService.getUserAddresses());
    }

    // Lấy chi tiết địa chỉ theo ID
    @GetMapping("/account/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {
        return ResponseEntity.ok(userService.getAddressById(addressId));
    }

    // ======================= HISTORY ==========================

    // Lấy toàn bộ lịch sử mua hàng của user
    @GetMapping("/history")
    public ResponseEntity<List<OrderResponseDTO>> getOrderHistory() {
        return ResponseEntity.ok(userService.getOrderHistory());
    }

    // Lấy chi tiết một đơn hàng theo serialNumber
    @GetMapping("/history/{serialNumber}")
    public ResponseEntity<OrderDetailResponseDTO> getOrderDetail(@PathVariable String serialNumber) {
        return ResponseEntity.ok(userService.getOrderDetail(serialNumber));
    }

    // Lọc lịch sử đơn hàng theo trạng thái
    @GetMapping("/history/status/{orderStatus}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@PathVariable String orderStatus) {
        return ResponseEntity.ok(userService.getOrdersByStatus(orderStatus));
    }

    // Hủy đơn hàng khi còn ở trạng thái `WAITING`
    @PutMapping("/history/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        return userService.cancelOrder(orderId);
    }

    //=======================WISHLIST=========================

    // Lấy danh sách sản phẩm yêu thích
    @GetMapping("/wish-list")
    public ResponseEntity<List<WishListDTO>> getWishList() {
        return ResponseEntity.ok(userService.getUserWishList());
    }

    // Thêm sản phẩm vào danh sách yêu thích
    @PostMapping("/wish-list/{productId}")
    public ResponseEntity<?> addToWishList(@PathVariable Long productId) {
        return userService.addToWishList(productId);
    }

    // Xóa sản phẩm khỏi danh sách yêu thích
    @DeleteMapping("/wish-list/{productId}")
    public ResponseEntity<?> removeFromWishList(@PathVariable Long productId) {
        return userService.removeFromWishList(productId);
    }
}

