package com.ra.st.service;

import com.ra.st.model.dto.UserLoginRequestDTO;
import com.ra.st.model.dto.UserLoginResponse;
import com.ra.st.model.dto.UserRegisterRequestDTO;
import com.ra.st.model.dto.UserRegisterResponseDTO;

public interface AuthService {
    UserLoginResponse login(UserLoginRequestDTO userLoginRequestDTO);
    UserRegisterResponseDTO register(UserRegisterRequestDTO userRegisterDTO);

}