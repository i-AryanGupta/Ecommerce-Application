package com.ea.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ea.entity.User;
import com.google.common.base.Optional;

public interface UserRepository extends JpaRepository<User, Integer>{

	boolean existsByEmail(String email);


}
