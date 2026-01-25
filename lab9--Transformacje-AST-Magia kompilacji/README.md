Przed Tobą Lab 9: Transformacje AST (Abstract Syntax Tree).

To jest poziom "ekspercki" Groovy'ego. 
Dowiesz się, jak kazać kompilatorowi napisać kod za Ciebie. 
AST to struktura, którą kompilator tworzy w pamięci podczas czytania Twojego kodu. 
Groovy pozwala nam "wstrzyknąć" do tej struktury logikę za pomocą prostych adnotacji.

Lab 9: Transformacje AST – Magia kompilacji
-------------------------------------------

Cel: 
Poznanie adnotacji, które optymalizują wydajność, implementują wzorce projektowe i rozwiązują problemy, 
które w Javie wymagają pisania dziesiątek linii kodu (lub używania zewnętrznych narzędzi jak Lombok).

Zadanie 1: @Delegate – Kompozycja zamiast dziedziczenia

Zamiast dziedziczyć po klasie (co jest sztywne), możesz "delegować" zadania do innego obiektu. 
Klasa z adnotacją @Delegate automatycznie zyska wszystkie metody obiektu, na który wskazuje.

Co masz zrobić:
Stwórz plik `src/main/groovy/PrinterDSL.groovy`:

code
Groovy
download
content_copy
expand_less
import groovy.transform.Delegate

class SimplePrinter {
void printLowRes(String text) { println "Niska jakość: $text" }
void connect() { println "Połączono z drukarką." }
}

class SmartPrinter {
String model = "HP Laser 5000"

    // Wszystkie metody z SimplePrinter pojawią się w SmartPrinter!
    @Delegate SimplePrinter basicPrinter = new SimplePrinter()
    
    void printHighRes(String text) { println "Model $model drukuje w 4K: $text" }
}
Zadanie 2: @Memoized – Automatyczny Cache

Wyobraź sobie funkcję, która liczy coś bardzo długo. @Memoized sprawia, że przy drugim wywołaniu z tym samym parametrem, wynik jest brany z pamięci (cache), a nie obliczany ponownie.

Co masz zrobić:
Stwórz klasę AnalyticsService.groovy:

code
Groovy
download
content_copy
expand_less
import groovy.transform.Memoized

class AnalyticsService {

    @Memoized
    String getComplexReport(int id) {
        println "Rozpoczynam ciężkie generowanie raportu dla ID: $id..."
        sleep(3000) // Symulacja ciężkiej pracy (3 sekundy)
        return "Raport #$id - Dane: ${Math.random()}"
    }
}
Zadanie 3: @TailRecursive – Koniec z StackOverflow

Rekurencja w Javie przy dużych liczbach często rzuca StackOverflowError. Groovy potrafi zamienić rekurencję w zwykłą pętlę pod spodem, jeśli użyjesz tej adnotacji.

code
Groovy
download
content_copy
expand_less
import groovy.transform.TailRecursive

class MathWizard {

    @TailRecursive
    static long sumLongRange(long n, long accumulator = 0) {
        if (n == 0) return accumulator
        return sumLongRange(n - 1, accumulator + n)
    }
}
Zadanie 4: Test Spocka (Weryfikacja Magii)

Teraz sprawdźmy, czy to faktycznie działa. Stwórz src/test/groovy/AstSpec.groovy:

code
Groovy
download
content_copy
expand_less
import spock.lang.Specification

class AstSpec extends Specification {

    def "powinien używać metod delegowanych"() {
        given:
        def smartPrinter = new SmartPrinter()

        expect: "metoda z SimplePrinter jest dostępna w SmartPrinter"
        smartPrinter.respondTo("printLowRes")
        smartPrinter.respondTo("connect")
    }

    def "powinien błyskawicznie zwracać zapamiętany wynik (@Memoized)"() {
        given:
        def service = new AnalyticsService()

        when: "pierwsze wywołanie (wolne)"
        long start = System.currentTimeMillis()
        def r1 = service.getComplexReport(101)
        long duration1 = System.currentTimeMillis() - start

        and: "drugie wywołanie z tym samym ID (powinno być natychmiastowe)"
        start = System.currentTimeMillis()
        def r2 = service.getComplexReport(101)
        long duration2 = System.currentTimeMillis() - start

        then: "wyniki są identyczne"
        r1 == r2
        
        and: "drugie wywołanie trwało mniej niż 100ms (zamiast 3s)"
        duration2 < 100
    }

    def "powinien policzyć sumę dla dużej liczby bez błędu stosu"() {
        expect:
        // Bez @TailRecursive to by wywaliło StackOverflow przy 50 000+
        MathWizard.sumLongRange(100_000) == 5000050000L
    }
}
Dlaczego to są "Practices"?

@Delegate: Pozwala unikać głębokich hierarchii dziedziczenia. Zamiast SmartPrinter extends SimplePrinter, używamy kompozycji. To "czystszy" kod.

