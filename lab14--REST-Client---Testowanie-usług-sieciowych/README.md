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



Wyzwanie Finałowe Lab 14:
-------------------------

Zasymulujemy scenariusz "Error Handling".
Dodaj metodę `getNonExistingPost()` do `RestClientService`, która strzela pod adres .../posts/9999 (który nie istnieje).
W Spocku napisz test, który zweryfikuje, co się dzieje, gdy serwer zwróci błąd 404.
Podpowiedź: 
HTTPBuilder-NG domyślnie rzuca wyjątek przy kodach 4xx/5xx. 
Użyj thrown(...) ze Spocka (Lab 7), aby przechwycić błąd i sprawdzić, czy status to rzeczywiście 404.


Oto kompletne rozwiązanie Wyzwania Finałowego Lab 14.
-----------------------------------------------------

Ponieważ odeszliśmy od problematycznej biblioteki HTTPBuilder-NG na rzecz natywnego HttpClient z Javy (co jest lepszą praktyką w Groovy 4), 
dostosujemy obsługę błędów tak, aby była czytelna i łatwa do przetestowania w Spocku.

1. Rozwiązanie w `RestClientService.groovy`

Dodajemy dedykowaną metodę, która celowo uderza w nieistniejący zasób. Zwróć uwagę na rzucanie wyjątku z konkretnym komunikatem.

```groovy

// ... reszta klasy RestClientService ...

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
```

2. Rozwiązanie w RestApiSpec.groovy

Piszesz test, który używa bloku then: thrown(...). To standardowy sposób testowania sytuacji awaryjnych w Spocku.

code
Groovy
download
content_copy
expand_less
// ... reszta klasy RestApiSpec ...

    def "Wyzwanie Finałowe: powinien przechwycić błąd 404 dla nieistniejącego posta"() {
        when: "wywołujemy metodę strzelającą w próżnię"
        RestClientService.getNonExistingPost()

        then: "Spock przechwytuje rzucony RuntimeException"
        // 'thrown' nie tylko sprawdza typ wyjątku, ale zwraca jego instancję do zmiennej 'ex'
        def ex = thrown(RuntimeException)

        and: "weryfikujemy treść błędu"
        ex.message == "API_ERROR_404"
    }
Co tutaj osiągnęliśmy? (Kluczowe punkty)

Kontrola nad błędami: Zamiast polegać na tym, co rzuci biblioteka zewnętrzna, sami zdefiniowaliśmy logikę: "Jeśli status to 404, rzuć błąd z komunikatem X". To sprawia, że Twoje API jest przewidywalne.

Asercja Wyjątku: W Spocku blok then: thrown(Class) musi być pierwszą rzeczą po when:. Jeśli metoda nie rzuci wyjątku, test od razu oblany. To daje 100% pewności, że obsługa błędów działa.

Użycie and:: Rozdzielenie sprawdzenia typu wyjątku od sprawdzenia jego wiadomości (ex.message) poprawia czytelność testu.

Gratulacje!

Właśnie domknąłeś ostatnie wyzwanie techniczne. Twój kod poprawnie obsługuje:
- Pobieranie danych (GET)
- Wysyłanie danych (POST)
- Błędy serwera (404)
- Wielowątkowość (GPars)
- Magiczne transformacje (AST)



Dodatkowe rozszerzenie (Advanced REST Patterns):
------------------------------------------------

W temacie integracji REST, przygotowałem zestaw zadań, które symulują realne problemy z pracy programisty 
(parametry zapytania, nagłówki, zaawansowane filtrowanie danych).

Zadanie 1: Parametry Zapytania (Query Parameters)
-------------------------------------------------

Większość API pozwala na filtrowanie wyników (np. ?userId=1).
Cel: 
Dodaj do `RestClientService` metodę `getPostsByUser(int userId)`, 
która pobiera listę postów tylko dla konkretnego użytkownika.

Wskazówka:
Adres URL powinien wyglądać tak: https://jsonplaceholder.typicode.com/posts?userId=1.

Test do napisania: 
Sprawdź w Spocku, czy:

- Otrzymana lista nie jest pusta.
- Wszystkie elementy na liście mają userId równe temu, o które pytałeś (użyj metody .every { ... } z Lab 12!).

Rozwiązanie zadania 1:
----------------------

To zadanie świetnie pokazuje, jak zwięźle Groovy radzi sobie z weryfikacją dużych zbiorów danych. 
W Javie musiałbyś użyć pętli lub Stream.allMatch(), tutaj robimy to naturalnym językiem.
Oto kompletne rozwiązanie Zadania 1 z Lab 14:

1. Rozwiązanie w `RestClientService.groovy`

