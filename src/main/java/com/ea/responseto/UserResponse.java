package com.ea.responseto;


import com.ea.enums.UserRole;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
	
	private int userId;
	private String username;
	private String email;
	private boolean isEmailVerified;
	private boolean isDeleted;
	@Enumerated(EnumType.STRING)
	private UserRole userRole;

}
