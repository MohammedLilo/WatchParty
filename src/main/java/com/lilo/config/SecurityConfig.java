package com.lilo.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(requests -> {
//			requests.requestMatchers("/","/videos/**","/js/**","/ws/**","/watch-parties/**","/user-info").authenticated();
			requests.requestMatchers("/signup", "/register", "/login", "/public", "/login.html", "/signup.html", "/js/**", "/css/**", "/swagger-ui/**", // Allow Swagger UI
					"/v3/api-docs/**")
					.permitAll()
					.anyRequest()
					.authenticated();

		});
		http.httpBasic(basic -> Customizer.withDefaults());
		http.csrf(csrf -> csrf.disable());
		http.formLogin(form -> {
			form.loginPage("/login").permitAll();
			form.defaultSuccessUrl("/");
		});
		HeaderWriterLogoutHandler clearSiteData = new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(Directive.ALL));
		http.logout((logout) -> logout.addLogoutHandler(clearSiteData));
		
		http.cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
				CorsConfiguration config = new CorsConfiguration();
				config.addAllowedMethod("*");
				config.addAllowedHeader("*");
				config.addAllowedOrigin("*");
				config.setAllowCredentials(true);
				config.setMaxAge(Duration.ofSeconds(120));
				return config;
			}
		}));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
