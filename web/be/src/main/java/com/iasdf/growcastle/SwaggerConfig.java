package com.iasdf.growcastle;

import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // String securityJwtName = "JWT";
        // SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityJwtName);
        Components components = new Components();
                // .addSecuritySchemes(securityJwtName, new SecurityScheme()
                //         .name(securityJwtName)
                //         .type(SecurityScheme.Type.HTTP)
                //         // .scheme(BEARER_TOKEN_PREFIX)
                //         .bearerFormat(securityJwtName));

        return new OpenAPI()
                // .addSecurityItem(securityRequirement)
                .components(components);

    }
}