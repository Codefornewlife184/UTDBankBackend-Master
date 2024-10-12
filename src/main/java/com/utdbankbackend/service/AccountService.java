package com.utdbankbackend.service;


import com.utdbankbackend.domain.*;
import com.utdbankbackend.domain.enumeration.UserRole;
import com.utdbankbackend.dto.AccountDTO;
import com.utdbankbackend.domain.enumeration.AccountStatus;
import com.utdbankbackend.dto.AdminAccountDTO;
import com.utdbankbackend.exception.BadRequestException;
import com.utdbankbackend.exception.ResourceNotFoundException;
import com.utdbankbackend.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountNoRepository accountNoRepository;
    private final UserRepository userRepository;
    private final TransferRepository transferRepository;
    private final AccountInfoRepository modifyInformationRepository;
    private final UserService userService;
    private final AccountInformation accountModifyInformation = new AccountInformation();


    private final static String ACCOUNT_NOT_FOUND_MSG = "account with accountNo %d not found";
    private final static String SSN_NOT_FOUND_MSG = "account with ssn %s not found";
    private final static String USER_NOT_FOUND_MSG = "user with id %d not found";


    public List<AdminAccountDTO> fetchAllAccounts(String ssn){
        User admin = userRepository.findBySsn(ssn) .orElseThrow(() ->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        List<UserRole> rolesAdmin = userService.getRoleList(admin);
        if (rolesAdmin.contains(UserRole.ROLE_MANAGER))
            return accountRepository.findAllByOrderById();
        else
            return accountRepository.findAllByRole(UserRole.ROLE_CUSTOMER);
    }
    public List<AdminAccountDTO> findAllByUserId(String ssn, Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, userId)));
        User admin = userRepository.findBySsn(ssn) .orElseThrow(() ->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        List<UserRole> rolesAdmin = userService.getRoleList(admin);
        List<UserRole> rolesUser = userService.getRoleList(user);
        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || rolesUser.contains(UserRole.ROLE_CUSTOMER))
            return accountRepository.findAllByUserId(user);
        else
            throw new BadRequestException(String.format("You dont have permission to access account " +
                    "with userId %d", userId));
    }
    public AdminAccountDTO findByAccountNoAuth(String ssn, Long accountNo) throws ResourceNotFoundException {
        User admin = userRepository.findBySsn(ssn) .orElseThrow(() ->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        AccountNo accountNumber = accountNoRepository.findById(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        AdminAccountDTO account = accountRepository.findByAccountNoOrderById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        User user = userRepository.findById(account.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG,
                        account.getUserId())));
        List<UserRole> rolesAdmin = userService.getRoleList(admin);
        List<UserRole> rolesUser = userService.getRoleList(user);
        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || rolesUser.contains(UserRole.ROLE_CUSTOMER))
            return account;
        else
            throw new BadRequestException(String.format("You dont have permission to access account " +
                    "with accountNo %d", accountNo));
    }
    public List<AccountDTO> findAllBySsn(String ssn) throws ResourceNotFoundException {
        User user = userRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        return accountRepository.findAllByUserIdAndAccountStatus(user, AccountStatus.ACTIVE);
    }
    public AccountDTO findBySsnAccountNo(Long accountNo, String ssn) throws ResourceNotFoundException {
        User user = userRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        AccountNo accountNumber = accountNoRepository.findById(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        AccountDTO accountDTO = accountRepository.findByAccountNoAndUserIdOrderById(accountNumber, user).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        if (accountDTO.getAccountStatus().equals(AccountStatus.ACTIVE))
            return accountDTO;
        else
            throw new BadRequestException(String.format("You dont have active account with accountNo %d", accountNo));
    }
    public Long add(String ssn, Account account) throws BadRequestException {
        User user = userRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        String createdBy = accountModifyInformation.setModifiedBy(user.getFirstName(),
                user.getLastName(), user.getRole());
        Timestamp createdDate = accountModifyInformation.setDate();
        ModifyInformation modifyInformation = new ModifyInformation(createdBy, createdDate,
                createdBy, createdDate);
        modifyInformationRepository.save(accountModifyInformation);
        account.setAccModInfId(accountModifyInformation);
        account.setUserId(user);
        account.setAccountStatus(AccountStatus.ACTIVE);
        AccountNo accountNumber = new AccountNo();
        accountNoRepository.save(accountNumber);
        account.setAccountNo(accountNumber);
        accountRepository.save(account);
        return account.getAccountNo().getId();
    }
    public Long addAuth(String ssn, Long userId, Account account) throws BadRequestException {
        User admin = userRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, userId)));
        String createdBy = accountModifyInformation.setModifiedBy(admin.getFirstName(),
                admin.getLastName(), admin.getRole());
        Timestamp createdDate = accountModifyInformation.setDate();
        AccountInformation accountInformation = new AccountInformation(createdBy, createdDate,
                createdBy, createdDate);
        List<UserRole> rolesAdmin = userService.getRoleList(admin);
        List<UserRole> rolesUser = userService.getRoleList(user);
        modifyInformationRepository.save(accountInformation);
        account.setAccModInfId(accountModifyInformation);
        account.setUserId(user);
        account.setAccountStatus(AccountStatus.ACTIVE);
        AccountNo accountNumber = new AccountNo();
        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || rolesUser.contains(UserRole.ROLE_CUSTOMER)){
            accountNoRepository.save(accountNumber);
            account.setAccountNo(accountNumber);
            accountRepository.save(account);
        }
        else
            throw new BadRequestException(String.format("You dont have permission to create " +
                    "user with userId %d", userId));
        return account.getAccountNo().getId();
    }
    public void updateAccount(String ssn, Long accountNo, Account account) throws BadRequestException {
        User user = userRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        AccountNo accountNumber = accountNoRepository.findById(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        Account acc = accountRepository.findByAccountNoAndUserId(accountNumber, user)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        Timestamp closedDate = null;
        if (account.getAccountStatus().equals(AccountStatus.CLOSED))
            closedDate = accountModifyInformation.setDate();
        String lastModifiedBy = accountModifyInformation.setModifiedBy(user.getFirstName(), user.getLastName(),
                user.getRole());
        Timestamp lastModifiedDate = accountModifyInformation.setDate();
        AccountInformation accountModifyInformation = new AccountInformation(acc.getAccModInfId().getId(),
                lastModifiedBy, lastModifiedDate, closedDate);
        account.setUserId(user);
        account.setId(acc.getId());
        account.setAccModInfId(accountModifyInformation);
        account.setAccountNo(accountNumber);
        account.setBalance(acc.getBalance());
        if (acc.getAccountStatus().equals(AccountStatus.ACTIVE)){
            modifyInformationRepository.save(accountModifyInformation);
            accountRepository.save(account);
        }
        else
            throw new BadRequestException(String.format(
                    "You dont have active account with accountNo %d to update", accountNo));
    }
    public void updateAccountAuth(String ssn, Long accountNo, Account account) throws BadRequestException {
        User admin = userRepository.findBySsn(ssn)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        AccountNo accountNumber = accountNoRepository.findById(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        Account acc = accountRepository.findByAccountNo(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        User user = userRepository.findById(acc.getUserId().getId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG,
                        acc.getUserId().getId())));
        List<UserRole> rolesAdmin = userService.getRoleList(admin);
        List<UserRole> rolesUser = userService.getRoleList(user);
        Timestamp closedDate = null;
        if (account.getAccountStatus().equals(AccountStatus.CLOSED))
            closedDate = accountModifyInformation.setDate();
        String lastModifiedBy = accountModifyInformation.setModifiedBy(admin.getFirstName(), admin.getLastName(),
                admin.getRole());
        Timestamp lastModifiedDate = accountModifyInformation.setDate();
        AccountInformation accountModifyInformation = new AccountInformation(acc.getAccModInfId().getId(),
                lastModifiedBy, lastModifiedDate, closedDate);
        modifyInformationRepository.save(accountModifyInformation);
        account.setUserId(acc.getUserId());
        account.setId(acc.getId());
        account.setAccModInfId(accountModifyInformation);
        account.setAccountNo(accountNumber);
        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || rolesUser.contains(UserRole.ROLE_CUSTOMER))
            accountRepository.save(account);
        else
            throw new BadRequestException(String.format("You dont have permission to update " +
                    "account with accountNo %d", accountNo));
    }
    public void removeByAccountIdAuth(String ssn, Long accountNo) throws ResourceNotFoundException {
        User admin = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
        AccountNo accountNumber = accountNoRepository.findById(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        Account account = accountRepository.findByAccountNo(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
        User user = userRepository.findById(account.getUserId().getId()).orElseThrow(() ->
                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, account.getUserId().getId())));
        List<UserRole> rolesAdmin = userService.getRoleList(admin);
        List<UserRole> rolesUser = userService.getRoleList(user);
        List<Transfer> transfer = transferRepository.findAllByFromAccountId(account);
        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || (rolesAdmin.contains(UserRole.ROLE_EMPLOYEE) &&
                (rolesUser.contains(UserRole.ROLE_CUSTOMER)))) {
            if (!transfer.isEmpty())
                throw new BadRequestException("You cannot delete account because of existing of transfer!");
            else {
                modifyInformationRepository.deleteById(account.getAccModInfId().getId());
                accountRepository.deleteById(account.getId());
            }
        }
        else
            throw new BadRequestException("You don't have permission to delete account!");
    }
    public void removeByAccountId(String ssn, Long accountNo) throws BadRequestException {
        User user = userRepository.findBySsn(ssn).orElseThrow(() ->
                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));

        AccountNo accountNumber = accountNoRepository.findById(accountNo)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));

        Account account = accountRepository.findByAccountNoAndUserId(accountNumber, user)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));

        List<Transfer> transfer = transferRepository.findAllByFromAccountId(account);

        if (!transfer.isEmpty())
            throw new BadRequestException("You cannot delete account because of existing of transfer!");

        else {
            modifyInformationRepository.deleteById(account.getAccModInfId().getId());
            accountRepository.deleteById(account.getId());
        }
    }
































