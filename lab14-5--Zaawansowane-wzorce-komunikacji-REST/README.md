Lab 14 – Część 2: Zaawansowane wzorce komunikacji REST
------------------------------------------------------

Te przykłady nauczą Cię obsługi żądań asynchronicznych, przetwarzania błędów z treścią JSON oraz pracy z zagnieżdżonymi strukturami danych.

Zadanie 5: Żądania Asynchroniczne (Non-blocking)
------------------------------------------------

W nowoczesnych systemach nie chcemy, aby wątek czekał na wolne API. Użyjemy sendAsync z HttpClient.

Cel: 
Pobierz dwa różne posty jednocześnie (równolegle) i połącz ich tytuły.

Kod w `RestClientService.groovy`:

```groovy


import java.util.concurrent.CompletableFuture

static CompletableFuture<String> getPostTitleAsync(int id) {
def request = HttpRequest.newBuilder()
.uri(URI.create("https://jsonplaceholder.typicode.com/posts/$id"))
.GET()
.build()

    // sendAsync zwraca CompletableFuture
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response -> 
                def json = slurper.parseText(response.body())
                return json.title
            }
}
```

Test w Spocku: `src/test/groovy/RestApiSpec.groovy`

```groovy
def "Zadanie 5: powinien pobrać dwa tytuły asynchronicznie"() {
when:
def future1 = RestClientService.getPostTitleAsync(1)
def future2 = RestClientService.getPostTitleAsync(2)

    // Czekamy na oba wyniki (join() blokuje do czasu zakończenia)
    def titles = [future1.join(), future2.join()]

    then:
    titles.size() == 2
    titles.every { it instanceof String }
}
```




Zadanie 6: Przetwarzanie Szczegółów Błędu (Error Body)
------------------------------------------------------

Często gdy API zwraca błąd (np. 400), przesyła też JSON z opisem błędu 
(np. {"message": "Invalid ID format"}). Musimy umieć to sparsować.

Cel: Zmodyfikuj obsługę błędu, aby wyciągać informację z ciała odpowiedzi.

Kod w `RestClientService.groovy`:

```groovy
static def getWithDetailedError(int id) {
    def request = HttpRequest.newBuilder()
            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/$id"))
            .GET()
            .build()

    def response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() >= 400) {
        // Próbujemy sparsować JSON błędu, jeśli istnieje
        def errorBody = [:]
        try {
            errorBody = slurper.parseText(response.body())
        } catch (Exception e) {
            errorBody = [error: "Raw error: ${response.body()}"]
        }
        
        // Rzucamy wyjątek z bogatym opisem
        throw new RuntimeException("API_ERROR_${response.statusCode()}: ${errorBody}")
    }
    return slurper.parseText(response.body())
}
```

Rozszerzony test w Spocku:
--------------------------

Oto test w Spocku, który weryfikuje Twoją implementację.
Ponieważ korzystamy z publicznego API (jsonplaceholder), 
musimy pamiętać o jednej specyfice: ten serwis dla błędu 404 zwraca pusty obiekt JSON {}.

Dlatego Twój JsonSlurper sparsuje to do pustej mapy w Groovym: [:]. 
Nasz test musi sprawdzić, czy ta pusta mapa (lub cokolwiek innego, co zwróci serwer) faktycznie trafiła do treści wyjątku.

Kod do `RestApiSpec.groovy`

```groovy
def "Zadanie 6: powinien dołączyć treść odpowiedzi (JSON) do komunikatu błędu"() {
when: "wołamy metodę dla nieistniejącego ID (wymuszamy 404)"
RestClientService.getWithDetailedError(99999)

        then: "zostaje rzucony wyjątek RuntimeException"
        def ex = thrown(RuntimeException)

        and: "wiadomość wyjątku zawiera kod statusu (404)"
        ex.message.contains("API_ERROR_404")

        and: "wiadomość wyjątku zawiera sparsowane ciało błędu"
        // JsonPlaceholder dla 404 zwraca pusty JSON '{}'.
        // Slurper zamienia to na pustą mapę Groovy, która w stringu wygląda tak: '[:]'
        println "Otrzymany komunikat błędu: ${ex.message}"
        
        ex.message.contains("[:]") 
    }
```

Wyjaśnienie co tu testujemy:

getWithDetailedError(99999): Wysyła żądanie, serwer odpowiada 404 Not Found oraz body {}.

slurper.parseText("{}"): Zamienia JSON na mapę Groovy [:].