Dopisz tę metodę `getPostsByUser(int userId)` do swojej klasy. 
Zauważ, jak łatwo wstrzykujemy parametr do adresu URL za pomocą GStringa ($userId).

```groovy

 
// ... istniejące importy i pola (client, slurper) ...

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
```

2. Rozwiązanie w `RestApiSpec.groovy` (Test Spock)

Ten test udowadnia, że serwis faktycznie filtruje dane. Wykorzystujemy tu potężną metodę .every { ... }.

```groovy
def "Zadanie 1: powinien pobrać posty przefiltrowane przez userId i sprawdzić ich poprawność"() {
given: "wybrany identyfikator użytkownika"
def targetId = 5

        when: "pobieramy posty dla tego użytkownika"
        List posts = RestClientService.getPostsByUser(targetId)

        then: "lista nie jest pusta"
        posts.size() > 0

        and: "wszystkie zwrócone posty należą do użytkownika o ID 5"
        // Metoda .every zwraca true tylko wtedy, gdy warunek w { } 
        // jest spełniony dla KAŻDEGO elementu listy.
        posts.every { post -> post.userId == targetId }
        
        and: "opcjonalnie: sprawdźmy czy pierwszy post ma sensowny tytuł"
        posts[0].title.length() > 0
    }
```

Dlaczego to jest "Practice" (Dobra praktyka)?

Dynamic Typing: 
Zwracamy List, co w Groovy jest bardzo elastyczne. 
Nie musimy tworzyć klasy PostResponse, bo Slurper zamienia JSON w ArrayList map.

Fluent Assertions: 
Zapis posts.every { it.userId == targetId } jest czytelny nawet dla osoby nieprogramującej (analityka, testera). 
To jest tzw. Executable Documentation.

GString Interpolation: 
Użycie $userId bezpośrednio w URI jest bezpieczne i czytelne, o ile nie potrzebujesz zaawansowanego 
kodowania znaków specjalnych (dla prostych liczb to standard).

 

Zadanie 2: Nagłówki i Autoryzacja (Headers)
-------------------------------------------

W prawdziwych systemach musisz wysyłać tokeny bezpieczeństwa.
Cel: 
Zmodyfikuj metodę createPost, aby dodawała nagłówek Authorization: 
Bearer MY_SECRET_TOKEN oraz User-Agent: Groovy-Learning-App.

Wskazówka w kodzie:

.header("Authorization", "Bearer MY_SECRET_TOKEN")
.header("User-Agent", "Groovy-Learning-App")

Rozwiązanie zadania 2:
----------------------

To zadanie przygotuje Cię do pracy z zabezpieczonymi API (np. OAuth2 lub JWT). 
W świecie Javy/Groovy, poprawne przesyłanie nagłówków jest kluczowe, 
aby serwer nie odrzucił Twojego żądania (np. błędem 401 Unauthorized lub 403 Forbidden).

Oto jak zmodyfikować kod, aby wspierał autoryzację i identyfikację aplikacji.

1. Rozwiązanie w `RestClientService.groovy`

dodaj nową metodę `createPostWithAddedHeader(...)`, dodając do buildera żądania wymagane nagłówki.

```groovy
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
```

2. Rozwiązanie w RestApiSpec.groovy (Test Spock)

W teście sprawdzimy, czy mimo dodania nagłówków, serwis nadal poprawnie tworzy zasób.

Uwaga: 
`jsonplaceholder.typicode.com` jest publicznym API, więc zignoruje nasz token, 
ale zaakceptuje go jako poprawne żądanie.

```groovy
def "Zadanie 2: powinien utworzyć post przesyłając nagłówki autoryzacji"() {
given: "dane nowego posta"
def postTitle = "Tytuł z Tokenem"
def postBody = "Treść wysłana z nagłówkiem Authorization"

        when: "wywołujemy createPost (który ma teraz wbudowane nagłówki)"
        def result = RestClientService.createPostWithAddedHeader(postTitle, postBody, 1)

        then: "serwer zwraca nowo utworzony obiekt (status 201 pod maską)"
        result != null
        result.title == postTitle
        result.id == 101 // Standardowe ID zwracane przez ten serwis dla nowych postów
        
        and: "nie rzucono żadnego wyjątku związanego z autoryzacją"
        notThrown(RuntimeException)
    }
```

Dlaczego to jest "Practice" (Dobra praktyka)?

User-Agent: 
Podawanie własnego User-Agent to dobra kultura programowania. 
Pozwala administratorom serwera API rozpoznać, która aplikacja generuje ruch (ułatwia to debugowanie po ich stronie).

