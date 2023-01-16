package pl.wasyluva.spring_messengerapi.web.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wasyluva.spring_messengerapi.data.service.AccountService;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.web.http.support.PrincipalService;

@RequiredArgsConstructor

@RestController
@RequestMapping("${apiPrefix}/accounts")
public class AccountController {
    private final AccountService accountService;
    private final PrincipalService principalService;

    @GetMapping
    public ResponseEntity<?> getAccount(){
        return accountService.getAccountById(principalService.getPrincipalAccountId()).getResponseEntity();
    }

    @PostMapping("/register")
    public ResponseEntity<?> createAccount(@RequestBody Account.AccountRegistrationForm newAccountForm){
        ServiceResponse<?> userAccount = accountService.createAccount(newAccountForm);
        return new ResponseEntity<>(userAccount, userAccount.getStatusCode());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(){
        ServiceResponse<?> userAccount = accountService.deleteAccount(principalService.getPrincipalAccountId());
        return new ResponseEntity<>(userAccount, userAccount.getStatusCode());
    }
}
