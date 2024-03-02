package com.ShopNetwork.ShopNetwork.models;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum Role implements GrantedAuthority {
    USER, ADMIN, MODERATOR, BANNED;

    @Override
    public String getAuthority() {
        return name();
    }
}