Bearer Token: 
To standard w nowoczesnych systemach (JWT). Nagłówek Authorization z przedrostkiem Bearer jest najpowszechniejszą metodą zabezpieczania mikroserwisów.

Wielokrotne wywołanie .header(): 
Builder z Javy pozwala wywoływać tę metodę wielokrotnie. 
Każde wywołanie dodaje kolejny klucz do mapy nagłówków.

Fail-fast (statusCode != 201): 
Dobrą praktyką jest rzucanie wyjątku od razu, gdy kod statusu nie jest taki, jakiego oczekiwaliśmy. 
Dzięki temu test w Spocku od razu pokaże błąd, zamiast próbować parsować pusty wynik.



Zadanie 3: Zaawansowana Analiza Danych (Groovy Magic)
-----------------------------------------------------

Często po pobraniu danych z API musisz wyciągnąć z nich specyficzne informacje.
Cel: 
Napisz test, który pobierze wszystkie posty (endpoint: /posts) i sprawdzi:

Czy post o najdłuższym tytule ma więcej niż 50 znaków? (Użyj data.max { it.title.length() }).

Ile jest unikalnych użytkowników (userId) w całym systemie? (Użyj data.userId.unique().size()).


Rozwiązanie zadania 3:
----------------------

To zadanie pokazuje, jak Groovy upraszcza analizę danych dzięki wbudowanym metodom kolekcji.
To zadanie pokaże Ci, dlaczego Groovy jest absolutnym królem, jeśli chodzi o przetwarzanie danych (Data Processing). W Javie wyciągnięcie takich statystyk wymagałoby rozbudowanych Streamów (Collectors.groupingBy, maxBy itd.). W Groovy robimy to naturalnie, używając metod wbudowanych bezpośrednio w kolekcje.

Oto rozwiązanie Zadania 3: Zaawansowana Analiza Danych.

1. Rozwiązanie w RestClientService.groovy

Dodaj metodę getAllPosts, która pobierze pełną listę 100 rekordów z API.

```groovy


// ... reszta klasy ...

    static List getAllPosts() {
        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                .GET()
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        
        // Zwraca listę 100 map (postów)
        return slurper.parseText(response.body())
    }
```

2. Rozwiązanie w `RestApiSpec.groovy` (Test Spock)

W tym teście użyjemy "magii", czyli metod max, unique oraz mechanizmu wyciągania pól z listy obiektów.

```groovy
def "Zadanie 3: powinien przeanalizować listę wszystkich postów używając magii Groovy"() {
when: "pobieramy wszystkie dostępne posty"
List allPosts = RestClientService.getAllPosts()

        then: "lista zawiera dokładnie 100 elementów (zgodnie z dokumentacją API)"
        allPosts.size() == 100

        and: "znajdujemy post z najdłuższym tytułem"
        // Metoda .max szuka elementu, dla którego wynik domknięcia jest największy
        def longestPost = allPosts.max { it.title.length() }
        
        longestPost.title.length() > 50
        println ">>> Najdłuższy tytuł (${longestPost.title.length()} znaków): ${longestPost.title}"

        and: "obliczamy liczbę unikalnych użytkowników, którzy publikowali posty"
        // 1. allPosts.userId - to tzw. spread operator (shorthand). 
        // Tworzy nową listę zawierającą same ID użytkowników: [1, 1, 1..., 2, 2, 10]
        // 2. .unique() - usuwa duplikaty
        def uniqueUsers = allPosts.userId.unique()
        
        uniqueUsers.size() == 10
        println ">>> Liczba unikalnych autorów: ${uniqueUsers.size()}"
        println ">>> ID autorów: $uniqueUsers"
    }
```


Dlaczego to jest "Groovy Magic" (Analiza)?
Metoda .max { ... }: 
Zamiast sortować listę lub używać skomplikowanych komparatorów, 
podajesz tylko kryterium (it.title.length()). 
Groovy sam przeiteruje listę i zwróci cały obiekt (mapę posta), który spełnia ten warunek.

Shorthand allPosts.userId: 
To jest genialne. 
Jeśli masz listę map (lub obiektów), możesz po kropce podać nazwę klucza, 
a Groovy wygeneruje listę wartości dla tego klucza ze wszystkich elementów. 
W Javie to byłoby: `allPosts.stream().map(Post::getUserId).collect(Collectors.toList())`.

Metoda .unique(): 
W Groovym nie musisz zamieniać listy na Set i z powrotem na listę. 
Metoda unique() modyfikuje listę (lub zwraca nową), zostawiając tylko unikalne wartości.

println w testach: 
Zwróć uwagę, że Spock pozwala na println w blokach then: czy and:. 
To świetny sposób na szybkie podejrzenie danych podczas pisania testu integracyjnego.

 