@Memoized: Idealne do pobierania kursów walut, konfiguracji z bazy danych czy wyników skomplikowanych algorytmów. Dramatycznie przyspiesza systemy.

@TailRecursive: Pozwala pisać eleganckie algorytmy funkcyjne bez ryzyka awarii aplikacji przy dużych danych.

Wyzwanie Finałowe Lab 9:

Dodaj do klasy SmartPrinter adnotację @groovy.transform.ToString.

Ustaw parametry tak, aby toString() wyświetlał tylko pole model, ale nie wyświetlał pola basicPrinter.

Sprawdź w teście Spocka, czy smartPrinter.toString() zawiera "HP Laser 5000".

Daj znać, czy Cache zadziałał i czy drukarka wydrukowała co trzeba! Jak to mamy, to w Lab 10 wejdziemy w dynamiczne budowanie obiektów przez Expando.



Zadanie-2.
----------
Wyzwanie Finałowe Lab 9:
Dodaj do klasy SmartPrinter adnotację @groovy.transform.ToString.
Ustaw parametry tak, aby toString() wyświetlał tylko pole model, ale nie wyświetlał pola basicPrinter.
Sprawdź w teście Spocka, czy smartPrinter.toString() zawiera "HP Laser 5000".

Oto rozwiązanie wyzwania do Lab 9. 
W tym zadaniu wykorzystasz jedną z najczęściej używanych transformacji AST – @ToString, 
która automatycznie generuje czytelną reprezentację obiektu, oszczędzając Ci pisania żmudnego kodu.

1. Rozwiązanie: Klasa SmartPrinter z adnotacją

Utwórz plik `src/main/groovy/PrinterDSLWithStringDelegate.groovy`. 
Użyjemy atrybutu includes, aby precyzyjnie wskazać, które pola mają trafić do wyniku.

 
import groovy.transform.Delegate
import groovy.transform.ToString

class SimplePrinter {
void printLowRes(String text) { println "Niska jakość: $text" }
void connect() { println "Połączono z drukarką." }
}

@ToString(includes = ['model']) // AST wygeneruje toString() tylko dla pola model
class SmartPrinter {
String model = "HP Laser 5000"

    @Delegate 
    SimplePrinter basicPrinter = new SimplePrinter()
    
    void printHighRes(String text) { 
        println "Model $model drukuje w 4K: $text" 
    }
}
2. Test Spocka: Weryfikacja wyniku

Dopisz poniższy test do swojej klasy AstSpec.groovy. Sprawdzimy nie tylko obecność modelu, ale też upewnimy się, że delegat (basicPrinter) nie zaśmieca nam wyniku.

code
Groovy
download
content_copy
expand_less
def "powinien wygenerować toString zawierający tylko model drukarki"() {
given: "nowa inteligentna drukarka"
def smartPrinter = new SmartPrinter()

    when: "wywołujemy metodę toString"
    def result = smartPrinter.toString()

    then: "wynik zawiera nazwę modelu"
    result.contains("HP Laser 5000")

    and: "wynik NIE zawiera informacji o delegacie basicPrinter"
    // Domyślnie toString bez parametrów wypisałby wszystkie pola
    !result.contains("basicPrinter")
    !result.contains("SimplePrinter")
    
    and: "wynik ma format oczekiwany przez Groovy"
    // Groovy generuje format: NazwaKlasy(pole1, pole2)
    result == "SmartPrinter(HP Laser 5000)"
}
Dlaczego to jest "Practice" (Dobra praktyka)?

Czytelność Logów: W dużych systemach logowanie całych obiektów jest kluczowe dla debugowania. @ToString gwarantuje, że log będzie zawsze aktualny, nawet jeśli dodasz nowe pola (wystarczy je dopisać do includes).

Unikanie rekurencji: Gdybyś miał dwa obiekty wskazujące na siebie nawzajem (np. User i jego Account), standardowy toString() wpadłby w pętlę i rzucił StackOverflowError. Dzięki includes lub excludes decydujesz, gdzie przerwać to połączenie.

Wydajność: Kod generowany przez AST jest kompilowany bezpośrednio do bajtkodu Javy – działa tak samo szybko, jak gdybyś napisał go ręcznie, ale bez ryzyka pomyłki (np. literówki w nazwie pola).

Co dalej?

Lab 9 za nami. Przetrenowałeś magię na etapie kompilacji. Czas na Lab 10: Dynamiczne Obiekty i Expando.

W Lab 10 dowiesz się:

Jak stworzyć obiekt, któremu możesz dodawać pola i metody "w locie" podczas działania programu (bez definiowania klasy!).

Do czego służy ExpandoMetaClass w testowaniu "niemożliwych" przypadków (np. zmiana zachowania statycznych metod w bibliotekach zewnętrznych).

Czy chcesz zobaczyć, jak stworzyć obiekt-kameleon (Expando)?