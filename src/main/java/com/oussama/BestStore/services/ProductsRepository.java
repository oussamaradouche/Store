package com.oussama.BestStore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oussama.BestStore.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {

    
}
