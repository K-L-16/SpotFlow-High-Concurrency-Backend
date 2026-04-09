package com.kl.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityScheme;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("TicketingSystem Demo")
                        .version("1.0")
                        .description("Spring Boot + JPA + mysql + Redis + RabbitMQ"))
                .components(new Components()
                        .addSecuritySchemes("authorization", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("authorization"));
    }
}