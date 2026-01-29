import spock.lang.Specification
import java.net.http.HttpTimeoutException


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

    def "Wyzwanie Finałowe: powinien przechwycić błąd 404 dla nieistniejącego posta"() {
        when: "wywołujemy metodę strzelającą w próżnię"
        RestClientService.getNonExistingPost()

        then: "Spock przechwytuje rzucony RuntimeException"
        // 'thrown' nie tylko sprawdza typ wyjątku, ale zwraca jego instancję do zmiennej 'ex'
        def ex = thrown(RuntimeException)

        and: "weryfikujemy treść błędu"
        ex.message == "API_ERROR_404"
    }

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