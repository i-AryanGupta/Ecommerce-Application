package com.ea.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ea.entity.AccessToken;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Integer>{

}
