package com.example.finance.repository;

import com.example.finance.entinty.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TransactionRepository extends R2dbcRepository<TransactionEntity, Long> {
}
