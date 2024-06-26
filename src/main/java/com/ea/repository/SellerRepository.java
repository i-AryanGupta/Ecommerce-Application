package com.ea.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ea.entity.Seller;

public interface SellerRepository extends JpaRepository<Seller, Integer> {

}
