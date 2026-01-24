package rs.ac.uns.ftn.asd.Projekatsiit2023.security.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class TokenBasedAuthentication
        extends AbstractAuthenticationToken {

    private final String principal;

    public TokenBasedAuthentication(String email, String role) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        this.principal = email;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
