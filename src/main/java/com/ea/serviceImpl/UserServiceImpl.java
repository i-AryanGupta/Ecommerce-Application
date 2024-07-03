package com.ea.serviceImpl;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.ea.entity.AccessToken;
import com.ea.entity.Customer;
import com.ea.entity.RefreshToken;
import com.ea.entity.Seller;
import com.ea.entity.User;
import com.ea.enums.UserRole;
import com.ea.exception.InvalidTokenException;
import com.ea.exception.JwtNotGeneratedException;
import com.ea.exception.UserAlreadyExistException;
import com.ea.exception.UserNotFoundException;
import com.ea.mapper.UserMapper;
import com.ea.repository.AccessTokenRepository;
import com.ea.repository.RefreshTokenRepository;
import com.ea.repository.UserRepository;
import com.ea.requestto.AuthRequest;
import com.ea.requestto.OtpVerificationRequest;
import com.ea.requestto.UserRequest;
import com.ea.responseto.AuthResponse;
import com.ea.responseto.UserResponse;
import com.ea.service.UserService;
import com.ea.utility.MessageData;
import com.ea.utility.ResponseStructure;
import com.google.common.cache.Cache;
import jakarta.mail.MessagingException;

@Service
public class UserServiceImpl implements UserService {

	private final Cache<String, User> userCache;
	private final Cache<String, String> otpCache;
	private UserRepository userRepository;
	private UserMapper userMapper;
	private final Random random;
	
	private final MailService mailService;
	private JwtService jwtService;
	
	private final AuthenticationManager authenticationManager;
	
	private final AccessTokenRepository accessTokenRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	
	@Value("${application.jwt.access_expiry_seconds}")
	private long accessExpirySeconds;
	
	@Value("${application.jwt.refresh_expiry_seconds}")
	private long refreshExpirySeconds;
	
	@Value("${application.cookie.domain}")
	private String domain;
	
	@Value("${application.cookie.sameSite")
	private String sameSite;
	
	@Value("${application.cookie.secure}")
	private boolean secure;
	
	public UserServiceImpl(Cache<String, User> userCache, Cache<String, String> otpCache, UserRepository userRepository,
			UserMapper userMapper, Random random, MailService mailService, JwtService jwtService,AuthenticationManager authenticationManager
			,AccessTokenRepository accessTokenRepository,RefreshTokenRepository refreshTokenRepository) {
		super();
		this.userCache = userCache;
		this.otpCache = otpCache;
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.random = random;
		this.mailService = mailService;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.accessTokenRepository= accessTokenRepository;
		this.refreshTokenRepository = refreshTokenRepository;
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
		
		System.out.println(user);
		
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
			e.printStackTrace();
		}
		
			System.out.println(user);
		
		
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(new ResponseStructure<UserResponse>()
						.setData(userMapper.maptoUserResponse(user))
						.setMessage("Added in cahce")
						.setStatus(HttpStatus.ACCEPTED.value()));
	}
	
	
	// for extracting the name from the email id
	public String extractUsernameFromEmail(String email)	
	{
        return email.split("@gmail.com")[0];
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
			
			String name =extractUsernameFromEmail(otpVerificationRequest.getEmail());
			
			
			
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
			throw new IllegalArgumentException("Invalid otp entered");
		
	}

	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> loginUser(AuthRequest authRequest) {
		
		Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
		if(authenticate.isAuthenticated())
		{
			return userRepository.findByUsername(authRequest.getUsername()).map(user -> {
				
				HttpHeaders httpHeaders = new HttpHeaders();
				System.out.println(user.getUserRole().toString());
				grantAccessToken(httpHeaders, user);
				grantRefreshToken(httpHeaders, user);
				
				return ResponseEntity.ok().headers(httpHeaders)
						.body(new ResponseStructure<AuthResponse>()
						.setStatus(HttpStatus.OK.value())
						.setMessage("Login Successful")
						.setData(AuthResponse.builder().userId(user.getUserId()).username(user.getUsername()).accessExpiration(accessExpirySeconds)
								.refreshExpiration(refreshExpirySeconds).role(user.getUserRole().toString()).build()));
						
			}).orElseThrow(() -> new UserNotFoundException("User not found"));

		}
		else
			throw new BadCredentialsException("Authentication failed");
	

	}
	
	private String generateCookie(String name, String value, long maxAge)
	{
		return ResponseCookie.from(name, value).domain(domain).path("/").maxAge(maxAge).sameSite(sameSite)
				 .httpOnly(true).secure(secure).build().toString();
	}
	
	private void grantAccessToken(HttpHeaders httpHeaders, User user)
	{
		System.out.println(user.getUserRole().toString());
		String jwtToken = jwtService.createJwtToken(user.getUsername(), accessExpirySeconds*1000, user.getUserRole().toString());
		
		 AccessToken accessToken = AccessToken.builder().token(jwtToken)
		.expiration(LocalDateTime.now().plusSeconds(accessExpirySeconds)).user(user).build();
		 accessTokenRepository.save(accessToken);
		 httpHeaders.add(httpHeaders.SET_COOKIE, generateCookie("at", jwtToken, accessExpirySeconds));
	}
	
	private void grantRefreshToken(HttpHeaders httpHeaders, User user)
	{	System.out.println(user.getUserRole().toString());
		String jwtToken = jwtService.createJwtToken(user.getUsername(), refreshExpirySeconds*1000, user.getUserRole().toString());
		
		 RefreshToken refreshToken = RefreshToken.builder().token(jwtToken)
		.expiration(LocalDateTime.now().plusSeconds(refreshExpirySeconds)).user(user).build();
		 refreshTokenRepository.save(refreshToken);
		 httpHeaders.add(httpHeaders.SET_COOKIE,  generateCookie("rt", jwtToken, refreshExpirySeconds));
	}

	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> refreshLogin(String refreshToken) {
		
		if(refreshToken == null || refreshToken.isEmpty())
			throw new InvalidTokenException("Wrong token");
		
		
			String username = jwtService.extractUsername(refreshToken);
			String userRole = jwtService.extractUserRole(refreshToken);
			
			
			return userRepository.findByUsername(username).map(user -> {
				
				HttpHeaders httpHeaders = new HttpHeaders();
				grantAccessToken(httpHeaders, user);
				
				return ResponseEntity.ok().headers(httpHeaders)
						.body(new ResponseStructure<AuthResponse>()
						.setStatus(HttpStatus.OK.value())
						.setMessage("Refresh Login Successful")
						.setData(AuthResponse.builder().userId(user.getUserId()).username(user.getUsername()).accessExpiration(accessExpirySeconds)
								.refreshExpiration(refreshExpirySeconds - (LocalDateTime.now().getSecond())).role(user.getUserRole().toString()).build()));
						
			}).orElseThrow(() -> new UserNotFoundException("User not found"));
	}
	
// Here the issue that : when we deploy things going 	

}
