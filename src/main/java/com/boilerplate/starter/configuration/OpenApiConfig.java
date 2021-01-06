package com.boilerplate.starter.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securitySchemesItem = new SecurityScheme();
        securitySchemesItem.setType(SecurityScheme.Type.HTTP);
        securitySchemesItem.setScheme("bearer");
        securitySchemesItem.setBearerFormat("JWT");
        securitySchemesItem.setIn(SecurityScheme.In.HEADER);
        securitySchemesItem.setName("Authorization");
        io.swagger.v3.oas.models.info.Info infoVersion = new io.swagger.v3.oas.models.info.Info().title("BoilerPlate API").version("snapshot");
        SecurityRequirement securityItem = new SecurityRequirement().addList("bearer-jwt", Arrays.asList("read", "write"));
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", securitySchemesItem))
                .info(infoVersion)
                .addSecurityItem(securityItem);
    }
}
