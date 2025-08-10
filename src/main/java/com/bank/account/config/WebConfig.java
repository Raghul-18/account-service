package com.bank.account.config;

import com.bank.account.security.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // This interceptor is now optional since JWT processing is done in filter
        // Keeping it for potential additional request processing
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/accounts/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/swagger-docs/**",
                        "/v3/api-docs/**",
                        "/actuator/**"
                );

        log.info("🔧 Web interceptors configured");
    }
}