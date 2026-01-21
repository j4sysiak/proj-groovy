Skoro kolekcje masz w małym palcu, czas na LAB 3, czyli to, co sprawia, 
że Groovy jest masowo używany nawet w projektach, gdzie cały kod produkcyjny jest w czystej Javie.

Lab 3: Spock Framework – Data Driven Testing & Mocks
----------------------------------------------------

Cel: 
Nauczysz się pisać testy parametryczne (jeden test dla wielu zestawów danych) oraz dowiesz się, 
jak symulować (mockować) zachowanie innych klas.

Zadanie 1: Data Driven Testing (Tabela Prawdy)
----------------------------------------------

Wyobraź sobie, że piszesz walidator haseł. 
Hasło musi mieć min. 8 znaków. 
Zamiast pisać 5 oddzielnych testów dla różnych haseł, w Spocku używamy bloku where:.

Co masz zrobić:
W folderze `src/test/groovy` stwórz plik `PasswordSpec.groovy` i wpisz tam:


```
import spock.lang.Specification
import spock.lang.Unroll

class PasswordSpec extends Specification {

    @Unroll // Powoduje, że każdy wiersz tabeli będzie widoczny jako osobny test w IntelliJ
    def "hasło '#password' powinno mieć status valid = #expectedResult"() {
        expect: "sprawdzenie długości hasła"
        password.length() >= 8 == expectedResult

        where: "tabela z danymi testowymi"
        password      || expectedResult
        "123"         || false
        "1234567"     || false
        "12345678"    || true
        "tajneHaslo"  || true
        ""            || false
    }
}
```

Dlaczego to jest super?

Spróbuj zmienić expectedResult dla któregoś wiersza i uruchom test. 
Zobaczysz dokładnie, dla której wartości test nie przeszedł.

