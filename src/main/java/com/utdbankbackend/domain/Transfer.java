package com.utdbankbackend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.NotNull;

import com.utdbankbackend.domain.enumeration.CurrencyCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;


@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="transfers")
public class Transfer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User userId;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "fromAccountId", referencedColumnName = "id")
    private AccountNo fromAccountId;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "toAccountId", referencedColumnName = "id")
    private AccountNo toAccountId;

    @Column(nullable = false)
    private Double transactionAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private CurrencyCode currencyCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss", timezone = "Turkey")
    @Column(nullable = true)
    private LocalDateTime transactionDate;

    @Size(max = 50)
    @NotNull(message = "Please enter the description")
    @Column(length = 50, nullable = false)
    private String description;

    public Transfer(Object fromAccountId, Object toAccountId, Object transactionAmount,
                    Object currencyCode, Object description)
    {

        String stringToConvertOne = String.valueOf(fromAccountId);
        Long fromaccountid = Long.parseLong(stringToConvertOne);

        String stringToConvertTwo = String.valueOf(toAccountId);
        Long toaccountid = Long.parseLong(stringToConvertTwo);

        AccountNo fromAccount = new AccountNo(fromaccountid);
        AccountNo toAccount = new AccountNo(toaccountid);

        this.fromAccountId = fromAccount;
        this.toAccountId = toAccount;
        this.transactionAmount = (Double) transactionAmount;
        this.currencyCode = CurrencyCode.valueOf(String.valueOf(currencyCode));
        this.description = (String) description;
    }


}
