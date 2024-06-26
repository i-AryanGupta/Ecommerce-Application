package com.ea.mapper;

import org.springframework.stereotype.Component;

import com.ea.entity.User;
import com.ea.requestto.UserRequest;
import com.ea.responseto.UserResponse;

@Component
public class UserMapper {
	
	public User maptoUserRequest(UserRequest userRequest, User user)
	{
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());

		
		return user;
	}
	
	public UserResponse maptoUserResponse(User user)
	{
		return UserResponse.builder()
				.userId(user.getUserId())
				.username(user.getUsername())
				.email(user.getEmail())
				.isDeleted(user.isDeleted())
				.userRole(user.getUserRole())
				.isEmailVerified(user.isEmailVerified())
				.build();
	}

}
