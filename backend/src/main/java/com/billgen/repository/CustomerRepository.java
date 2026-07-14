package com.billgen.repository;

import com.billgen.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByGstNumber(String gstNumber);

    List<Customer> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.phone LIKE %:keyword% OR c.gstNumber LIKE %:keyword%")
    List<Customer> searchCustomers(@Param("keyword") String keyword);
}
