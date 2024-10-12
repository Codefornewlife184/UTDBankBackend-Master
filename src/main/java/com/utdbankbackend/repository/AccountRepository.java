package com.utdbankbackend.repository;
import com.utdbankbackend.domain.Account;


import com.utdbankbackend.domain.AccountNo;
import com.utdbankbackend.domain.User;
import com.utdbankbackend.domain.enumeration.AccountStatus;
import com.utdbankbackend.domain.enumeration.UserRole;
import com.utdbankbackend.dto.AccountDTO;
import com.utdbankbackend.dto.AdminAccountDTO;
import com.utdbankbackend.exception.ResourceNotFoundException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;



import java.util.List;
import java.util.Optional;


@Transactional
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

        Optional<Account> findByAccountNoAndUserId(AccountNo accountNo, User userId) throws ResourceNotFoundException;
        Optional<Account> findByAccountNo(AccountNo accountNo) throws ResourceNotFoundException;
        Optional<AdminAccountDTO> findByAccountNoOrderById(AccountNo accountNo) throws ResourceNotFoundException;
        Optional<AccountDTO> findByAccountNoAndUserIdOrderById(AccountNo accountNo, User user) throws ResourceNotFoundException;
        List<AdminAccountDTO> findAllByOrderById();
        List<AdminAccountDTO> findAllByUserId(User user) throws ResourceNotFoundException;
        Boolean existsAccountById(Long id)throws ResourceNotFoundException;
        @Query("SELECT new com.utdbankbackend.domain.Account(a) FROM Account a WHERE a.accountNo.id = ?1")
        Optional<Account> findByAccountNoLong(Long accountNo) throws ResourceNotFoundException;

        //    @Query("SELECT a from Account a " +
//            "LEFT JOIN FETCH a.userId u " +
//            "LEFT JOIN FETCH u.roles r " +
//            "WHERE a.userId = ?1 and r.name = ?2")
//    List<AdminAccountDao> findAllByUserIdAndRole(User user, UserRole userRole) throws ResourceNotFoundException;
        @Query("SELECT a from Account a " +
                "LEFT JOIN FETCH a.userId u " +
                "LEFT JOIN FETCH u.roles r " +
                "WHERE r.name = ?1")
        List<AdminAccountDTO> findAllByRole(UserRole userRole);
        List<AccountDTO> findAllByUserIdAndAccountStatus(User user, AccountStatus accountStatus)
                throws ResourceNotFoundException;
}


