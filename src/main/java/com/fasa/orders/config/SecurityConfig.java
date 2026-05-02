package com.fasa.orders.config;

import com.fasa.orders.entity.AppUserEntity;
import com.fasa.orders.repository.AppUserRepository;
import com.fasa.orders.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class SecurityConfig {

    private final AppUserDetailsService appUserDetailsService;
    private final AppUserRepository appUserRepository;

    @Value("${app.security.bootstrap.username:admin}")
    private String bootstrapUsername;

    @Value("${app.security.bootstrap.password:ChangeMe123!}")
    private String bootstrapPassword;

    @Value("${app.security.bootstrap.role:ADMIN}")
    private String bootstrapRole;

    public SecurityConfig(
            AppUserDetailsService appUserDetailsService,
            AppUserRepository appUserRepository) {
        this.appUserDetailsService = appUserDetailsService;
        this.appUserRepository = appUserRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(appUserDetailsService)
                .csrf()
                    .ignoringAntMatchers("/api/orders/**", "/health/**")
                .and()
                .authorizeRequests()
                    .antMatchers("/api/orders/**", "/health/**", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                    .antMatchers(HttpMethod.POST, "/users/create").hasRole("ADMIN")
                    .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .failureUrl("/login?error=true")
                    .defaultSuccessUrl("/dashboard?status=PENDING", true)
                    .permitAll()
                .and()
                .logout()
                    .logoutUrl("/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessUrl("/login?logout=true")
                    .permitAll();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void ensureBootstrapUser() {
        if (appUserRepository.existsByUsername(bootstrapUsername)) {
            return;
        }
        AppUserEntity user = new AppUserEntity();
        user.setUsername(bootstrapUsername);
        user.setPasswordHash(new BCryptPasswordEncoder().encode(bootstrapPassword));
        user.setRole((bootstrapRole == null || bootstrapRole.trim().isEmpty())
                ? "ADMIN"
                : bootstrapRole.trim().toUpperCase());
        user.setEnabled(true);
        appUserRepository.save(user);
    }
}
