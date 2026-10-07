package com.smartcampus.analytics.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class StudentSessionFilter extends OncePerRequestFilter {

    private final StudentSessionService sessionService;
    private final FacultySessionService facultySessionService;

    public StudentSessionFilter(StudentSessionService sessionService,
                                FacultySessionService facultySessionService) {
        this.sessionService = sessionService;
        this.facultySessionService = facultySessionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            if (token.length() >= 32 && token.length() <= 128) {
                sessionService.studentIdForToken(token).ifPresentOrElse(
                        studentId -> SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        new StudentPrincipal(studentId),
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))),
                        () -> facultySessionService.facultyIdForToken(token).ifPresent(
                                facultyId -> SecurityContextHolder.getContext()
                                        .setAuthentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        new FacultyPrincipal(facultyId),
                                                        null,
                                                        List.of(new SimpleGrantedAuthority(
                                                                "ROLE_FACULTY"))))));
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