Zadanie 4: Obsługa Timeoutów (Stability)
----------------------------------------

Systemy nie mogą czekać w nieskończoność na odpowiedź.
Cel:
Ustaw limit czasu (Timeout) na żądanie HTTP.

Rozwiązanie zadania 4:
----------------------

To ostatnie zadanie z Lab 14, ale niezwykle istotne w produkcji. Systemy rozproszone (mikroserwisy) zawodzą najczęściej nie dlatego, że zwracają błędy, ale dlatego, że przestają odpowiadać, blokując wszystkie wątki Twojej aplikacji.

Oto rozwiązanie Zadania 4: Obsługa Timeoutów (Stability).

1. Rozwiązanie w RestClientService.groovy

W natywnym HttpClient z Javy timeout ustawiamy na poziomie budowania żądania (HttpRequest). Dodamy metodę, która pozwala nam sterować czasem oczekiwania.

```groovy
import java.time.Duration
import java.net.http.HttpTimeoutException

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
```
    
    
2. Rozwiązanie w RestApiSpec.groovy (Test Spock)

W teście sprawdzimy dwa scenariusze:
Sukces: 
Gdy damy serwerowi wystarczająco dużo czasu.

Wymuszony błąd: 
Spróbujemy wymusić timeout, dając serwerowi absurdalnie mało czasu (np. 1 milisekundę).

```groovy
import java.net.http.HttpTimeoutException

class RestApiSpec extends Specification {

    // ... poprzednie testy ...

    def "Zadanie 4: powinien rzucić wyjątek, gdy serwer nie wyrobi się w czasie"() {
        when: "ustawiamy ekstremalnie niski timeout (1 milisekunda)"
        RestClientService.getAllPostsWithTimeout(1)

        then: "przechwytujemy nasz błąd biznesowy rzucony w bloku catch"
        def ex = thrown(RuntimeException)
        ex.message == "API_TIMEOUT"
    }

    def "Zadanie 4: dodatkowy test na błędny parametr (0)"() {
        when: "próbujemy podać 0"
        RestClientService.getAllPostsWithTimeout(0)

        then: "JDK rzuca błąd argumentu jeszcze przed wysłaniem żądania"
        thrown(IllegalArgumentException)
    }
}
```

Uwaga techniczna: Wartość Duration.ofSeconds(0) lub bardzo mała wartość w ofMillis natychmiast wyrzuci błąd w HttpClient, co pozwala nam przetestować ścieżkę obsługi wyjątku bez czekania.

Dlaczego to jest "Practice" (Dobra praktyka)?

Fail Fast: Lepiej, żeby Twoja aplikacja "wybuchła" po 2 sekundach i zwróciła błąd użytkownikowi, niż żeby "wisiała" 30 sekund i blokowała pamięć RAM serwera.

Właściwy typ wyjątku: Zauważ, że HttpClient rzuca specyficzny HttpTimeoutException. Dobrą praktyką jest złapanie go i zamiana na coś, co rozumie Twoja aplikacja (np. RuntimeException lub własny ExternalServiceException).

Java Time API: Użycie java.time.Duration jest znacznie bezpieczniejsze i czytelniejsze niż podawanie "gołych" liczb typu int timeout = 5000 (gdzie nie wiadomo, czy to sekundy, czy milisekundy).

PODSUMOWANIE LAB 14

Zakończyłeś najbardziej "życiowy" moduł tego kursu. Potrafisz teraz:

GET/POST do REST API.

Filtrować dane za pomocą Query Params.

Zabezpieczać zapytania nagłówkami (Headers).

Analizować setki rekordów w jednej linii kodu (Groovy Magic).

Stabilizować aplikację za pomocą Timeoutów.

To był wielki finał! 🏆
Zrealizowaliśmy wszystkie Labu (1-14). Stałeś się ekspertem od synergii Java-Groovy.

Czy chcesz, abym przygotował dla Ciebie finałowe zestawienie "Cheat Sheet" z najważniejszymi konstrukcjami, które poznałeś w tym kursie, czy masz jeszcze jakieś pytania?

    }
Dlaczego te zadania są ważne?
Zadanie 1 uczy Cię budowania dynamicznych URL-i.
Zadanie 2 to podstawa bezpieczeństwa (Auth).
Zadanie 3 pokazuje, dlaczego Groovy jest genialny do testów – w Javie musiałbyś napisać pętle lub skomplikowane Streamy, by znaleźć max lub unique. Tutaj robisz to "w locie".
Zadanie 4 to dobra praktyka inżynierska – zapobiegasz zawieszaniu się aplikacji.
