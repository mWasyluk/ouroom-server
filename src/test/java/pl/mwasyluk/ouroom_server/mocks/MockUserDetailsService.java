package pl.mwasyluk.ouroom_server.mocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;

public class MockUserDetailsService implements UserDetailsService {
    private final Map<String, User> userMap = new HashMap<>();

    public MockUserDetailsService() {
        User user = new User("user", "pass", Set.of(UserAuthority.USER));
        User admin = new User("admin", "pass", Set.of(UserAuthority.ADMIN, UserAuthority.USER));
        
        userMap.put(user.getUsername(), user);
        userMap.put(admin.getUsername(), admin);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (userMap.containsKey(username)) {
            return userMap.get(username);
        }

        throw new UsernameNotFoundException("Username '" + username + "' not found.");
    }
}
