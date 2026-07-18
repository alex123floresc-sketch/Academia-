package com.unaj.project.config;

import com.unaj.project.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/img/**").permitAll()

                        .requestMatchers("/profesores/**", "/ciclos/**", "/reportes/**").hasRole("ADMIN")
                        .requestMatchers("/horarios/nuevo", "/horarios/guardar", "/horarios/editar/**", "/horarios/eliminar/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/horarios", "/horarios/**").authenticated()

                        .requestMatchers("/cursos/nuevo", "/cursos/guardar", "/cursos/editar/**", "/cursos/eliminar/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/cursos", "/cursos/**").authenticated()

                        .requestMatchers("/alumnos/nuevo", "/alumnos/guardar", "/alumnos/editar/**", "/alumnos/eliminar/**")
                        .hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/alumnos", "/alumnos/**").authenticated()

                        .requestMatchers("/pagos/registrar/**").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/pagos/**").authenticated()

                        .requestMatchers("/matriculas/nueva", "/matriculas/guardar").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/matriculas/anular/**", "/matriculas/eliminar/**").hasRole("ADMIN")
                        .requestMatchers("/matriculas/**").authenticated()
                        .requestMatchers("/inicio").authenticated()
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/alumnos", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }
}