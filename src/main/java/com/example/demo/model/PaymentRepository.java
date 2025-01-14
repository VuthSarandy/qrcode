package com.example.demo.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByMd5Hash(String md5Hash);
    List<PaymentTransaction> findByBankAccount(String bankAccount);
    List<PaymentTransaction> findByStatus(String status);
}