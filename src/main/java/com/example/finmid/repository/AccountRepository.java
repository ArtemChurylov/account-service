package com.example.finmid.repository;

import com.example.finmid.entinty.AccountEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface AccountRepository extends R2dbcRepository<AccountEntity, Long> {
}
