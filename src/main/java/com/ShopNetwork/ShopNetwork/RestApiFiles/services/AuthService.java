package com.ShopNetwork.ShopNetwork.RestApiFiles.services;

import com.ShopNetwork.ShopNetwork.RestApiFiles.components.JwtProvider;
import com.ShopNetwork.ShopNetwork.RestApiFiles.jwt.JwtAuthentication;
import com.ShopNetwork.ShopNetwork.RestApiFiles.jwt.JwtRequest;
import com.ShopNetwork.ShopNetwork.RestApiFiles.jwt.JwtResponce;
import com.ShopNetwork.ShopNetwork.models.User;
import com.ShopNetwork.ShopNetwork.repo.UserRepository;
import com.ShopNetwork.ShopNetwork.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.message.AuthException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final Map<String, String> refreshStorage = new HashMap<>();

    private final JwtProvider jwtProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public JwtResponce login(JwtRequest authReques) throws AuthException {
        final User user = userService.findByUsername(authReques.getUsername());
        if (passwordEncoder.matches(authReques.getPassword(), user.getPassword())) {
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String refreshToken = jwtProvider.generateRefreshToken(user);
            refreshStorage.put(user.getUsername(), refreshToken);
            return new JwtResponce(accessToken, refreshToken);
        } else {
            return new JwtResponce("null", "null");
        }
    }

    public JwtResponce getAccessToken(String refreshToken) throws AuthException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final User user = userService.findByUsername(login);
                final String accessToken = jwtProvider.generateAccessToken(user);
                return new JwtResponce(accessToken, null);
            }
        }
        return new JwtResponce(null, null);
    }

    public JwtResponce refresh(String refreshToken) throws AuthException {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final User user = userService.findByUsername(login);
                final String accessToken = jwtProvider.generateAccessToken(user);
                final String newRefreshToken = jwtProvider.generateRefreshToken(user);
                refreshStorage.put(user.getUsername(), newRefreshToken);
                return new JwtResponce(accessToken, newRefreshToken);
            }
        }
        return new JwtResponce("ivalid token", "invalid refresh token");
    }

    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }

}
