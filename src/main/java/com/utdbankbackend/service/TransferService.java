package com.utdbankbackend.service;

import com.utdbankbackend.domain.Account;
import com.utdbankbackend.domain.AccountNo;
import com.utdbankbackend.domain.Transfer;
import com.utdbankbackend.domain.User;

import com.utdbankbackend.dto.TransferAdminDTO;

import com.utdbankbackend.domain.enumeration.UserRole;


import com.utdbankbackend.dto.TransferDTO;
import com.utdbankbackend.exception.BadRequestException;
import com.utdbankbackend.exception.ResourceNotFoundException;
import com.utdbankbackend.repository.AccountNoRepository;
import com.utdbankbackend.repository.AccountRepository;
import com.utdbankbackend.repository.TransferRepository;
import com.utdbankbackend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.utdbankbackend.domain.enumeration.UserRole.ROLE_EMPLOYEE;
import static com.utdbankbackend.domain.enumeration.UserRole.ROLE_MANAGER;

@AllArgsConstructor
@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final AccountNoRepository accountNoRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private final static String SSN_NOT_FOUND_MSG = "user with ssn %s not found";
    private final static String USER_NOT_FOUND_MSG = "user with id %d not found";
    private final static String ACCOUNT_NOT_FOUND_MSG = "account with accountNo %s not found";
    private final static String TRANSFER_NOT_FOUND_MSG = "transfer with id %d not found";


    //**************Get All transfers

    
        

    public List<TransferAdminDTO> getAllUserTransfers(String ssn){
        User user = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, ssn)));

        if(user.getRoles().contains("Employee")){
        return transferRepository.getAllTransfersforEmployee(UserRole.ROLE_CUSTOMER);
        }else
            return transferRepository.getAllTransfers();

    }

    //**************Get transfers by user id


    public List<TransferAdminDTO> findByUserId(String ssn ,Long userId) throws ResourceNotFoundException {

        User user1 = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, ssn)));

        User user2= userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, ssn)));

        if(user2.getRoles().contains("Manager") || user2.getRoles().contains("Employee")){
            if(user1.getRoles().contains("Manager")){
                return  transferRepository.findTransferByUserId(userId);
            }else
                throw new BadRequestException("You are not allowed to do this function");
        }else
            return  transferRepository.findTransferByUserId(userId);


    }
    public List<TransferDTO> getTransfersByAccountUser(String ssn, Optional<Account> account) throws ResourceNotFoundException {

        User user = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, ssn)));

        if(user == account.get().getUserId()) {
            return transferRepository.findTransfersByAccount(account.get().getAccountNo().getId());
        }else
            throw new BadRequestException("This account does not belong to the related user!");
    }



    public List<TransferDTO> getTransfersByAccount(String ssn, Optional<Account> account) throws ResourceNotFoundException {

        User user = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, ssn)));
        if(account.get().getUserId().getRoles().contains("Manager") || account.get().getUserId().getRoles().contains("Employee")){
            if(user.getRoles().contains("Manager")){
                return transferRepository.findTransfersByAccount(account.get().getAccountNo().getId());
            }  else
                throw new BadRequestException("You are not allowed to do this function");
        } else
          return transferRepository.findTransfersByAccount(account.get().getAccountNo().getId());

    }
    public Optional<TransferDTO> getTransferByTransferId(String ssn, Long id) throws ResourceNotFoundException{
        User user = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, ssn)));

        Optional<TransferDTO> transfer = transferRepository.findTransferById(id);
        Optional<Account> fromAccount= accountRepository.findByAccountNoLong(transfer.get().getFromAccountId());

        if(fromAccount.get().getUserId().getId()==user.getId())
        {
            return transfer;
        }else
            throw new BadRequestException("This transfer does not belong to the related user!");
    }
    public Double makeTransfer (String ssn, Transfer transfer) throws BadRequestException {

        User user = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, ssn)));

        Optional<Account> fromAccount = Optional.ofNullable(accountRepository.findByAccountNo(transfer.getFromAccountId()).orElseGet(null));
        Optional<Account> toAccount = Optional.ofNullable(accountRepository.findByAccountNo(transfer.getToAccountId()).orElseGet(null));

        if(user.getId()==fromAccount.get().getUserId().getId()) {
            if (accountRepository.existsAccountById(fromAccount.get().getId())) {
                if (accountRepository.existsAccountById(toAccount.get().getId())) {

                    if (fromAccount.get().getCurrencyCode() == toAccount.get().getCurrencyCode()) {
                        if (fromAccount.get().getCurrencyCode() == transfer.getCurrencyCode()) {
                            if (fromAccount.get().getBalance() >= transfer.getTransactionAmount()) {
                                fromAccount.get().setBalance(
                                        fromAccount.get().getBalance() - transfer.getTransactionAmount());

                                toAccount.get().setBalance(
                                        toAccount.get().getBalance() + transfer.getTransactionAmount());

                            } else
                                throw new BadRequestException("Balance is not enough");

                        } else
                            throw new BadRequestException("Transfer currency should be same with account currency");

                    } else
                        throw new BadRequestException("Transfer is only possible between same currency code accounts");

                } else
                    throw new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, transfer.getToAccountId().getId()));
            } else
                throw new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, transfer.getFromAccountId().getId()));
        }else
            throw new BadRequestException("The account does not belong to the related user!");



        accountRepository.save(fromAccount.get());
        accountRepository.save(toAccount.get());

        LocalDateTime transactionDate = LocalDateTime.now();
        transfer.setTransactionDate(transactionDate);
        transfer.setUserId(user);
        transferRepository.save(transfer);

        return fromAccount.get().getBalance();

    }
    public List<TransferDTO> getTransfers(String ssn) throws ResourceNotFoundException{

        User user = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, ssn)));

        return transferRepository.findAllByUserId(user);
    }

    public List<TransferAdminDTO> findAllByAccountNoAuth(Long accountNo, String ssn) throws ResourceNotFoundException {
        User admin = userRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));

        AccountNo accountNumber = accountNoRepository.findById(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));

        Account account = accountRepository.findByAccountNo(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));


        User user = userRepository.findById(account.getUserId().getId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));

        List<UserRole> rolesAdmin = userService.getRoleList(admin);
        List<UserRole> rolesUser = userService.getRoleList(user);

        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || rolesUser.contains(UserRole.ROLE_CUSTOMER))
            return transferRepository.findAllByFromAccountIdOrderById(account);

        else
            throw new BadRequestException(String.format("You dont have permission to access transfer " +
                    "with accountNo %d", accountNo));
    }


    public List<TransferDTO> findAllByAccountNo(Long accountNo, String ssn) throws ResourceNotFoundException {
        User user = userRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));

        AccountNo accountNumber = accountNoRepository.findById(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));

        Account account = accountRepository.findByAccountNo(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));

        return transferRepository.findAllByUserIdAndFromAccountId(user, account);
    }


}
