package com.ShopNetwork.ShopNetwork.RestApiFiles.controllers;

import com.ShopNetwork.ShopNetwork.EmailService;
import com.ShopNetwork.ShopNetwork.RestApiFiles.jwt.JwtRequest;
import com.ShopNetwork.ShopNetwork.RestApiFiles.jwt.JwtResponce;
import com.ShopNetwork.ShopNetwork.RestApiFiles.jwt.RefreshJwtRequest;
import com.ShopNetwork.ShopNetwork.RestApiFiles.services.AuthService;
import com.ShopNetwork.ShopNetwork.models.User;
import com.ShopNetwork.ShopNetwork.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.message.AuthException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final UserRepository userRepository;

    private final AuthService authService;

    @PostMapping("/auth/login")
    public ResponseEntity<JwtResponce> login(@RequestBody JwtRequest authReques) throws AuthException {
        final JwtResponce token = authService.login(authReques);
        User user = userRepository.findByUsername(authReques.getUsername());

        //тут отправим токен на электронную почту авторизованному пользователю
        EmailService emailService = new EmailService();
        emailService.SendEmailJWT(user.getEmail(), "Токен");
        return ResponseEntity.ok(token);
    }

    @PostMapping("/auth/token")
    public ResponseEntity<JwtResponce> getNewAccessToken(@RequestBody RefreshJwtRequest request) throws AuthException {
        final JwtResponce token = authService.getAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<JwtResponce> getNewRefreshToken(@RequestBody RefreshJwtRequest request) throws AuthException {
        final JwtResponce token = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }
}