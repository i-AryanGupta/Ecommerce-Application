package com.ea.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtExpiredException extends RuntimeException {
	
	private String message;

}
