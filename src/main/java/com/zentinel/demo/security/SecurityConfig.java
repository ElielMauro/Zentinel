package com.zentinel.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/login", "/error").permitAll()
                        // Panel exclusivo del Super Admin (Zentinel)
                        .requestMatchers("/zentinel-master/**").hasRole("SUPER_ADMIN")
                        // Administración de la Empresa
                        .requestMatchers("/usuarios/**", "/almacenes/**").hasAnyRole("ADMIN_EMPRESA", "SUPER_ADMIN", "ADMIN")
                        // Catálogos: ADMIN_EMPRESA puede crear/editar
                        .requestMatchers("/productos/nuevo", "/productos/guardar", "/categorias/**",
                                "/proveedores/nuevo", "/proveedores/guardar", "/clientes/nuevo", "/clientes/guardar")
                        .hasAnyRole("ADMIN_EMPRESA", "SUPER_ADMIN", "MOSTRADOR", "ADMIN")
                        // Lectura y Operación
                        .requestMatchers("/productos/**", "/proveedores/**", "/clientes/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN_EMPRESA", "AUDITOR", "OPERATIVO", "MOSTRADOR", "ADMIN")
                        // Movimientos y Reportes
                        .requestMatchers("/entradas/**", "/salidas/**", "/reportes/**")
                        .hasAnyRole("ADMIN_EMPRESA", "OPERATIVO", "AUDITOR", "MOSTRADOR", "ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
