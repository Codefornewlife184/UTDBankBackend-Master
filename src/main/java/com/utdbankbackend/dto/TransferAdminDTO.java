package com.utdbankbackend.dto;


import com.utdbankbackend.domain.Transfer;
import com.utdbankbackend.domain.enumeration.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferAdminDTO {
    private Long id;
    private Long userId;
    private Long fromAccountId;
    private Long toAccountId;
    private Double transactionAmount;
    private CurrencyCode currencyCode;
    private LocalDateTime transactionDate;
    private String description;



    public TransferAdminDTO(Transfer transfer){
        this.id =transfer.getId();
        this.userId =transfer.getUserId().getId();
        this.fromAccountId =transfer.getFromAccountId().getId();
        this.toAccountId =transfer.getToAccountId().getId();
        this.transactionAmount =transfer.getTransactionAmount();
        this.currencyCode =transfer.getCurrencyCode();
        this.transactionDate =transfer.getTransactionDate();
        this.description =transfer.getDescription();
    }
}
