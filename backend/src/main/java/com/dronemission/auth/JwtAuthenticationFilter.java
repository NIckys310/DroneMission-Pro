package com.dronemission.auth;

import com.dronemission.users.User;
import com.dronemission.users.UserRepository;
import com.dronemission.users.Permission;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro que se ejecuta en cada petición: lee el header "Authorization: Bearer <token>",
 * valida el JWT y registra al usuario (con sus permisos) en el SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            jwtService.extractValidSubject(token)
                    .flatMap(userRepository::findByEmailIgnoreCase)
                    .filter(User::isActive)
                    .ifPresent(user -> authenticate(user, request));
        }
        chain.doFilter(request, response);
    }

    private void authenticate(User user, HttpServletRequest request) {
        Set<String> permissions = user.getRole().getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        // Autoridades = permisos + ROLE_<rol>, para poder usar hasAuthority() o hasRole()
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().name()));

        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().getName(), permissions);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