//    private final AccountRepository accountRepository;
//    private final AccountNoRepository accountNoRepository;
//    private final UserRepository userRepository;
//    private final AccountInfoRepository accountInfoRepository;
//    private final AccountInformation accountInformation = new AccountInformation();
//    private final UserService userService;
//    private final ModifyInformationRepository modifyInformationRepository;
//    private final TransferRepository transferRepository;
//
//
//
//    private final static String SSN_NOT_FOUND_MSG = "account with ssn %s not found";
//    private final static String USER_NOT_FOUND_MSG = "user with id %d not found";
//    private static final String ACCOUNT_NOT_FOUND_MSG ="account with id %d not found";
//
//
//    public AccountService(AccountRepository accountRepository, AccountNoRepository accountNoRepository, UserRepository userRepository, AccountInfoRepository accountInfoRepository, UserService userService, ModifyInformationRepository modifyInformationRepository, TransferRepository transferRepository) {
//        this.accountRepository = accountRepository;
//        this.accountNoRepository = accountNoRepository;
//        this.userRepository = userRepository;
//        this.accountInfoRepository = accountInfoRepository;
//        this.userService = userService;
//        this.modifyInformationRepository = modifyInformationRepository;
//        this.transferRepository=transferRepository;
//    }
//    public AccountDTO findByAndAccountNo(Long accountNo) throws ResourceNotFoundException {
//        return accountRepository.findAccountByUserId(accountNo).orElseThrow(()->
//                new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG)));
//    }
//    public Optional<Account> findAccountById(Long id){
//        return (accountRepository.findById(id));
//    }
//    public Optional<Account> findByAccountNo (Long accountNo)
//    {
//        return accountRepository.findByAccountNoLong(accountNo);
//    }
//
//    public List<Account> fetchAllAccounts(){
//        return accountRepository.findAll();
//    }
//    public List<Account> findAllByUserId(Long userId) throws ResourceNotFoundException {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, userId)));
//        return accountRepository.findAllByUserId(user);
//    }
//    public Long add(String ssn, Account account) throws BadRequestException {
//        User user = userRepository.findBySsn(ssn)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
//        String createdBy = accountInformation.setModifiedBy(user.getFirstName(),
//                user.getLastName(), user.getRole());
//        Timestamp createdDate = accountInformation.setDate();
//        AccountInformation accountModifyInformation = new AccountInformation(createdBy, createdDate,
//                createdBy, createdDate);
//        accountInfoRepository.save(accountModifyInformation);
//        account.setAccModInfId(accountModifyInformation);
//        account.setUserId(user);
//        account.setAccountStatus(AccountStatus.ACTIVE);
//        AccountNo accountNo = new AccountNo();
//        accountNoRepository.save(accountNo);
//        account.setAccountNo(accountNo);
//        accountRepository.save(account);
//        return account.getAccountNo().getId();
//    }
//
//
//
//    public AccountService(AccountRepository accountRepository, AccountNoRepository accountNoRepository, UserRepository userRepository, AccountInfoRepository accountInfoRepository, UserService userService, ModifyInformationRepository modifyInformationRepository, TransferRepository transferRepository) {
//        this.accountRepository = accountRepository;
//        this.accountNoRepository = accountNoRepository;
//        this.userRepository = userRepository;
//        this.accountInfoRepository = accountInfoRepository;
//        this.userService = userService;
//        this.modifyInformationRepository = modifyInformationRepository;
//        this.transferRepository = transferRepository;
//    }
//
//
//
//    public void updateAccount(String ssn, Long accountNo, Account accountNew) throws BadRequestException {
//        User user = userRepository.findBySsn(ssn)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
//
//        Account account = accountRepository.findByAndAccountNo(accountNo)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//
//
//        Timestamp closedDate = null;
//        if (accountNew.getAccountStatus().equals(AccountStatus.CLOSED))
//            closedDate = accountInformation.setDate();
//
//        String lastModifiedBy = accountInformation.setModifiedBy(user.getFirstName(), user.getLastName(),
//                user.getRole());
//
//        Timestamp lastModifiedDate = accountInformation.setDate();
//
//        AccountInformation accountModifyInformation = new AccountInformation(accountNew.getAccModInfId().getId(),
//                lastModifiedBy, lastModifiedDate, closedDate);
//
//        account.setId(accountNew.getId());
//        account.setAccountStatus(accountNew.getAccountStatus());
//        account.setAccountType(accountNew.getAccountType());
//        account.setBalance(accountNew.getBalance());
//        account.setAccModInfId(accountModifyInformation);
//        account.setUserId(accountNew.getUserId());
//        account.setDescription(accountNew.getDescription());
//        account.setCurrencyCode(accountNew.getCurrencyCode());
//
//        if (accountNew.getAccountStatus().equals(AccountStatus.ACTIVE)){
//            accountInfoRepository.save(accountModifyInformation);
//            accountRepository.save(account);
//        }
//        else
//            throw new BadRequestException(String.format(
//                    "You dont have active account with accountNo %d to update", accountNo));
//    }
//
//    public List<AccountDTO> findAllBySsn(String ssn) throws ResourceNotFoundException {
//        User user = userRepository.findBySsn(ssn)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
//
//        return accountRepository.findAllByUserIdAndAccountStatus(user, AccountStatus.ACTIVE);
//    }
//
//    public AccountDTO findBySsnAccountNo(Long accountNo, String ssn) throws ResourceNotFoundException {
//        User user = userRepository.findBySsn(ssn)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
//
//        AccountNo accountNumber = accountNoRepository.findById(accountNo)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//        AccountDTO accountDTO = accountRepository.findByAccountNoAndUserIdOrderById(accountNo, user).orElseThrow(() ->
//                new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//        if (accountDTO.getAccountStatus().equals(AccountStatus.ACTIVE))
//            return accountDTO;
//        else
//            throw new BadRequestException(String.format("You dont have active account with accountNo %d", accountNo));
//    }
//
//    public Long addAuth(String ssn, Long userId, Account account) throws BadRequestException {
//        User admin = userRepository.findBySsn(ssn)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, userId)));
//
//        String createdBy = accountInformation.setModifiedBy(admin.getFirstName(),
//                admin.getLastName(), admin.getRole());
//
//        Timestamp createdDate = accountInformation.setDate();
//
//        AccountInformation accountModifyInformation = new AccountInformation(createdBy, createdDate,
//                createdBy, createdDate);
//
//        List<UserRole> rolesAdmin = userService.getRoleList(admin);
//        List<UserRole> rolesUser = userService.getRoleList(user);
//
//        ModifyInformationRepository.save(accountModifyInformation);
//
//        account.setAccModInfId(accountModifyInformation);
//        account.setUserId(user);
//        account.setAccountStatus(AccountStatus.ACTIVE);
//
//        AccountNo accountNumber = new AccountNo();
//
//        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || rolesUser.contains(UserRole.ROLE_CUSTOMER)){
//            accountNoRepository.save(accountNumber);
//            account.setAccountNo(accountNumber);
//            accountRepository.save(account);
//        }
//
//        else
//            throw new BadRequestException(String.format("You dont have permission to create " +
//                    "user with userId %d", userId));
//
//        return account.getAccountNo().getId();
//    }
//
//    public void updateAccountAuth(String ssn, Long accountNo, Account account) throws BadRequestException {
//        User admin = userRepository.findBySsn(ssn)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
//
//        AccountNo accountNumber = accountNoRepository.findById(accountNo)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//        Account acc = accountRepository.findByAccountNo(accountNumber)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//        User user = userRepository.findById(acc.getUserId().getId())
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG,
//                        acc.getUserId().getId())));
//
//        List<UserRole> rolesAdmin = userService.getRoleList(admin);
//        List<UserRole> rolesUser = userService.getRoleList(user);
//
//        Timestamp closedDate = null;
//        if (account.getAccountStatus().equals(AccountStatus.CLOSED))
//            closedDate = accountInformation.setDate();
//
//        String lastModifiedBy = accountInformation.setModifiedBy(admin.getFirstName(), admin.getLastName(),
//                admin.getRole());
//
//        Timestamp lastModifiedDate = accountInformation.setDate();
//
//        AccountInformation accountModifyInformation = new AccountInformation(acc.getAccModInfId().getId(),
//                lastModifiedBy, lastModifiedDate, closedDate);
//
//        ModifyInformationRepository.save(accountModifyInformation);
//
//        account.setUserId(acc.getUserId());
//        account.setId(acc.getId());
//        account.setAccModInfId(accountModifyInformation);
//        account.setAccountNo(accountNumber);
//
//        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || rolesUser.contains(UserRole.ROLE_CUSTOMER))
//            accountRepository.save(account);
//        else
//            throw new BadRequestException(String.format("You dont have permission to update " +
//                    "account with accountNo %d", accountNo));
//    }
//
//    public void removeByAccountIdAuth(String ssn, Long accountNo) throws ResourceNotFoundException {
//        User admin = userRepository.findBySsn(ssn).orElseThrow(() ->
//                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
//
//        AccountNo accountNumber = accountNoRepository.findById(accountNo)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//        Account account = accountRepository.findByAccountNo(accountNumber)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//        User user = userRepository.findById(account.getUserId().getId()).orElseThrow(() ->
//                new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, account.getUserId().getId())));
//
//        List<UserRole> rolesAdmin = userService.getRoleList(admin);
//        List<UserRole> rolesUser = userService.getRoleList(user);
//
//        List<Transfer> transfer = transferRepository.findAllByFromAccountId(account);
//        if (rolesAdmin.contains(UserRole.ROLE_MANAGER) || (rolesAdmin.contains(UserRole.ROLE_EMPLOYEE) &&
//                (rolesUser.contains(UserRole.ROLE_CUSTOMER)))) {
//
//            if (!transfer.isEmpty())
//                throw new BadRequestException("You cannot delete account because of existing of transfer!");
//
//            else {
//                modifyInformationRepository.deleteById(account.getAccModInfId().getId());
//                accountRepository.deleteById(account.getId());
//            }
//        }
//        else
//            throw new BadRequestException("You don't have permission to delete account!");
//    }
//
//
//    public void removeByAccountId(String ssn, Long accountNo) throws BadRequestException {
//        User user = userRepository.findBySsn(ssn).orElseThrow(() ->
//                new ResourceNotFoundException(String.format(SSN_NOT_FOUND_MSG, ssn)));
//
//        AccountNo accountNumber = accountNoRepository.findById(accountNo)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//        Account account = accountRepository.findByAccountNoAndUserId(accountNumber, user)
//                .orElseThrow(() -> new ResourceNotFoundException(String.format(ACCOUNT_NOT_FOUND_MSG, accountNo)));
//
//        List<Transfer> transfer = transferRepository.findAllByFromAccountId(account);
//
//        if (!transfer.isEmpty())
//            throw new BadRequestException("You cannot delete account because of existing of transfer!");
//
//        else {
//            modifyInformationRepository.deleteById(account.getAccModInfId().getId());
//            accountRepository.deleteById(account.getId());
//        }
//    }


}
