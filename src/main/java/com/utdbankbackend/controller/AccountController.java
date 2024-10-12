package com.utdbankbackend.controller;


import com.utdbankbackend.dto.AccountDTO;
import com.utdbankbackend.domain.Account;
import com.utdbankbackend.dto.AdminAccountDTO;
import com.utdbankbackend.service.AccountService;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Produces(MediaType.APPLICATION_JSON)
@RequestMapping("/account")
public class AccountController {

    public AccountService accountService;
    @GetMapping("/{accountNo}/auth")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<AdminAccountDTO> getAccountByAccountNo(HttpServletRequest request, @PathVariable Long accountNo){
        String ssn = (String) request.getAttribute("ssn");
        AdminAccountDTO account = accountService.findByAccountNoAuth(ssn, accountNo);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }
    @PutMapping("/{accountNo}/update")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE') or hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> updateAccount(HttpServletRequest request, @PathVariable Long accountNo,
                                                             @Valid @RequestBody Account account) {
        String ssn=(String) request.getAttribute("ssn");
        accountService.updateAccount(ssn, accountNo, account);
        Map<String, Object> map= new HashMap<>();
        map.put("success", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
    @PostMapping("/{UserId}/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Boolean>> createAccountAdmin(HttpServletRequest request,
                                                              @Valid @RequestBody Account account) {
        String ssn = (String) request.getAttribute("ssn");
        accountService.add(ssn, account);
        Map<String, Boolean> map = new HashMap<>();
        map.put("Account created successfully!", true);
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> createAccount(HttpServletRequest request,
                                                             @Valid @RequestBody Account account) {
        String ssn = (String) request.getAttribute("ssn");
        Long accId = accountService.add(ssn, account);
        Map<String, Object> map = new HashMap<>();
        map.put("Account created successfully!", true);
        map.put("AccountId", accId);
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }


    @GetMapping("/auth/all")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AdminAccountDTO>> getAllAccounts(@QueryParam(value = "ssn") String ssn){
        List<AdminAccountDTO> accounts = accountService.fetchAllAccounts(ssn);
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/auth")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AdminAccountDTO>> getAccountsByUserId(HttpServletRequest request, @PathVariable Long userId){
        String ssn=(String) request.getAttribute("ssn");
        List<AdminAccountDTO> account = accountService.findAllByUserId(ssn,userId);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @GetMapping("")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<AccountDTO>> getAccountsBySsn(HttpServletRequest request){
        String ssn = (String) request.getAttribute("ssn");
        List<AccountDTO> account = accountService.findAllBySsn(ssn);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }


    @GetMapping("/{accountNo}/user")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<AccountDTO> getAccountBySsnAccountNo(@PathVariable Long accountNo,
                                                               HttpServletRequest request){
        String ssn = (String) request.getAttribute("ssn");
        AccountDTO account = accountService.findBySsnAccountNo(accountNo, ssn);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }


    @PostMapping("/{userId}/create")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> createAccountAuth(HttpServletRequest request,
                                                                 @PathVariable Long userId,
                                                                 @Valid @RequestBody Account account) {
        String ssn = (String) request.getAttribute("ssn");
        Long accId = accountService.addAuth(ssn, userId, account);

        Map<String, Object> map = new HashMap<>();
        map.put("Account created successfully!", true);
        map.put("AccountId", accId);
        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }

    @PutMapping("/{accountNo}/auth")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Boolean>> updateAuthAccount(HttpServletRequest request,
                                                                  @PathVariable Long accountNo,
                                                                  @Valid @RequestBody Account account) {
        String ssn = (String) request.getAttribute("ssn");
        accountService.updateAccountAuth(ssn, accountNo, account);

        Map<String, Boolean> map = new HashMap<>();
        map.put("success", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @DeleteMapping("/{accountNo}/auth")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Boolean>> deleteAccountAuth(HttpServletRequest request, @PathVariable Long accountNo){
        String ssn = (String) request.getAttribute("ssn");
        accountService.removeByAccountIdAuth(ssn, accountNo);
        Map<String, Boolean> map = new HashMap<>();
        map.put("success", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @DeleteMapping("/{accountNo}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Map<String, Boolean>> deleteAccount(HttpServletRequest request, @PathVariable Long accountNo){
        String ssn = (String) request.getAttribute("ssn");
        accountService.removeByAccountId(ssn, accountNo);
        Map<String, Boolean> map = new HashMap<>();
        map.put("success", true);
        return new ResponseEntity<>(map, HttpStatus.OK);
    }



}
