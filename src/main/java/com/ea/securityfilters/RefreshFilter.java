package com.ea.securityfilters;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ea.exception.UsernameRoleNotFoundException;
import com.ea.serviceImpl.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RefreshFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String refreshToken = null;
		if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("rt")) 
                	refreshToken = cookie.getValue();
            }
		}
		
		if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            TokenExceptionHandler.tokenHandler(HttpStatus.UNAUTHORIZED.value(), "Token is Blocked", " Token is not available", response);
            return;
        }
		
		String username = null;
		String role = null;
		try {
		 username= jwtService.extractUsername(refreshToken);
		 role = jwtService.extractUserRole(refreshToken);
		
		}
		
		catch (Exception e) {
			// TODO: handle exception
		}
		
        if (username != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        	Collection<? extends GrantedAuthority> userRole = Collections.singletonList(new SimpleGrantedAuthority(role));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, userRole);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            filterChain.doFilter(request, response);
        }
        
        else
        	throw new UsernameRoleNotFoundException("username or role not found");
        
	
       
        
		
	}

}
