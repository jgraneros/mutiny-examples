package org.example.tutorials;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.tuples.Tuple2;

public class UniPipeline {

    private static final String CHUCK_NORRIS_API = "https://api.chucknorris.io/jokes/random";
    private static final String CHUCK_NORRIS_CATEGORIES_API = "https://api.chucknorris.io/jokes/categories";




    /**
     * A Uni represents a stream that can only emit either an item or a failure event.
     * Uni<T> are great to represent aync actions such as a remote procedure call, an HTTP
     * request or an operation producing a single result.
     */

    public static void main(String[] args) throws IOException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        HttpClient httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .version(HttpClient.Version.HTTP_2)
                .build();

        List<String> frases = new ArrayList<>();

        Cancellable quoteOne = Uni.createFrom()
                .item(getQuote(httpClient))
                .onItem().delayIt().by(Duration.ofMillis(1000))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .subscribe()
                .with(value -> procesarRequestQuotes(value, frases), System.out::println);

        System.out.println("hola");


        /**
         * Llamada a la api de categorias, quitar comentario
         */

        /*Cancellable categories = Uni.createFrom().item(getCategories(httpClient))
                .subscribe()
                .with(System.out::println, System.out::println);*/


        /**
         * Ejemplo de combinar Unis, quitar comentario
         */
        /*Uni<String> quoteUni = Uni.createFrom()
                .item(getQuote(httpClient));

        Uni<List<String>> categoriesUni = Uni.createFrom()
                .item(getCategories(httpClient));

        Uni<Tuple2<String, List<String>>> tuple = Uni
                .combine().all().unis(quoteUni, categoriesUni)
                        .asTuple();

        tuple.subscribe().with(t -> {
            System.out.println("Chuck Norris quote: " + t.getItem1());
            System.out.println("Category: " + t.getItem2().get(0));
        });*/




    }

    private static String getQuote(HttpClient httpClient) throws IOException, InterruptedException {

        HttpResponse<String> response;
        ObjectMapper objectMapper = new ObjectMapper();

        HttpRequest request =HttpRequest.newBuilder()
                .uri(URI.create(CHUCK_NORRIS_API))
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode jsonNode = objectMapper.readTree(response.body());

        return jsonNode.get("value").asText();
    }

    public static List<String> getCategories(HttpClient httpClient) throws  IOException, InterruptedException {
        HttpResponse<String> response;
        ObjectMapper objectMapper = new ObjectMapper();

        HttpRequest request =HttpRequest.newBuilder()
                .uri(URI.create(CHUCK_NORRIS_CATEGORIES_API))
                .GET()
                .build();

        response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        List<String> jsonNode = objectMapper.readValue(response.body(), new TypeReference<>() {});

        return jsonNode;
    }

    /**
     * Funciones de suscripcion, Consumers
     */

    private static void procesarRequestQuotes(String value, List<String> list) {

        if (value != null && !value.isEmpty()) {
            System.out.println("agregando frases a la lista de eventos");
            list.add(value);
        }
    }

}
