package com.ea.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ea.securityfilters.AuthFilter;
import com.ea.securityfilters.LoginFilter;
import com.ea.securityfilters.RefreshFilter;
import com.ea.serviceImpl.JwtService;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
	
	private final JwtService jwtService;

	@Bean
	PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder(12);
	}
	
	@Order(1)
	@Bean
	SecurityFilterChain loginFilterChain(HttpSecurity httpSecurity) throws Exception
	{		
		return httpSecurity.csrf(csrf -> csrf.disable())
				.securityMatchers(match -> match.requestMatchers("/api/version1/login/**", "/api/version1/sellers/registers**", "/api/version1/customers/registers**"))
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(new LoginFilter(), UsernamePasswordAuthenticationFilter.class)
				.build();
	}
	
	@Order(2)
	@Bean
	SecurityFilterChain refreshFilterChain(HttpSecurity httpSecurity) throws Exception
	{
		return httpSecurity.csrf(csrf -> csrf.disable())
                .securityMatchers(match -> match.requestMatchers("/api/version1/refresh/**"))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new RefreshFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();        
	}
	
	
	@Order(3)
	@Bean
	 SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception
	{
		return httpSecurity.csrf(csrf-> csrf.disable())
				.securityMatchers(match -> match.requestMatchers("/api/version1/**"))
				.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(new AuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
				.build();
	}
	
	@Bean
	AuthenticationManager getAuthentication(AuthenticationConfiguration authenticationConfiguration) throws Exception
	{
		return authenticationConfiguration.getAuthenticationManager();

	}

}
