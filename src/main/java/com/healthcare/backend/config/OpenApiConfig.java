package com.healthcare.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI healthcareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Healthcare Management System API")
                        .description("API documentation for the Healthcare Management System backend.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Healthcare Team"))
                        .license(new License()
                                .name("Internal Use")));
    }
}
