package it.unicam.cs.ids.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disabilita CSRF per testare le API facilmente
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Sblocca queste rotte
                        .requestMatchers(("/h2-console/**")).permitAll()
                        .anyRequest().authenticated() // Tutto il resto resta protetto
                )
                .formLogin(form -> form.disable()) // Disabilita la mascherina di login che vedi in foto
                .httpBasic(Customizer.withDefaults()); // Permette l'autenticazione base se serve

        return http.build();
    }
}