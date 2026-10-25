package org.gagan.florasense.Service;

import jakarta.validation.Valid;
import org.gagan.florasense.Dto.AuthResponse;
import org.gagan.florasense.Dto.CurrentUserResponse;
import org.gagan.florasense.Dto.LoginRequest;
import org.gagan.florasense.Dto.RegisterRequest;
import org.gagan.florasense.Entity.User;
import org.gagan.florasense.Exception.*;
import org.gagan.florasense.Repositary.UserRepositary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepositary userRepositary;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    //Register the New User
    public AuthResponse register(@Valid RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())){
            throw new InvalidPasswordException("Password and confirm password do not match");
        }

        if(userRepositary.existsByUsername(request.getUsername())){
            throw  new UsernameAlreadyExistsException("Username already exists");
        }

        if(userRepositary.existsByEmail(request.getEmail())){
            throw  new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        //role, notification settings, isActive will use the defaults defined in User entity
        //save/register the user
        User savedUser = userRepositary.save(user);

        //Generate JWT after successful registeration
        String token = jwtService.generateToken(savedUser);

        //Create Response after registering the User
        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("User registered successfully");
        authResponse.setUserId(savedUser.getUserId());
        authResponse.setUsername(savedUser.getUsername());
        authResponse.setEmail(savedUser.getEmail());
        authResponse.setToken(token);
        return authResponse;
    }

    //Login User
    public AuthResponse login(@Valid LoginRequest request) {
        Optional<User> user = userRepositary.findByEmail(request.getEmail());
        if(user.isEmpty() || !passwordEncoder.matches(request.getPassword(),user.get().getPassword())){
            throw new InvalidEmailOrPasswordException("Invalid email or password");
        }
        //if user is deactivate then don't login until again activate the account
        if(!user.get().getIsActive()){
            throw new AccountDeactivatedException("Your account has been deactivate");
        }
        String token = jwtService.generateToken(user.get());
        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Login successful");
        authResponse.setUserId(user.get().getUserId());
        authResponse.setUsername(user.get().getUsername());
        authResponse.setEmail(user.get().getEmail());
        authResponse.setToken(token);

        return authResponse;
    }

    //Get currently logged-in user
    public CurrentUserResponse getCurrentUser(String username) {
        User user = userRepositary.findByUsername(username)
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found with username: "+username));

        return  new CurrentUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getRole().name());
    }
}
