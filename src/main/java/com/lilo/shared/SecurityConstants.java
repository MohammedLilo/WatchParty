package com.lilo.shared;

import lombok.Getter;

import static com.lilo.shared.WebConstants.staticResourcesUrlPattern;

public class SecurityConstants {
    @Getter
    private static String[] publicEndpoints = {//"/**",
                                        "/api/v1/auth/signup", "/register", "/api/v1/auth/login"
                                        , "/public", "/login.html", "/signup.html"
                                        , "/js/**", "/css/**", "/swagger-ui/**"
                                        ,"/v3/api-docs/**"
                                        ,"/ws/**"
                                        ,staticResourcesUrlPattern
                                        };

}
