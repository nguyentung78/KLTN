package com.ra.st.controller;

import com.ra.st.model.dto.UserRegisterRequestDTO;
import com.ra.st.model.dto.UserRegisterResponseDTO;
import com.ra.st.model.dto.UserLoginRequestDTO;
import com.ra.st.model.dto.UserLoginResponse;
import com.ra.st.service.AuthService;
import com.ra.st.service.LogoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDTO requestDTO) {
        try {
            UserLoginResponse userLoginResponse = authService.login(requestDTO);
            return new ResponseEntity<>(userLoginResponse, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            // Xử lý cả trường hợp username không tồn tại hoặc mật khẩu sai
            return new ResponseEntity<>("Tên đăng nhập hoặc mật khẩu không đúng!", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>("Đã xảy ra lỗi khi đăng nhập!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("fullname") String fullname,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        UserRegisterRequestDTO requestDTO = UserRegisterRequestDTO.builder()
                .username(username)
                .email(email)
                .fullname(fullname)
                .password(password)
                .phone(phone)
                .address(address)
                .avatar(avatar)
                .build();

        try {
            UserRegisterResponseDTO response = authService.register(requestDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Autowired
    private LogoutService logoutService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token không hợp lệ!");
        }

        String jwt = token.substring(7); // Cắt "Bearer " khỏi token
        logoutService.blacklistToken(jwt);

        // Xóa thông tin authentication khỏi SecurityContext
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Đăng xuất thành công!");
    }
}