package com.utdbankbackend.domain;


import com.utdbankbackend.domain.enumeration.AccountStatus;
import com.utdbankbackend.domain.enumeration.AccountType;
import com.utdbankbackend.domain.enumeration.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import javax.persistence.Table;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="accounts")
public class Account implements Serializable {


//    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "account_no", referencedColumnName = "id", unique = true, nullable = false)
    private AccountNo accountNo;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User userId;


    @NotNull(message = "Please write your description ")
    @Size(max = 250)
    @Column(length = 250, nullable = false)
    private String description;

    @NotNull(message = "Please enter your balance ")
    @Column(name = "balance", nullable = false)
    private Double balance;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Please enter your currencyCode ")
    @Column(name = "currencyCode", nullable = false)
    private CurrencyCode currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "accountType", nullable = false)
    @NotNull(message = "Please select an accountType")
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Please provide an account Status type")
    @Column(name = "accountStatus", nullable = false)
    private AccountStatus accountStatus;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_info_id", referencedColumnName = "id")
    private AccountInformation accountInfo;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "acc_modify_inf_id", referencedColumnName = "id")
    private AccountInformation accModInfId;

    public Account(Account account) {
        this.id = account.getId();
        this.accountNo = account.getAccountNo();
        this.userId = account.getUserId();
        this.description = account.getDescription();
        this.balance = account.getBalance();
        this.currencyCode = account.getCurrencyCode();
        this.accountType = account.getAccountType();
        this.accountStatus = account.getAccountStatus();
        this.accountInfo = account.getAccountInfo();
        this.accModInfId = account.getAccModInfId();
    }


}
