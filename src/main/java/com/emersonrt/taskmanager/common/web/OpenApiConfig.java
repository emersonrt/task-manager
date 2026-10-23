package com.emersonrt.taskmanager.common.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI taskManagerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Task Manager API")
                .description("Tasks organized by categories, with filtering, pagination and RFC 7807 errors")
                .version("v1"));
    }
}
