package com.example.finmid.converter;

import com.example.finmid.dto.AccountDto;
import com.example.finmid.dto.CreateAccountDto;
import com.example.finmid.entinty.AccountEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class AccountConverter {

    public static AccountEntity createAccountToEntity(CreateAccountDto accountDto) {
        LocalDateTime createDate = LocalDateTime.now(ZoneOffset.UTC);
        return AccountEntity.builder()
                .balance(accountDto.getBalance())
                .createDate(createDate)
                .modifyDate(createDate)
                .build();
    }

    public static AccountDto entityToDto(AccountEntity entity) {
        return AccountDto.builder()
                .id(entity.getId())
                .balance(entity.getBalance())
                .createDate(entity.getCreateDate())
                .modifyDate(entity.getModifyDate())
                .build();
    }

    private AccountConverter() {
    }
}
