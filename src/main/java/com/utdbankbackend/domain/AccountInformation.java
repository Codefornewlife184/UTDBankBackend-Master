package com.utdbankbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;


import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "account_info")
public class AccountInformation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @Column(length = 160, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(updatable = false)
    private Timestamp createdDate;

    private Timestamp closedDate;

    public String setInfoModify(String firstName, String lastName, Set<Role> roles) {

        return firstName.toLowerCase()+" "+lastName.toLowerCase()+" "+roles;
    }

//    public Timestamp createdDate;

    @LastModifiedBy
    @Column(length = 250)
    private String lastModifiedBy;

    @LastModifiedDate
    private Timestamp lastModifiedDate;

//    public Timestamp closedDate;

    public AccountInformation(String createdBy, Timestamp createdDate,
                                    String lastModifiedBy, Timestamp lastModifiedDate) {
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
    }

    public AccountInformation(Long id, String lastModifiedBy, Timestamp lastModifiedDate, Timestamp closedDate) {
        this.id = id;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.closedDate = closedDate;
    }

    public String setModifiedBy(String firstName, String lastName, Set<Role> roles) {
        return firstName.toLowerCase() + "_" + lastName.toLowerCase() + "_" + roles;
    }

    public Timestamp setDate() {
        Date date= new Date();
        long time = date.getTime();
        return lastModifiedDate = new Timestamp(time);

    }

}
