package com.pm.apigateway;

import com.pm.apigateway.filter.JwtValidationGatewayFilterFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.server.reactive.ServerHttpRequest;

@SpringBootApplication
public class ApiGatewayApplication {

        public static void main(String[] args) {
                SpringApplication.run(ApiGatewayApplication.class, args);
        }

        @Bean
        public RouteLocator customRouteLocator(
                        RouteLocatorBuilder builder,
                        JwtValidationGatewayFilterFactory jwtValidationGatewayFilterFactory) {
                System.out.println("DEBUG: Registering customRouteLocator...");
                return builder.routes()
                                // Route for auth-service
                                // Backend expects /auth/login, so we strip /api
                                .route("auth-service-route", r -> r.path("/api/auth/**")
                                                .filters(f -> f.stripPrefix(1))
                                                .uri("http://auth-service:4005"))

                                // Route for patient-service
                                // Backend expects /patients, so we strip /api
                                // Apply jwtValidationFilter for user request patient endpoint
                                .route("patient-service-route", r -> r.path("/api/patients/**")
                                                .filters(f -> f
                                                                .stripPrefix(1)
                                                                .filter(jwtValidationGatewayFilterFactory
                                                                                .apply(new Object())))
                                                .uri("http://patient-service:4000"))

                                // Route for api-docs
                                .route("api-docs-patient-route", r -> r.path("/api-docs/patients")
                                                .filters(f -> f.rewritePath("/api-docs/patients", "/v3/api-docs"))
                                                .uri("http://patient-service:4000"))

                                .route("api-docs-auth-route", r -> r.path("/api-docs/auth")
                                                .filters(f -> f.rewritePath("/api-docs/auth", "/v3/api-docs"))
                                                .uri("http://auth-service:4005"))

                                .build();
        }

        @Bean
        public WebClient.Builder webClientBuilder() {
                return WebClient.builder();
        }

        @Bean
        public GlobalFilter loggingFilter() {
                return (exchange, chain) -> {
                        ServerHttpRequest request = exchange.getRequest();
                        System.out.println("DEBUG: Incoming Request: " + request.getMethod() + " " + request.getURI());
                        return chain
                                        .filter(exchange)
                                        .then(Mono.fromRunnable(() -> System.out.println("DEBUG: Response Code: "
                                                        + exchange.getResponse().getStatusCode())));
                };
        }

}
