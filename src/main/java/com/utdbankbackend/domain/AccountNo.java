package com.utdbankbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account_number")
public class AccountNo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator="accounts_seq")
    @SequenceGenerator(name="accounts_seq", sequenceName="accounts_seq", initialValue = 20001705, allocationSize = 1905)
    private Long id;


}
