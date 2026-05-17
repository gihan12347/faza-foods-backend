package com.fasa.orders.service;

import com.fasa.orders.entity.AppUserEntity;
import com.fasa.orders.repository.AppUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserService appUserService;

    public AppUserDetailsService(AppUserService appUserService) {
         this.appUserService = appUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUserEntity user = appUserService.getByUsername(username);

        List<GrantedAuthority> authorities =
                Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }
}
