package cp7.entities.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_OWNER, ROLE_INFORMER, ROLE_PERSONAL;

    @Override
    public String getAuthority() {
        return name();
    }
}
