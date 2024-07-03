package com.ea.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsernameRoleNotFoundException extends RuntimeException {
	
	private String message;

}
