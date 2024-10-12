package com.utdbankbackend.dto;


import com.utdbankbackend.domain.enumeration.AccountStatus;
import com.utdbankbackend.domain.enumeration.AccountType;
import com.utdbankbackend.domain.enumeration.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long id;

    private Long accountNo;

    @Size(max = 160)
    @NotNull(message = "Please write description")
    private String description;

    @NotNull(message = "Please enter balance")
    private Double balance;

    @NotNull(message = "Please enter currency code")
    private CurrencyCode currencyCode;

    @NotNull(message = "Please choose account type")
    private AccountType accountType;

    @NotNull(message = "Please choose account status type")
    private AccountStatus accountStatus;

    private Timestamp createdDate;

    private Timestamp closedDate;


}
