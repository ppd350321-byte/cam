package com.canteen.security;

import com.canteen.entity.Admin;
import com.canteen.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class SecurityUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final String tokenType;
    private final Set<String> permissionCodes;
    private final Collection<? extends GrantedAuthority> authorities;

    /** Admin constructor — carries RBAC permissions */
    public SecurityUser(Admin admin, Set<String> permissionCodes) {
        this.id = admin.getId();
        this.username = admin.getUsername();
        this.password = admin.getPasswordHash();
        this.enabled = "active".equals(admin.getStatus()) && !Boolean.TRUE.equals(admin.getIsDeleted());
        this.tokenType = "admin";
        this.permissionCodes = permissionCodes;
        this.authorities = permissionCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    /** Customer constructor — no RBAC permissions */
    public SecurityUser(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.enabled = "active".equals(user.getStatus()) && !Boolean.TRUE.equals(user.getIsDeleted());
        this.tokenType = "customer";
        this.permissionCodes = Set.of();
        this.authorities = Set.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
