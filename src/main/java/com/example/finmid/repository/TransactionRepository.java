package com.example.finmid.repository;

import com.example.finmid.entinty.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TransactionRepository extends R2dbcRepository<TransactionEntity, Long> {
}
