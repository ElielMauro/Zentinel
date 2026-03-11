package com.zentinel.demo.security;

import com.zentinel.demo.models.Usuario;
import com.zentinel.demo.repositories.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsuarioAndActivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo: " + username));

        return User.builder()
                .username(usuario.getUsuario())
                .password(usuario.getPassword())
                .roles(usuario.getRol())
                .build();
    }
}
