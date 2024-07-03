package com.ea.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ea.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

	public RefreshToken findByToken(String token);

}
