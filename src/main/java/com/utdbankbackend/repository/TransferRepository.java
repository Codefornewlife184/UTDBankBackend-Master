package com.utdbankbackend.repository;


import com.utdbankbackend.domain.Account;
import com.utdbankbackend.domain.Transfer;
import com.utdbankbackend.domain.User;

import com.utdbankbackend.dto.TransferAdminDTO;

import com.utdbankbackend.domain.enumeration.UserRole;



import com.utdbankbackend.dto.TransferDTO;
import com.utdbankbackend.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface TransferRepository extends JpaRepository<Transfer,Long> {

    //**************Get All transfers
    @Query("Select new com.utdbankbackend.dto.TransferAdminDTO (t)  from Transfer t ")
    List<TransferAdminDTO> getAllTransfers();

    //**************Get transfers by user id
    @Query("SELECT new com.utdbankbackend.dto.TransferAdminDTO (t) "
            + "FROM Transfer t WHERE t.userId.id=:userId")
    List<TransferAdminDTO> findTransferByUserId(Long userId);


    @Query("SELECT new com.utdbankbackend.dto.TransferAdminDTO (t) FROM Transfer t LEFT OUTER JOIN User u ON t.userId.id = u.id LEFT OUTER JOIN u.roles r WHERE r.name =?1 ")
    List<TransferAdminDTO> getAllTransfersforEmployee(UserRole userRole);


    @Query("SELECT new com.utdbankbackend.dto.TransferDTO(t) FROM Transfer t WHERE t.fromAccountId.id = ?1 OR t.toAccountId.id=?1")
    List<TransferDTO> findTransfersByAccount(Long id);

    Optional<TransferDTO> findTransferById(Long id) throws ResourceNotFoundException;

    List<TransferDTO> findAllByUserId(User user) throws ResourceNotFoundException;

    List<Transfer> findAllByFromAccountId(Account id) throws ResourceNotFoundException;

    List<TransferAdminDTO> findAllByFromAccountIdOrderById(Account id) throws ResourceNotFoundException;

    List<TransferDTO> findAllByUserIdAndFromAccountId(User user, Account account);

}
