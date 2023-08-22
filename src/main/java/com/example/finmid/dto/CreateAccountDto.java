package com.example.finmid.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountDto {

    private BigDecimal balance;
}
