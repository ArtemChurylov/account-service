package com.example.finance.repository;

import com.example.finance.entinty.AccountEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface AccountRepository extends R2dbcRepository<AccountEntity, Long> {
}
