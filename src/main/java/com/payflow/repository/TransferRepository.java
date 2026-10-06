package com.payflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payflow.entity.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

}