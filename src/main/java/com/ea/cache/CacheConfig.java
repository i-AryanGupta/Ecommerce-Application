package com.ea.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ea.entity.User;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Configuration
public class CacheConfig {
	
	
	@Bean
	Cache<String, String> otpCache(){
		
		return CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5))
		.concurrencyLevel(Runtime.getRuntime().availableProcessors()).build();
		
		
	}
	
	@Bean
	Cache<String, User> userCache()
	{
		return CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(15))
				.concurrencyLevel(Runtime.getRuntime().availableProcessors()).build();
	}

}
