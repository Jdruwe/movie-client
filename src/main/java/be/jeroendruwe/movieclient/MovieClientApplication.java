package be.jeroendruwe.movieclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@SpringBootApplication
public class MovieClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(MovieClientApplication.class);

    @Bean
    WebClient client() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .filter(logRequest())
                .filter(logResponseStatus())
                .build();
    }

    private static ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> logger.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    private static ExchangeFilterFunction logResponseStatus() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response Status {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    @Bean
    CommandLineRunner demo(WebClient webClient) {

        return args -> {
            
            webClient.get()
                    .uri("/movies/5ab6772cd0c31d39c62930c9/events")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .flatMapMany(response -> response.bodyToFlux(MovieEvent.class))
                    .subscribe(System.out::println);


        };
    }

    public static void main(String[] args) {
        SpringApplication.run(MovieClientApplication.class, args);
    }
}
