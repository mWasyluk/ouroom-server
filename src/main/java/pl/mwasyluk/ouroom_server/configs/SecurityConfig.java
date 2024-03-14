package pl.mwasyluk.ouroom_server.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import pl.mwasyluk.ouroom_server.services.user.UserService;

@RequiredArgsConstructor

@Configuration
public class SecurityConfig {
    private final UserService userService;
    @Getter
    @Setter
    @Value("${app.endpoint}")
    private String appEndpoint;
    @Getter
    @Setter
    @Value("${server.api.prefix}")
    private String apiPrefix;

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((auth) -> auth
                        // swagger endpoint (enabled on dev profile only)
                        .requestMatchers(apiPrefix + "/swagger/**").permitAll()
                        // options request for any resource
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // static resources access
                        .requestMatchers("/*.js", "/*.jsx", "/*.css", "/*.json", "/*.ico", "/", "/static/**")
                        .permitAll()
                        // websocket
                        .requestMatchers("/ws/**").permitAll()
                        // registration endpoint
                        .requestMatchers(HttpMethod.POST, apiPrefix + "/users").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(c -> c.authenticationEntryPoint(authenticationEntryPoint()))
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
