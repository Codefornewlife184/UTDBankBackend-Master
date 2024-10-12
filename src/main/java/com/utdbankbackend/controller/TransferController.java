package com.utdbankbackend.controller;

import com.utdbankbackend.domain.Account;
import com.utdbankbackend.domain.Transfer;
import com.utdbankbackend.dto.TransferAdminDTO;
import com.utdbankbackend.dto.TransferDTO;
import com.utdbankbackend.service.AccountService;
import com.utdbankbackend.service.TransferService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*",maxAge = 3600)
@AllArgsConstructor
@RestController
@RequestMapping("/transfer")
public class TransferController {
    private TransferService transferService;

    private AccountService accountService;

    //**************Get All transfers
    @GetMapping("/auth/all")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")

    public ResponseEntity<List<TransferAdminDTO>> getAllUserTransfers(HttpServletRequest request){
        String ssn =  (String) request.getAttribute("ssn");
        List<TransferAdminDTO> transfers= transferService.getAllUserTransfers(ssn);

        return ResponseEntity.ok(transfers);
    }

    //**************Get transfers by user id
    @GetMapping("/user/{userId}/auth")
    @PreAuthorize("hasRole('MANAGER') or hasRole('EMPLOYEE')")

    public ResponseEntity<List<TransferAdminDTO>> getAllUserTransfers(HttpServletRequest request, @PathVariable Long userId){
        String ssn =  (String) request.getAttribute("ssn");
        List<TransferAdminDTO> transfer = transferService.findByUserId(ssn, userId);

        return ResponseEntity.ok(transfer);
    }

//
//    @GetMapping("/{id}/auth")
//    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER')")
//    public ResponseEntity<List<TransferDTO>> getTransfersByAccountId(HttpServletRequest request, @PathVariable Long id){
//
//        String ssn =  (String) request.getAttribute("ssn");
//
//        Optional<Account> account = accountService.findAccountById(id);
//        List<TransferDTO> transfers =  transferService.getTransfersByAccount(ssn, account);
//        return ResponseEntity.ok(transfers);
//    }


    @GetMapping("/{accountNo}/accountNo/auth")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<List<TransferAdminDTO>> getTransfersByAccountNoAuth(HttpServletRequest request,@PathVariable Long accountNo){

        String ssn = (String) request.getAttribute("ssn");
        List<TransferAdminDTO> transfers = transferService.findAllByAccountNoAuth(accountNo, ssn);
        return new ResponseEntity<>(transfers, HttpStatus.OK);
    }
    @PostMapping("/create")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('CUSTOMER')")
    public ResponseEntity<Map<String,Object>> makeTransfer(HttpServletRequest request,
                                                           @RequestBody Map<String,Object>  transfer){

        Transfer trans = new Transfer(transfer.get("fromAccountId"),transfer.get("toAccountId"),
                transfer.get("transactionAmount"),transfer.get("currencyCode"),transfer.get("description") );

        String ssn =  (String) request.getAttribute("ssn");
        Double currentBalance= transferService.makeTransfer(ssn,trans);

        Map<String, Object> map = new HashMap<>();
        map.put("Current Balance",currentBalance);//"Current Balance" : "400.0"
        map.put("Transfer created successfully!", true);//"transfer created succ.." : true

        return new ResponseEntity<>(map, HttpStatus.CREATED);
    }
    @GetMapping("")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('CUSTOMER')")
    public ResponseEntity<List<TransferDTO>> getTransfers(HttpServletRequest request){

        String ssn = (String) request.getAttribute("ssn");
        List<TransferDTO> transfers =  transferService.getTransfers(ssn);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/{transferId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('CUSTOMER')")
    public ResponseEntity<Optional<TransferDTO>> getTransfersByTranserId(HttpServletRequest request , @PathVariable Long transferId){

        String ssn = (String) request.getAttribute("ssn");
        Optional<TransferDTO> transfer =  transferService.getTransferByTransferId(ssn, transferId);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/{accountNo}/accountNo")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<TransferDTO>> getTransfersByAccountNo(HttpServletRequest request,
                                                                         @PathVariable Long accountNo){
        String ssn = (String) request.getAttribute("ssn");
        List<TransferDTO> transfers = transferService.findAllByAccountNo(accountNo, ssn);
        return new ResponseEntity<>(transfers, HttpStatus.OK);
    }
}
