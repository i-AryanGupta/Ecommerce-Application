package com.ea.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JwtNotGeneratedException extends RuntimeException {
private String message;
}
