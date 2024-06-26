package com.ea.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ea.enums.UserRole;
import com.ea.requestto.OtpVerificationRequest;
import com.ea.requestto.UserRequest;
import com.ea.responseto.UserResponse;
import com.ea.service.UserService;
import com.ea.utility.ResponseStructure;

@RestController
@RequestMapping("/api/version1")
public class UserController {
	
	
	private UserService userService;
	
	
	// this is more secure , @autowired used field injection which sometime inject null values.
	public UserController(UserService userService) {
		this.userService = userService;
	}



	@PostMapping("/sellers")
	public ResponseEntity<ResponseStructure<UserResponse>> addSeller(@RequestBody UserRequest userRequest)
	{
		return userService.addUser(userRequest, UserRole.SELLER);
	}
	
	@PostMapping("/customers")
	public ResponseEntity<ResponseStructure<UserResponse>> addCustomer(@RequestBody UserRequest userRequest)
	{
		return userService.addUser(userRequest, UserRole.CUSTOMER);
	}
	
	@PostMapping("/verify")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOtp(@RequestBody OtpVerificationRequest otpVerificationRequest)
	{
		return userService.verifyOtp(otpVerificationRequest);
	}

}
