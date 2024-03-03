package com.ShopNetwork.ShopNetwork.config;

import com.ShopNetwork.ShopNetwork.RestApiFiles.components.JwtFilter;
import com.ShopNetwork.ShopNetwork.components.AuthProviderImpl;
import com.ShopNetwork.ShopNetwork.components.CustomLogoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig {


    @Autowired
    private AuthProviderImpl authProvider;

    @Autowired
    private CustomLogoutHandler customLogoutHandler;


    @Autowired
    private JwtFilter jwtFilter;

    //тут производим авторизацию для mvc
    @Bean
    public SecurityFilterChain formLoginSecurityFilterChain(HttpSecurity http) throws Exception {
        http.requestMatcher(new AntPathRequestMatcher("/**"))  //тут мы слушаем все, что после нашего адреса
                .requestMatcher(new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**"))) //доступно всем, кроме API
                .authorizeRequests(authorize -> authorize
                        .antMatchers("/admin", "/admin/**").hasRole("ADMIN") // данные ссылки доступны только для тех, у кого роль Админа
                        .antMatchers("/user", "/item/add", "/item/*/delete", "/item/*/update").authenticated() //ссылки доступны только авторизованным пользователям
                        .antMatchers("/", "/api/", "/index", "/item/*").permitAll() //где .permitAll() - доступно всем
                        .antMatchers("/css/**", "/js/**", "/images/**", "/registration").permitAll()
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login").permitAll() //с этой страницы мы молучаем данные об авторизации, так же страница доступна всем
                        .defaultSuccessUrl("/user") //после авторизации нас автоматом перебрасывает на эту страницу
                        .and().authenticationProvider(authProvider))
                .logout().addLogoutHandler(customLogoutHandler);
        return http.build();
    }

    //тут производим авторизацию для API, через JWT TOKEN
    @Bean
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .requestMatcher(new AntPathRequestMatcher("/api")) //тут мы слушаем только то, что идет после ссылки api
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests(authz -> authz
                        .antMatchers("/api/auth/login", "/api/auth/token", "/api/user/add")
                        .permitAll()//ссылки что указаны выше - доступны всем
                        .anyRequest()
                        .authenticated()).addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authProvider)
                .logout().addLogoutHandler(customLogoutHandler);

        return http.build();
    }
    //тут хеширование в base64
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
