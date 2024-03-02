package com.ShopNetwork.ShopNetwork.RestApiFiles.controllers;

import com.ShopNetwork.ShopNetwork.RestApiFiles.jwt.JwtAuthentication;
import com.ShopNetwork.ShopNetwork.RestApiFiles.services.AuthService;
import com.ShopNetwork.ShopNetwork.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

        private final AuthService authService;

        @PreAuthorize("hasRole('USER')")
        @GetMapping("/user")
        public ResponseEntity<String> helloUser() {
            final JwtAuthentication authInfo = authService.getAuthInfo();
            return ResponseEntity.ok("Hello user " + authInfo.getPrincipal() + "!");
        }

        @PreAuthorize("AnyAuthorites('ADMIN')")
        @GetMapping("/admin")
        public ResponseEntity<String> helloAdmin() {
            final JwtAuthentication authInfo = authService.getAuthInfo();
            if(authInfo.getRoles().contains(Role.ADMIN))
                return ResponseEntity.ok("Hello admin " + authInfo.getPrincipal() + "!");
            else
                return ResponseEntity.ok("Доступ запрещен");
        }

    }
