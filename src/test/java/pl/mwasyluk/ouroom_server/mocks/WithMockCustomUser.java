package pl.mwasyluk.ouroom_server.mocks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithUserDetailsSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String name() default MockUserDetailsService.USER;

    @AliasFor("name")
    String value() default MockUserDetailsService.USER;
}
