package pl.mwasyluk.ouroom_server.web.http.support;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import pl.mwasyluk.ouroom_server.data.repository.AccountRepository;
import pl.mwasyluk.ouroom_server.domain.userdetails.Account;

@Service
public class PrincipalService {
    private final AccountRepository accountRepository;

    public PrincipalService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public UUID getPrincipalAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof Account)) {
            return null;
        }

        return ((Account) authentication.getPrincipal()).getId();
    }

    public UUID getPrincipalProfileId() {
        Optional<Account> byId = accountRepository.findById(getPrincipalAccountId());
        if (!byId.isPresent()) {
            return null;
        }
        if (byId.get().getProfile() == null) {
            return null;
        }
        return byId.get().getProfile().getId();
    }
}
