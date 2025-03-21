package com.ra.st.service;

import com.ra.st.model.dto.*;
import com.ra.st.model.entity.Role;
import com.ra.st.model.entity.Users;
import com.ra.st.repository.RoleRepository;
import com.ra.st.repository.UserRepository;
import com.ra.st.security.UserPrinciple;
import com.ra.st.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImp implements AuthService {
    @Autowired
    private AuthenticationProvider authenticationProvider;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UploadService uploadService;
    @Override
    public UserLoginResponse login(UserLoginRequestDTO userLoginRequestDTO) {
        Authentication authentication;
        authentication = authenticationProvider
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                userLoginRequestDTO.getUsername(),
                                userLoginRequestDTO.getPassword())
                );
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();

        Set<RoleResponseDTO> roleResponseDTOS = userPrinciple.getUser().getRoles()
                .stream()
                .map(role -> new RoleResponseDTO(role.getRoleName().name()))
                .collect(Collectors.toSet());

        return UserLoginResponse.builder()
                .username(userPrinciple.getUsername())
                .typeToken("Bearer Token")
                .accessToken(jwtProvider.generateToken(userPrinciple))
                .roles(roleResponseDTOS)
                .build();
    }

    @Override
    public UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterRequestDTO) {
        if (userRepository.findUserByUsername(userRegisterRequestDTO.getUsername()) != null) {
            throw new RuntimeException("Username đã tồn tại!");
        }
        if (userRepository.findUsersByEmail(userRegisterRequestDTO.getEmail()) != null) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findRoleByRoleName(Role.RoleName.USER);
        roles.add(role);

        MultipartFile avatarFile = userRegisterRequestDTO.getAvatar();
        String avatarUrl = (avatarFile != null && !avatarFile.isEmpty())
                ? uploadService.uploadFile(avatarFile)
                : "https://res.cloudinary.com/default-avatar.png";

        Users newUser = Users.builder()
                .username(userRegisterRequestDTO.getUsername())
                .email(userRegisterRequestDTO.getEmail())
                .fullname(userRegisterRequestDTO.getFullname())
                .password(new BCryptPasswordEncoder().encode(userRegisterRequestDTO.getPassword()))
                .status(true)
                .roles(roles)
                .phone(userRegisterRequestDTO.getPhone())
                .address(userRegisterRequestDTO.getAddress())
                .avatar(avatarUrl)
                .build();

        Users savedUser = userRepository.save(newUser);

        Set<RoleResponseDTO> roleDTOs = savedUser.getRoles().stream()
                .map(r -> new RoleResponseDTO(r.getRoleName().name()))
                .collect(Collectors.toSet());

        return UserRegisterResponseDTO.builder()
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullname(savedUser.getFullname())
                .phone(savedUser.getPhone())
                .address(savedUser.getAddress())
                .avatar(savedUser.getAvatar())
                .roles(roleDTOs)
                .build();
    }

}