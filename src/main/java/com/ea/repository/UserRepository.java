package com.ea.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ea.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>{

	boolean existsByEmail(String email);


	Optional<User> findByUsername(String username);


}
