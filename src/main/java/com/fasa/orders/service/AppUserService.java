package com.fasa.orders.service;

import com.fasa.orders.entity.AppUserEntity;
import com.fasa.orders.repository.AppUserRepository;
import com.fasa.orders.repository.OrderSpecifications;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

@Service
public class AppUserService {
    private static final Logger log = LoggerFactory.getLogger(AppUserService.class);
    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Cacheable(value = "app_user_by_username", key = "#username")
    public AppUserEntity getByUsername(String username) {
        log.info("===== Get data from database ============");
        return appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }
}
