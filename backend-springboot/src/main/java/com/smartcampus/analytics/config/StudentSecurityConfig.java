package com.smartcampus.analytics.config;

import com.smartcampus.analytics.security.StudentSessionFilter;
import com.smartcampus.analytics.security.StudentSessionService;
import com.smartcampus.analytics.security.FacultySessionService;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class StudentSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    StudentSessionFilter studentSessionFilter(StudentSessionService sessionService,
                                              FacultySessionService facultySessionService) {
        return new StudentSessionFilter(sessionService, facultySessionService);
    }

    @Bean
    SecurityFilterChain studentSecurityFilterChain(HttpSecurity http,
                                                   StudentSessionFilter sessionFilter)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/student-portal/auth/login",
                                "/api/v1/student-portal/auth/logout",
                                "/api/v1/student-portal/auth/roster",
                                "/api/v1/faculty-portal/auth/login",
                                "/api/v1/faculty-portal/auth/logout",
                                "/api/v1/faculty-portal/auth/roster")
                        .permitAll()
                        .requestMatchers("/api/v1/student-portal/**").hasRole("STUDENT")
                        .requestMatchers("/api/v1/faculty-portal/**").hasRole("FACULTY")
                        .requestMatchers("/api/v1/**").hasRole("FACULTY")
                        .anyRequest().denyAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(401))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(403)))
                .addFilterBefore(sessionFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
