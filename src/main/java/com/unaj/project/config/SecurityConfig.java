package com.unaj.project.config;

import com.unaj.project.model.TipoAccion;
import com.unaj.project.service.RegistroActividadService;
import com.unaj.project.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final RegistroActividadService registroActividadService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          RegistroActividadService registroActividadService) {
        this.userDetailsService = userDetailsService;
        this.registroActividadService = registroActividadService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> response.sendRedirect(request.getContextPath() + "/inicio");
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username");
            String detalle = (username != null && !username.isBlank()) ? " (usuario: " + username + ")" : "";
            registroActividadService.registrar(username, TipoAccion.LOGIN_FALLIDO, "Sesión", null,
                    "Intento de inicio de sesión fallido" + detalle);
            response.sendRedirect(request.getContextPath() + "/login?error");
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                registroActividadService.registrar(authentication.getName(), TipoAccion.LOGOUT, "Sesión", null,
                        "Cerró sesión");
            }
            response.sendRedirect(request.getContextPath() + "/login?logout");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/img/**", "/fonts/**",
                                "/configuracion/logo", "/configuracion/favicon", "/configuracion/fondo-login",
                                "/sedes/*/foto", "/profesores/*/foto", "/testimonios/*/foto",
                                "/logros-ingreso/*/foto", "/galeria/*/imagen", "/solicitud-informacion")
                        .permitAll()

                        .requestMatchers("/perfil/**").authenticated()
                        .requestMatchers("/ciclos/**", "/reportes/**", "/configuracion/**", "/sedes/**",
                                "/faq/**", "/testimonios/**", "/logros-ingreso/**", "/galeria/**", "/calendario/**",
                                "/pasos-admision/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/solicitudes/**").hasAnyRole("ADMIN", "CAJERO")
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
                        .requestMatchers("/actividad/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
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