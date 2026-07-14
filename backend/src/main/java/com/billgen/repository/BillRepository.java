package com.billgen.repository;

import com.billgen.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumber(String billNumber);

    List<Bill> findByCustomerIdOrderByBillDateDesc(Long customerId);

    List<Bill> findByBillDateBetween(LocalDate startDate, LocalDate endDate);

    List<Bill> findByBillDateOrderByBillDateDesc(LocalDate billDate);

    @Query("SELECT b FROM Bill b WHERE b.billDate BETWEEN :startDate AND :endDate ORDER BY b.billDate DESC")
    List<Bill> findBillsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(b) FROM Bill b WHERE b.billDate = :date")
    Long countBillsByDate(@Param("date") LocalDate date);

    @Query("SELECT b FROM Bill b WHERE b.customer.id = :customerId AND b.billDate BETWEEN :startDate AND :endDate")
    List<Bill> findCustomerBillsByDateRange(@Param("customerId") Long customerId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
}
