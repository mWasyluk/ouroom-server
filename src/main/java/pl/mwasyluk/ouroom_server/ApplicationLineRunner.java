package pl.mwasyluk.ouroom_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import pl.mwasyluk.ouroom_server.data.repository.AccountRepository;
import pl.mwasyluk.ouroom_server.domain.userdetails.Account;
import pl.mwasyluk.ouroom_server.domain.userdetails.UserAuthority;

import java.util.Arrays;

@Profile("dev")
@Configuration
public class ApplicationLineRunner implements CommandLineRunner {
	@Autowired
	private AccountRepository accountRepository;

	@Override
	public void run(String... args) {
		// create and persist the admin Account if it does not exist
		if (accountRepository.findAll().stream().noneMatch((account -> account.getUsername().equals("admin")))) {
			// create an Account with the admin auth
			Account account1 = new Account(
					"admin",
					"{bcrypt}$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu",
					Arrays.asList(UserAuthority.USER, UserAuthority.ADMIN));

			// persist the Account
			Account savedUserWithProfile1 = accountRepository.save(account1);
		}
	}
}
