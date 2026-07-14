package com.billgen.repository;

import com.billgen.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.hsnCode LIKE %:keyword%")
    List<Product> searchProducts(@Param("keyword") String keyword);

    List<Product> findByOrderByCreatedAtDesc();
}
