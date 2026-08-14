package com.fleet.telematics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI fleetTelematicsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fleet Telematics Stream & Validation Engine API")
                        .description("High-performance IoT telemetry data stream ingestion, vehicle ownership validation, and deduplication backend service.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Fleet Engineering Team")
                                .email("engineering@fleet-telematics.com"))
                        .license(new License().name("Apache 2.0").url("https://spring.io")));
    }
}
