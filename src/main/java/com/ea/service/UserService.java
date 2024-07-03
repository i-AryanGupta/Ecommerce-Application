package com.ea.service;

import org.springframework.http.ResponseEntity;

import com.ea.enums.UserRole;
import com.ea.requestto.AuthRequest;
import com.ea.requestto.OtpVerificationRequest;
import com.ea.requestto.UserRequest;
import com.ea.responseto.AuthResponse;
import com.ea.responseto.UserResponse;
import com.ea.utility.ResponseStructure;

public interface UserService {

	ResponseEntity<ResponseStructure<UserResponse>> addUser(UserRequest userRequest, UserRole userRole);

	ResponseEntity<ResponseStructure<UserResponse>> verifyOtp(OtpVerificationRequest otpVerificationRequest);

	ResponseEntity<ResponseStructure<AuthResponse>>  loginUser(AuthRequest authRequest);

	ResponseEntity<ResponseStructure<AuthResponse>> refreshLogin(String refreshToken);

}
