package com.bolsadeideas.springboot.webflux.app;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

    @Autowired
    private ProductoService service;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        mongoTemplate.dropCollection("productos").subscribe();
        mongoTemplate.dropCollection("categorias").subscribe();

        Categoria electronico = new Categoria("Electrónico");
        Categoria deporte = new Categoria("Deporte");
        Categoria computacion = new Categoria("Computación");
        Categoria muebles = new Categoria("Muebles");

        Flux.just(electronico, deporte, computacion, muebles)
                .flatMap(service::saveCategoria)
                .doOnNext(categoria -> {
                    log.info("Categoria creada: " + categoria.getNombre()
                            + ", id: " + categoria.getId());
                }).thenMany( //thenMany es para Flux.
                            //then es para Mono
                        Flux.just(new Producto("Tv Panasonic Pantalla LCD", 456.890, electronico),
                                        new Producto("Sony Camara HD Digital", 177.890, deporte),
                                        new Producto("Apple iPod", 460.890, computacion),
                                        new Producto("Xiaomi Redmi Note 8", 590.850, muebles))
                                .flatMap(producto -> {
                                    producto.setCreateAt(new Date());
                                    return service.save(producto);
                                })).subscribe(producto -> log.info(producto.toString()));
    }
}