Możesz używać zmiennych z tabeli (#password) bezpośrednio w nazwie metody!

Zadanie 2: Mockowanie (Symulowanie zależności)
----------------------------------------------

To jest kluczowe w pracy zawodowej. 
Załóżmy, że masz DiscountService, który pobiera aktualny kurs dolara z zewnętrznego serwisu (API banku). 
Nie chcesz łączyć się z internetem podczas testu. 
Musisz "podstawić" fałszywy bank.

Co masz zrobić:
Dopisz pod poprzednim testem (lub w nowym pliku):  nowy plik `DiscountSpec.groovy`:


```
interface RateService {
decimal getRate(String currency)
}

class DiscountService {
RateService rateService

    double calculatePrice(double pricePln) {
        def rate = rateService.getRate("USD")
        return pricePln / rate
    }
}

class DiscountSpec extends Specification {
def "powinien obliczyć cenę w USD używając mocka"() {
given: "mock usługi pobierania kursów"
def rateServiceMock = Mock(RateService)
def service = new DiscountService(rateService: rateServiceMock)

        when: "wyliczamy cenę"
        def result = service.calculatePrice(100.0)

        then: "oczekujemy, że mock zwróci kurs 4.0"
        1 * rateServiceMock.getRate("USD") >> 4.0 // ">>" oznacza: zwróć tę wartość
        
        and: "wynik powinien być poprawny"
        result == 25.0
    }
}
```

Analiza zapisu 1 * rateServiceMock.getRate("USD") >> 4.0:

1 * – Spock sprawdza, czy metoda została wywołana dokładnie raz.

rateServiceMock.getRate("USD") – Spock sprawdza, czy wywołano ją z parametrem "USD".

>> 4.0 – Spock "wstrzykuje" wynik 4.0 do Twojego serwisu.

Twoje Wyzwanie:

Uruchom oba testy.

W zadaniu z hasłem dodaj nowy warunek do tabeli where: – sprawdź, co się stanie, gdy hasło to null. (Podpowiedź: test pewnie rzuci błędem, bo nie można wywołać .length() na nullu. Jak byś to naprawił w kodzie expect?)

W zadaniu z mockiem, zmień 1 * na 2 *. Zobacz, jaki błąd wypisze Spock. To bardzo uczy czytania błędów testowych.

Daj znać, jak Spock "przemówił" do Ciebie swoimi błędami! To najważniejsza lekcja tego Labu.



User
zrob to:
W tym trzecim teście spróbuj sam napisać logikę (na podstawie koszyka z Zadania 2):
Znajdź wszystkie produkty, których cena jest większa niż 5.
Zbierz ich ceny do nowej listy.
Zsumuj je i sprawdź, czy wynik to 36 (Wino 30 + Chleb 6).

Jasne, oto gotowe rozwiązanie Zadania 3 do wklejenia do Twojej klasy Lab2Collections.

Zwróć uwagę na to, jak w Groovym możemy "łańcuchować" (chaining) metody jedna po drugiej.

pilk: `Lab2Collections.groovy`

```
import spock.lang.Specification

class CollectionSpec extends Specification {

    def "powinien przefiltrować i zsumować ceny produktów"() {
        given: "lista produktów"

        // To krótkie wyjaśnienie:
        // w Groovym to jest lista (List) map (Map) — każdy element to mapa z kluczami name, price, cat.
        // Mapy tworzy się składnią [key: value, ...].
        // Do pól można dostać się jako item.name lub item['name'].
        // Poniżej przykład pokazujący utworzenie, odczyt i filtrowanie (użyto findAll i collect, oraz operatora rozproszenia \*.

        def products = [
                [name: 'Laptop', price: 3500, cat: 'Tech'],
                [name: 'Klawiatura', price: 200, cat: 'Tech'],
                [name: 'Sok', price: 5, cat: 'Food']
        ]

        when: "wybieramy tylko produkty Tech i pobieramy ich ceny"
        def techPrices
                = products.findAll { it.cat == 'Tech' }
                                   .collect { it.price }
        // alternatywnie: def techPrices = products.findAll { it.cat == 'Tech' }*.price

        then: "lista cen tech powinna mieć 2 elementy"
        techPrices == [3500, 200]

        and: "suma cen tech powinna wynosić 3700"
        techPrices.sum() == 3700
    }


    def "powinien zademonstrować bezpieczną nawigację na mapie"() {
        given: "mapa zagnieżdżona"
        def user = [profile: [address: [city: 'Warszawa']]]

        expect: "dostęp do nieistniejącego klucza nie rzuca NullPointerException"
        // Użyj operatora ?. (Safe Navigation), aby dostać się do 'street'
        user?.profile?.address?.street == null
    }
}
```

Dlaczego to jest "magiczne"?

findAll { it.price > 5 }: Zwraca nową listę, w której są tylko Mapy pasujące do warunku (Wino i Chleb).

.price: To jest genialny skrót w Groovy. Zamiast pisać .collect { it.price }, możesz po prostu po kropce podać nazwę klucza/pola, a Groovy sam "wyjmie" te wartości z każdego elementu listy i stworzy z nich nową listę: [30, 6].

.sum(): Standardowa metoda Groovy'ego dodana do kolekcji liczbowych.

Jeśli to uruchomiłeś i działa, to masz już solidne podstawy kolekcji!


User
nie kumam tego Laba 2. Jakiś haos. Co mam tak na prawdę zrobić ?
------------------------------------------------------------------


Masz rację, przepraszam. 
Za dużo teorii, za mało konkretnego planu. 
Zróbmy to krok po kroku, w formie konkretnych zadań do wykonania.

Oto co masz zrobić w Lab 2, żeby poczuć "moc" Groovy'ego:

Krok 1: Przygotuj nowy plik testowy
-----------------------------------

W folderze `src/test/groovy` stwórz plik `Lab2Collections.groovy`. Wklej do niego poniższy szkielet kodu:

``````
import spock.lang.Specification

class Lab2Collections extends Specification {

    def "Zadanie 1 - Podstawy list"() {
        given: "Lista liczb"
        def liczby = [1, 5, 10, 15, 20]

        expect: "Ostatni element to 20 (użyj ujemnego indeksu)"
        // WPISZ TUTAJ: liczby[...] == 20
        
        and: "Suma liczb to 51"
        // WPISZ TUTAJ: liczby.sum() == 51
    }

    def "Zadanie 2 - Magia findAll i collect"() {
        given: "Lista zakupów (mapy)"
        def koszyk = [
            [name: 'Piwo', price: 5, alcohol: true],
            [name: 'Mleko', price: 4, alcohol: false],
            [name: 'Wino', price: 30, alcohol: true],
            [name: 'Chleb', price: 6, alcohol: false]
        ]

        when: "Szukamy tylko alkoholu"
        // findAll - szuka wszystkich elementów spełniających warunek
        def alkohole = koszyk.findAll { it.alcohol == true }

        then: "Znaleziono 2 produkty"
        alkohole.size() == 2

        when: "Wyciągamy same nazwy produktów alkoholowych"
        // collect - transformuje każdy element (odpowiednik map w Javie)
        def nazwy = alkohole.collect { it.name }

        then: "Nazwy to Piwo i Wino"
        nazwy == ["Piwo", "Wino"]
    }
}
Krok 2: Co masz teraz zrobić? (Twoje zadania)

Uruchom ten test. (Prawy przycisk myszy na nazwę klasy -> Run). Pierwszy test ("Zadanie 1") się wywali, bo musisz go uzupełnić.

Zadanie 1: W linii liczby[...] == 20 wpisz -1 w nawiasy. W Groovy [-1] to ostatni element listy. Sprawdź, czy przejdzie.

Zadanie 2 (Kluczowe): Dopisz pod spodem w tym samym pliku trzeci test o nazwie: def "Zadanie 3 - Suma cen drogich produktów"().

W tym trzecim teście spróbuj sam napisać logikę (na podstawie koszyka z Zadania 2):

Znajdź wszystkie produkty, których cena jest większa niż 5.

Zbierz ich ceny do nowej listy.

Zsumuj je i sprawdź, czy wynik to 36 (Wino 30 + Chleb 6).

Dlaczego to robimy?

W Javie, żeby zrobić to samo, musiałbyś użyć:
koszyk.stream().filter(it -> it.price > 5).map(it -> it.price).reduce(0, Integer::sum).

W Groovy robisz to tak:
koszyk.findAll { it.price > 5 }.price.sum()

Spróbuj dopisać to "Zadanie 3" i daj znać, czy udało Ci się zsumować te ceny jedną linijką kodu. Jak to zadziała, to Lab 2 uznajemy za zaliczony i idziemy do konkretów ze Spockiem!