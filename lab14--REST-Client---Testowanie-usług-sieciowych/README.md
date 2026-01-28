Treść Labu 14, dostosowana do Twojego środowiska (JDK 17 + Groovy 4).

Lab 14: REST Client – Integracja Java HttpClient z Groovy
---------------------------------------------------------

Cel: 
Wykorzystanie standardowego java.net.http.HttpClient (dostępnego od Java 11+) do komunikacji REST, 
przy jednoczesnym użyciu magii Groovy'ego do przetwarzania JSON.

Krok 2: Nowoczesny klient REST (Refaktoryzacja)
-----------------------------------------------

Zamiast zewnętrznych bibliotek, używamy wbudowanego w JDK klienta. 
Stwórz plik `src/main/groovy/RestClientService.groovy`

```groovy
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI

class RestClientService {

    private static final HttpClient client = HttpClient.newHttpClient()
    private static final JsonSlurper slurper = new JsonSlurper()

    // Metoda GET - pobieranie danych
    static def getPost(int id) {
        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts/$id"))
                .header("Accept", "application/json")
                .GET()
                .build()

        // BodyHandlers.ofString() zwraca surowy tekst JSON
        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 404) {
            throw new RuntimeException("POST_NOT_FOUND")
        }

        // Groovy zamienia String na dynamiczny obiekt/mapę
        return slurper.parseText(response.body())
    }

    // Metoda POST - wysyłanie danych
    static def createPost(String title, String body, int userId) {
        // JsonOutput zamienia mapę Groovy na String JSON
        def jsonRequest = JsonOutput.toJson([title: title, body: body, userId: userId])
        
        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return slurper.parseText(response.body())
    }
}
```

Krok 3: Test Integracyjny w Spocku
----------------------------------

Testujemy logikę bez zmian w asercjach – to pokazuje potęgę Groovy: silnik pod spodem się zmienił, 
ale łatwość sprawdzania danych (post.id, post.title) pozostała.

Stwórz `src/test/groovy/RestApiSpec.groovy`:

```groovy

import spock.lang.Specification

class RestApiSpec extends Specification {

    def "powinien pobrać post o ID 1 i sprawdzić jego strukturę"() {
        when: "pobieramy post"
        def post = RestClientService.getPost(1)

        then: "dane są poprawnie sparsowane do obiektu"
        post.id == 1
        post.userId instanceof Integer
        post.title != null
        
        and: "możemy użyć regex do sprawdzenia treści"
        post.body ==~ /(?s).*quia.*/ // Czy zawiera słowo 'quia' (flaga (?s) dla wielu linii)
    }

    def "powinien utworzyć nowy zasób i zwrócić wygenerowane ID"() {
        given: "dane wejściowe"
        def title = "Groovy Power"
        def content = "HttpClient + Spock = Love"

        when: "wysyłamy POST"
        def result = RestClientService.createPost(title, content, 99)

        then: "otrzymujemy potwierdzenie od serwisu (ID 101)"
        result.id == 101
        result.title == title
        result.body == content
    }

    def "powinien rzucić wyjątek dla nieistniejącego zasobu"() {
        when: "pytamy o post, którego nie ma"
        RestClientService.getPost(9999)

        then: "przechwytujemy wyjątek"
        def ex = thrown(RuntimeException)
        ex.message == "POST_NOT_FOUND"
    }
}
```


Zero konfliktów: 
Nie musisz się martwić o to, czy biblioteka taka jak http-builder-ng wspiera Groovy 4 czy 5. Używasz JDK, które masz zawsze pod ręką.

Prędkość: 
Brak dodatkowych JAR-ów do załadowania, natywna implementacja w Javie jest niezwykle wydajna.

Czystość: 
Kod Groovy'ego doskonale "opakowuje" przegadane API Javy (jak HttpRequest.newBuilder()), czyniąc je czytelnym.