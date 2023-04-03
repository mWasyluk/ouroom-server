package pl.mwasyluk.ouroom_server.web.http.controller;

import lombok.RequiredArgsConstructor;
import pl.mwasyluk.ouroom_server.data.service.AccountService;
import pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse;
import pl.mwasyluk.ouroom_server.domain.userdetails.Account;
import pl.mwasyluk.ouroom_server.web.http.support.PrincipalService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor

@RestController
@RequestMapping("${apiPrefix}/accounts")
public class AccountController {
    private final AccountService accountService;
    private final PrincipalService principalService;

    @GetMapping
    public ResponseEntity<?> getAccount() {
        return accountService.getAccountById(principalService.getPrincipalAccountId()).getResponseEntity();
    }

    @PostMapping("/register")
    public ResponseEntity<?> createAccount(@RequestBody Account.AccountRegistrationForm newAccountForm) {
        ServiceResponse<?> userAccount = accountService.createAccount(newAccountForm);
        return new ResponseEntity<>(userAccount, userAccount.getStatusCode());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount() {
        ServiceResponse<?> userAccount = accountService.deleteAccount(principalService.getPrincipalAccountId());
        return new ResponseEntity<>(userAccount, userAccount.getStatusCode());
    }
}
