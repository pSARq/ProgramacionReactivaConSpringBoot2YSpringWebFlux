package com.bolsadeideas.springboot.webflux.app;

import com.bolsadeideas.springboot.webflux.app.handler.ProductoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler){
        return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), handler::listar);
    }
}
