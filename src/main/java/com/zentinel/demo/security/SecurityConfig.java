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
                        // Administración total para ADMIN
                        .requestMatchers("/usuarios/**", "/almacenes/**", "/configuracion/**").hasRole("ADMIN")
                        // Catálogos: ADMIN puede crear/editar, MOSTRADOR solo ver
                        .requestMatchers("/productos/nuevo", "/productos/guardar", "/categorias/**",
                                "/proveedores/nuevo", "/proveedores/guardar", "/clientes/nuevo", "/clientes/guardar")
                        .hasRole("ADMIN")
                        .requestMatchers("/productos/**", "/proveedores/**", "/clientes/**")
                        .hasAnyRole("ADMIN", "MOSTRADOR")
                        // Movimientos: Ambos pueden operar
                        .requestMatchers("/entradas/**", "/salidas/**", "/reportes/**").hasAnyRole("ADMIN", "MOSTRADOR")
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
