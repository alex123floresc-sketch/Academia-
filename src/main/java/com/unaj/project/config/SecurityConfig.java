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

                        .requestMatchers("/ciclos/**", "/reportes/**", "/configuracion/**").hasRole("ADMIN")
                        .requestMatchers("/profesores/nuevo", "/profesores/guardar", "/profesores/editar/**",
                                "/profesores/eliminar/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/profesores", "/profesores/**").hasAnyRole("ADMIN", "CAJERO")

                        .requestMatchers("/horas-docentes/pagos/**").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/horas-docentes/**").hasAnyRole("ADMIN", "CAJERO", "AUXILIAR")
                        .requestMatchers("/horarios/nuevo", "/horarios/guardar", "/horarios/quitar-curso/**",
                                "/horarios/bloques/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/horarios", "/horarios/**").hasAnyRole("ADMIN", "CAJERO")

                        .requestMatchers("/cursos/nuevo", "/cursos/guardar", "/cursos/editar/**", "/cursos/eliminar/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/cursos", "/cursos/**").hasAnyRole("ADMIN", "CAJERO")

                        .requestMatchers("/areas/**").hasAnyRole("ADMIN", "CAJERO")

                        .requestMatchers("/alumnos/nuevo", "/alumnos/guardar", "/alumnos/editar/**", "/alumnos/eliminar/**",
                                "/alumnos/*/matricular")
                        .hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/alumnos", "/alumnos/**").authenticated()

                        .requestMatchers("/pagos/registrar/**").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/pagos/**").hasAnyRole("ADMIN", "CAJERO")

                        .requestMatchers("/matriculas/anular/**").hasRole("ADMIN")
                        .requestMatchers("/matriculas/**").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/resumen").hasAnyRole("ADMIN", "CAJERO")
                        .requestMatchers("/asistencias/**").hasAnyRole("ADMIN", "AUXILIAR")
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/inicio", true)
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