package com.tlias.weather.security.api;

import com.tlias.weather.common.ApiResponse;
import com.tlias.weather.security.JwtService;
import com.tlias.weather.security.Role;
import com.tlias.weather.security.RoleRepository;
import com.tlias.weather.security.User;
import com.tlias.weather.security.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationController(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already in use");
        }
        Role userRole = roles.findByCode("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User role is missing"));
        User user = users.save(new User(request.username(), passwordEncoder.encode(request.password()), Set.of(userRole), Instant.now()));
        return ApiResponse.of(responseFor(user));
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = users.findByUsername(request.username()).filter(User::isEnabled)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        return ApiResponse.of(responseFor(user));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(Authentication authentication) {
        User user = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return ApiResponse.of(new CurrentUserResponse(user.getId(), user.getUsername(), user.getRoles().stream().map(Role::getCode).sorted().toList()));
    }

    private AuthenticationResponse responseFor(User user) {
        return new AuthenticationResponse(jwtService.createToken(user), "Bearer", user.getRoles().stream().map(Role::getCode).sorted().toList());
    }
}
