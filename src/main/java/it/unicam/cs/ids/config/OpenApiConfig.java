package it.unicam.cs.ids.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {


    //Questo permette di dire a Swagger che le chiamate API richiedono un token Bearer. Altrimenti, quando si clicca "Try it out" su Swagger, si riceve un errore 403.
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HackHub API Documentation")
                        .version("1.0")
                        .description("API documentation for project: HackHub IDS 2026"));

    }
}