package org.gagan.florasense.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.gagan.florasense.Dto.AuthResponse;
import org.gagan.florasense.Dto.CurrentUserResponse;
import org.gagan.florasense.Dto.LoginRequest;
import org.gagan.florasense.Dto.RegisterRequest;
import org.gagan.florasense.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    //1) Register New User
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        AuthResponse response = authService.register(request);
        return  ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //2)Login User
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        AuthResponse authResponse = authService.login(request);
        return  ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    //3)Get currently logged-in user
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(Authentication authentication){
        String username = authentication.getName();

        CurrentUserResponse response = authService.getCurrentUser(username);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
