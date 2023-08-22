package com.example.finmid.entinty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("accounts")
public class AccountEntity {

    @Id
    private Long id;
    private BigDecimal balance;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
}
