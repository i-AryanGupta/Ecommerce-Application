package com.ea.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ea.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
