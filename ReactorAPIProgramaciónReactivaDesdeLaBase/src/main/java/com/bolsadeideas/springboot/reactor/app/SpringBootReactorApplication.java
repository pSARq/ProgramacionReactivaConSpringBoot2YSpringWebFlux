package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.Comentarios;
import com.bolsadeideas.springboot.reactor.app.models.Usuario;
import com.bolsadeideas.springboot.reactor.app.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //ejemploIterable();
        //ejemploFlatMap();
        //ejemploToString();
        //ejemploCollectList();
        //ejemploUsuarioComentariosFlatMap();
        //ejemploUsuarioComentariosZipWith()
        //ejemploUsuarioComentariosZipWithForma2();
        //ejemploZipWithRangos();
        //ejemploInterval();
        //ejemploDelayElements();
        //ejemploIntervalInfinito();
        //ejemploIntervalDesdeCreate();
        ejemploContraPresion();
    }

    public void ejemploContraPresion(){
        Flux.range(1, 10)
                .log()
                //.limitRate(5) // Esta linea reemplaza todo lo que se llama en .subscribe()
                .subscribe(new Subscriber<Integer>() {

                    private Subscription s;
                    private Integer limite = 5;
                    private Integer consumido = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(limite);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info(integer.toString());
                        consumido++;
                        if (consumido == limite){
                            consumido = 0;
                            s.request(limite);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void ejemploIntervalDesdeCreate(){
        Flux.create(emitter -> {
           Timer timer = new Timer();
           timer.schedule(new TimerTask() {

               private Integer contador = 0;

               @Override
               public void run() {
                   emitter.next(++contador);
                   if (contador == 10){
                       timer.cancel();
                       emitter.complete();
                   }
                   if (contador == 5){
                       emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
                   }
               }
           }, 1000, 1000);
        }).subscribe(next -> log.info(next.toString()),
                error -> log.error(error.getMessage()),
                () -> log.info("Hemos terminado"));
    }

    public void ejemploIntervalInfinito() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown)
                .flatMap(i -> {
                    if (i >= 5){
                        return Flux.error(new InterruptedException("solo, hasta 5"));
                    }
                    return  Flux.just(i);
                })
                .map(segundo -> "Hola "+ segundo)
                .retry(2)
                .subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

        latch.await();
    }

    public void ejemploDelayElements(){
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()));

        rango.blockLast();
    }

    public void ejemploInterval(){
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retraso, (ran, retra) -> ran)
                .doOnNext(i -> log.info(i.toString()))
                .blockLast();
    }

    public void ejemploZipWithRangos(){
        Flux<Integer> rangos = Flux.range(0, 4);
        Flux.just(1, 2, 3, 4)
                .map(i -> i * 2)
                .zipWith(rangos, (uno, dos) -> {
                    return String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos);
                }).subscribe(texto -> log.info(texto));
    }

    public void ejemploUsuarioComentariosZipWithForma2(){
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));

        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("Hola pepe, qué tal!");
            comentarios.addComentario("Mañana voy a la playa!");
            comentarios.addComentario("Estoy tomando el curso de spring con reactor!");
            return comentarios;
        });

        Mono<UsuarioComentarios> usuariosConComentarios = usuarioMono
                .zipWith(comentariosUsuarioMono)
                .map(tuple -> {
                    Usuario usuario = tuple.getT1();
                    Comentarios comentarios = tuple.getT2();
                    return new UsuarioComentarios(usuario, comentarios);
                });

        usuariosConComentarios.subscribe(uc -> log.info(uc.toString()));
    }

    public void ejemploUsuarioComentariosZipWith(){
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));

        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("Hola pepe, qué tal!");
            comentarios.addComentario("Mañana voy a la playa!");
            comentarios.addComentario("Estoy tomando el curso de spring con reactor!");
            return comentarios;
        });

        Mono<UsuarioComentarios> usuariosConComentarios = usuarioMono
                .zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) -> {
                    return new UsuarioComentarios(usuario, comentariosUsuario);
                });

        usuariosConComentarios.subscribe(uc -> log.info(uc.toString()));
    }

    public void ejemploUsuarioComentariosFlatMap(){
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Doe"));

        Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("Hola pepe, qué tal!");
            comentarios.addComentario("Mañana voy a la playa!");
            comentarios.addComentario("Estoy tomando el curso de spring con reactor!");
            return comentarios;
        });

        usuarioMono.flatMap(usuario -> {
            return comentariosUsuarioMono.map(comentarios -> {
                return new UsuarioComentarios(usuario, comentarios);
            });
        }).subscribe(uc -> log.info(uc.toString()));
    }

    public void ejemploCollectList() throws Exception {

        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Andres",  "Guzman"));
        usuariosList.add(new Usuario("Pedro", "Fulano"));
        usuariosList.add(new Usuario("Maria", "Fulana"));
        usuariosList.add(new Usuario("Diego", "Sultano"));
        usuariosList.add(new Usuario("Juan", "Mengano"));
        usuariosList.add(new Usuario("Bruce", "Lee"));
        usuariosList.add(new Usuario("Bruce", "Willis"));

        Flux.fromIterable(usuariosList)
                .collectList()
                .subscribe(lista -> {
                    lista.forEach(item -> log.info(item.toString()));
                });
    }

    public void ejemploToString() throws Exception {

        List<Usuario> usuariosList = new ArrayList<>();
        usuariosList.add(new Usuario("Andres",  "Guzman"));
        usuariosList.add(new Usuario("Pedro", "Fulano"));
        usuariosList.add(new Usuario("Maria", "Fulana"));
        usuariosList.add(new Usuario("Diego", "Sultano"));
        usuariosList.add(new Usuario("Juan", "Mengano"));
        usuariosList.add(new Usuario("Bruce", "Lee"));
        usuariosList.add(new Usuario("Bruce", "Willis"));

        Flux.fromIterable(usuariosList)
                .map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
                .flatMap(nombre -> {
                    if (nombre.contains("bruce".toUpperCase())){
                        return Mono.just(nombre);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(nombre -> {
                    return nombre.toLowerCase();
                }).subscribe(u -> log.info(u.toString()));
    }


    public void ejemploFlatMap() throws Exception {

        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Andres Guzman");
        usuariosList.add("Pedro Fulano");
        usuariosList.add("Maria Fulana");
        usuariosList.add("Diego Sultano");
        usuariosList.add("Juan Mengano");
        usuariosList.add("Bruce Lee");
        usuariosList.add("Bruce Willis");

        Flux.fromIterable(usuariosList)
                .map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
                .flatMap(usuario -> {
                    if (usuario.getNombre().equalsIgnoreCase("bruce")){
                        return Mono.just(usuario);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(usuario -> {
                    String nombre = usuario.getNombre().toLowerCase();
                    usuario.setNombre(nombre);
                    return usuario;
                }).subscribe(u -> log.info(u.toString()));
    }

    public void ejemploIterable() throws Exception {

        List<String> usuariosList = new ArrayList<>();
        usuariosList.add("Andres Guzman");
        usuariosList.add("Pedro Fulano");
        usuariosList.add("Maria Fulana");
        usuariosList.add("Diego Sultano");
        usuariosList.add("Juan Mengano");
        usuariosList.add("Bruce Lee");
        usuariosList.add("Bruce Willis");

        Flux<String> nombres = Flux.fromIterable(usuariosList); //Flux.just("Andres Guzman", "Pedro Fulano", "Maria Fulana", "Diego Sultano", "Juan Mengano", "Bruce Lee", "Bruce Willis");

        Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
                .filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce"))
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("Los nombres no pueden ser vacíos");
                    }

                    System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
                })
                .map(usuario -> {
                    String nombre = usuario.getNombre().toLowerCase();
                    usuario.setNombre(nombre);
                    return usuario;
                });

        usuarios.subscribe(e -> log.info(e.toString()),
                error -> log.error(error.getMessage()),
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("Ha finalizado la ejecución del observable con éxito!");
                    }
                });
    }
}