Konstrukcja wyjątku: Twój kod robi: "API_ERROR_404: [:]".

ex.message.contains("[:]"): Sprawdzamy, czy ta mapa faktycznie została "doklejona" do wyjątku.

Gdybyś łączył się z bardziej "gadatliwym" API, które zwraca np. {"reason": "Bad ID"}, 
    w teście sprawdziłbyś po prostu: ex.message.contains("Bad ID").


Zadanie 7: Deep GPath (Praca z zagnieżdżonymi danymi)
-----------------------------------------------------

Wyobraź sobie, że pobierasz listę komentarzy do posta. 
Chcesz wyciągnąć listę domen z adresów e-mail osób, które skomentowały post.

Cel: 
Pobierz komentarze (/posts/1/comments) i stwórz listę unikalnych domen e-mail.

Kod w `RestClientService.groovy`:

```groovy
static List getComments(int postId) {
      def request = HttpRequest.newBuilder()
              .uri(URI.create("https://jsonplaceholder.typicode.com/posts/$postId/comments"))
              .GET()
              .build()

    def response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return slurper.parseText(response.body())
}
```


Test w Spocku (Magia Groovy):

```groovy
def "Zadanie 7: powinien wyciągnąć domeny z maili komentujących"() {
when:
List comments = RestClientService.getComments(1)

    then:
    // 1. comments.email -> lista maili ["Nikita@garfield.biz", "Hayden@althea.it" ...]
    // 2. collect { it.split('@')[1] } -> lista samych domen
    // 3. unique() -> bez duplikatów
    def domains = comments.email.collect { it.split('@')[1] }.unique()

    println "Unikalne domeny komentujących: $domains"
    domains.size() > 0
    domains.contains("garfield.biz")
}
```


Zadanie 8: Prosta mechanika "Retry" (Ponawianie)
------------------------------------------------

W systemach rozproszonych sieć czasem "mignie". 
Napiszemy prostą funkcję w Groovy, która ponowi zapytanie REST w razie błędu.

Kod w `RestApiSpec.groovy` (jako metoda pomocnicza):

```groovy
def withRetry(int maxAttempts, Closure code) {
    int attempts = 0
    while (attempts < maxAttempts) {
        try {
            return code() // Próbuj wykonać kod (np. zapytanie REST)
        } catch (Exception e) {
            attempts++
            println "Próba $attempts nieudana. Ponawiam..."
            if (attempts >= maxAttempts) throw e
            sleep(500) // Odczekaj pół sekundy przed kolejną próbą
        }
    }
}
```


// Użycie w teście:

```groovy

    def "Zadanie 8: powinien ponowić zapytanie w razie awarii"() {
        when:
        def result = withRetry(3) {
            RestClientService.getPost(1)
        }

        then:
        result.id == 1
    }

    def withRetry(int maxAttempts, Closure code) {
        int attempts = 0
        while (attempts < maxAttempts) {
            try {
                // Próba wykonania przekazanego kodu
                return code()
            } catch (Exception e) {
                attempts++
                println ">>> [RETRY] Próba $attempts nieudana (${e.message}). Ponawiam..."

                // Jeśli wyczerpaliśmy limit prób, rzucamy błąd dalej
                if (attempts >= maxAttempts) throw e

                // Czekamy chwilę przed kolejną próbą (np. 500ms)
                sleep(500)
            }
        }
    }

```

Dlaczego te zadania są kluczowe?

Asynchroniczność (Zadanie 5):
-----------------------------
Pozwala na drastyczne skrócenie czasu testów integracyjnych – zamiast pobierać 10 zasobów jeden po drugim, 
pobierasz je równolegle.

Szczegóły błędów (Zadanie 6):
-----------------------------
W debugowaniu na produkcji informacja "400 Bad Request" to za mało. 
Musisz wiedzieć, że serwer dopisał: "Pole 'email' jest wymagane".

GPath i Transformacje (Zadanie 7):
----------------------------------
To pokazuje, dlaczego Groovy deklasuje Javę w testach API. 
Wyciągnięcie domen z maili w Javie to 5-10 linii kodu. 
W Groovy to jeden czytelny łańcuch.

Closures (Zadanie 8): 
---------------------
Mechanizm `withRetry` pokazuje, jak możesz tworzyć własne struktury kontrolne w języku, 
co jest fundamentem tworzenia DSL.

Spróbuj wdrożyć Zadanie 7 i 8 – są najbardziej "groovy-style". 
Czy chcesz, abym pomógł Ci z implementacją któregoś z nich?