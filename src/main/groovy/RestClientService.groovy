import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.net.http.HttpTimeoutException

class RestClientService {

    private static final HttpClient client = HttpClient.newHttpClient()
    private static final JsonSlurper slurper = new JsonSlurper()

    static def getPost(int id) {
        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts/$id"))
                .GET()
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 404) throw new RuntimeException("POST_NOT_FOUND")

        return slurper.parseText(response.body())
    }

    static def createPost(String title, String body, int userId) {
        def jsonBody = JsonOutput.toJson([title: title, body: body, userId: userId])

        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return slurper.parseText(response.body())
    }

    // Metoda stworzona specjalnie pod wyzwanie
    static def getNonExistingPost() {
        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts/9999")) // Ten post nie istnieje
                .GET()
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        // W natywnym HttpClient sami sprawdzamy status i decydujemy o rzuceniu wyjątku
        if (response.statusCode() == 404) {
            // Rzucamy wyjątek z kodem błędu w wiadomości
            throw new RuntimeException("API_ERROR_404")
        }

        return slurper.parseText(response.body())
    }

    static List getPostsByUser(int userId) {
        // Tworzymy zapytanie z parametrem po znaku zapytania (?)
        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts?userId=$userId"))
                .GET()
                .build()

        // Wysyłamy i parsujemy odpowiedź
        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        // Zwracamy listę map
        return slurper.parseText(response.body())
    }

    static def createPostWithAddedHeader(String title, String body, int userId) {
        // Konwersja danych na JSON
        def jsonRequest = JsonOutput.toJson([title: title, body: body, userId: userId])

        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
        // --- DODANE NAGŁÓWKI (Zadanie 2) ---
                .header("Content-Type", "application/json") // Informujemy o formacie danych
                .header("Authorization", "Bearer MY_SECRET_TOKEN") // Symulacja tokena
                .header("User-Agent", "Groovy-Learning-App") // Identyfikacja klienta
        // -----------------------------------
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        // Weryfikacja czy serwer przyjął żądanie (201 Created)
        if (response.statusCode() != 201) {
            throw new RuntimeException("Błąd serwera: ${response.statusCode()}")
        }

        return slurper.parseText(response.body())
    }

    static List getAllPosts() {
        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                .GET()
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        // Zwraca listę 100 map (postów)
        return slurper.parseText(response.body())
    }

    static List getAllPostsWithTimeout(long millis) {
        // HttpClient wymaga wartości > 0
        if (millis <= 0) throw new IllegalArgumentException("Timeout must be > 0")

        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                .timeout(Duration.ofMillis(millis)) // Używamy milisekund
                .GET()
                .build()

        try {
            def response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return slurper.parseText(response.body())
        } catch (HttpTimeoutException e) {
            // To złapie PRAWDZIWY timeout sieciowy
            throw new RuntimeException("API_TIMEOUT")
        }
    }
}