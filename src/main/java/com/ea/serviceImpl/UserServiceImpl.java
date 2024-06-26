package com.ea.serviceImpl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ea.config.UtilityBeanConfig;
import com.ea.entity.Customer;
import com.ea.entity.Seller;
import com.ea.entity.User;
import com.ea.enums.UserRole;
import com.ea.exception.UserAlreadyExistException;
import com.ea.mapper.UserMapper;
import com.ea.repository.UserRepository;
import com.ea.requestto.OtpVerificationRequest;
import com.ea.requestto.UserRequest;
import com.ea.responseto.UserResponse;
import com.ea.service.UserService;
import com.ea.utility.MessageData;
import com.ea.utility.ResponseStructure;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;

@Service

public class UserServiceImpl implements UserService {

	private final Cache<String, User> userCache;
	private final Cache<String, String> otpCache;
	private UserRepository userRepository;
	private UserMapper userMapper;
	private final Random random;
	
	private final MailService mailService;
	
	
	

	public UserServiceImpl(Cache<String, User> userCache, Cache<String, String> otpCache, UserRepository userRepository,
			UserMapper userMapper, Random random, MailService mailService) {
		super();
		this.userCache = userCache;
		this.otpCache = otpCache;
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.random = random;
		this.mailService = mailService;
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> addUser(UserRequest userRequest, UserRole userRole) {
		
		 boolean existsUser = userRepository.existsByEmail(userRequest.getEmail());
		 
		 if(existsUser)
			 throw new UserAlreadyExistException("Email :" + userRequest.getEmail() +" is already exist");
	
		User user = null;
		
		switch (userRole) {
		case SELLER ->user = new Seller();
		case CUSTOMER ->user = new Customer();
		}
		
		if(user != null)
		{
			user.setUserRole(userRole);
			user = userMapper.maptoUserRequest(userRequest, user);
		}
		
		int number = random.nextInt(100000, 999999);
		String numberStr = String.valueOf(number);
		
		userCache.put(user.getEmail(),user);
		otpCache.put(user.getEmail(),numberStr);
		
		MessageData messageData = new MessageData();
		messageData.setTo(user.getEmail());
		messageData.setSubject("Your OTP Code");
		messageData.setText("Your OTP code is: " + numberStr);
		messageData.setSentDate(new Date(System.currentTimeMillis()));
		
		
		
			try {
				mailService.sendMail(messageData);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		
		
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(new ResponseStructure<UserResponse>()
						.setData(userMapper.maptoUserResponse(user))
						.setMessage("Added in cahce")
						.setStatus(HttpStatus.ACCEPTED.value()));
	}
	
	public String extractUsername(String email)
	{
		 if (email == null || !email.contains("@gmail.com")) {
	            throw new IllegalArgumentException("Invalid Gmail address");
	        }
	        
	        int atIndex = email.indexOf("@");
	        if (atIndex == -1) {
	            throw new IllegalArgumentException("Invalid email address format");
	        }
	        
	        return email.substring(0, atIndex);
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOtp(OtpVerificationRequest otpVerificationRequest) {
		
		User user = userCache.getIfPresent(otpVerificationRequest.getEmail());
		String otp = otpCache.getIfPresent(otpVerificationRequest.getEmail());
		
		
		
		
		if(user == null || otp == null)
		{
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseStructure<UserResponse>()
							.setMessage("Invalid OTP or User not found")
							.setStatus(HttpStatus.NOT_FOUND.value()));
		}
		
		if(otpVerificationRequest.getOtp().equals(otp))
		{
			
			String name =extractUsername(otpVerificationRequest.getEmail());
			
			user.setUsername(name);
			user.setEmailVerified(true);
			userRepository.save(user);
			
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(new ResponseStructure<UserResponse>()
							.setMessage("User Saved")
							.setStatus(HttpStatus.CREATED.value())
							.setData(userMapper.maptoUserResponse(user)));
			
		}
		
		else
		{
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ResponseStructure<UserResponse>()
							.setMessage("Invalid otp")
							.setStatus(HttpStatus.NOT_FOUND.value()));
		}
	}

}
