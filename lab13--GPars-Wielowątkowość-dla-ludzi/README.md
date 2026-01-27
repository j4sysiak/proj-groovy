Wielowątkowość w Javie (nawet z CompletableFuture czy Stream.parallel()) bywa skomplikowana. 
W Groovy mamy bibliotekę GPars (Groovy Parallel Systems), 
która pozwala zamienić zwykłą pętlę w potwora wydajności jednym słowem kluczowym.

Bonus Lab 13: GPars – Wielowątkowość dla ludzi
----------------------------------------------

Cel: 
Zobaczysz, jak przetworzyć kolekcję równolegle (wykorzystując wszystkie rdzenie procesora) i zredukować czas wykonywania ciężkich 
operacji, nie martwiąc się o tworzenie wątków.

Krok 0: Dodanie zależności (build.gradle)
-----------------------------------------

GPars nie jest domyślnie w "core", więc musimy go dodać. 
Otwórz `build.gradle`, dodaj linię w dependencies i odśwież projekt (ikonka słonia):

``````
dependencies {
// ... twoje inne zależności
implementation 'org.codehaus.gpars:gpars:1.2.1'
}
``````

Zadanie 1: Wyścig – Serial vs Parallel
--------------------------------------

Stwórzmy symulację ciężkiej pracy (np. pobieranie danych z sieci, symulowane przez sleep). Zobaczymy różnicę w czasie.

Stwórz plik `src/main/groovy/ConcurrencyDemo.groovy`:

````groovy
import groovyx.gpars.GParsPool

class ConcurrencyDemo {
static void main(String[] args) {
// Lista liczb do przetworzenia
def numbers = (1..10).toList()

        // Definicja "ciężkiej" operacji (trwa 1 sekundę)
        def heavyCalculation = { num ->
            sleep(1000) // Symulacja opóźnienia
            return num * 10
        }

        println "--- START: Przetwarzanie sekwencyjne (zwykłe collect) ---"
        long start = System.currentTimeMillis()
        
        // Zwykłe Groovy: leci jeden po drugim (10 * 1s = ok. 10s)
        def resultSeq = numbers.collect(heavyCalculation)
        
        long end = System.currentTimeMillis()
        println "Wynik: $resultSeq"
        println "Czas sekwencyjny: ${(end - start)} ms"


        println "\n--- START: Przetwarzanie równoległe (GPars) ---"
        start = System.currentTimeMillis()

        // GPars Magic: Tworzymy pulę wątków i używamy metody collectParallel
        GParsPool.withPool {
            // collectParallel automatycznie dzieli pracę na rdzenie procesora
            def resultPar = numbers.collectParallel(heavyCalculation)
            println "Wynik wewnątrz puli: $resultPar"
        }

        end = System.currentTimeMillis()
        println "Czas równoległy: ${(end - start)} ms"
    }
}
````

Uruchom to (Run ConcurrencyDemo.main()) i obserwuj konsolę.

Sekwencyjnie powinno zająć ok. 10 000 ms (10 sekund).

Równolegle? Zależnie od Twojego procesora, pewnie ok. 1000 - 2000 ms.

Zadanie 2: Map / Reduce w wersji Turbo

Często musimy pobrać dane z wielu źródeł, a potem je zsumować. W GPars robisz to w bloku withPool.

Dodaj metodę do klasy (lub stwórz test, patrz Zadanie 3):

code
Groovy
download
content_copy
expand_less
// Przykład użycia findAllParallel i operacji współbieżnych
GParsPool.withPool {
def data = (1..1000)

    // Znajdź parzyste liczby równolegle
    def evens = data.findAllParallel { it % 2 == 0 }
    
    // Przemnóż każdą przez 2 równolegle
    def doubled = evens.collectParallel { it * 2 }
    
    println "Liczba elementów: ${doubled.size()}" // Powinno być 500
}
Zadanie 3: Test Spocka (Weryfikacja poprawności)

Największym ryzykiem wielowątkowości jest to, że wyniki wrócą w losowej kolejności lub błędne. Sprawdźmy, czy collectParallel zachowuje się deterministycznie.

Stwórz src/test/groovy/GparsSpec.groovy:

code
Groovy
download
content_copy
expand_less
import spock.lang.Specification
import groovyx.gpars.GParsPool

class GparsSpec extends Specification {

    def "powinien przetworzyć listę równolegle i zachować kolejność"() {
        given: "lista wejściowa"
        def input = [1, 2, 3, 4, 5]

        when: "przetwarzamy równolegle (kwadrat liczby)"
        def result = GParsPool.withPool {
            return input.collectParallel { it * it }
        }

        then: "wynik jest poprawny matematycznie"
        result == [1, 4, 9, 16, 25]

        and: "rozmiar listy jest ten sam"
        result.size() == input.size()
    }

    def "powinien znaleźć elementy równolegle (findAllParallel)"() {
        given:
        def names = ["Anna", "Jan", "Adam", "Ewa", "Zenon"]

        when:
        def aNames = GParsPool.withPool {
            names.findAllParallel { it.startsWith("A") }
        }

        then:
        aNames.size() == 2
        aNames.containsAll(["Anna", "Adam"])
    }
}
Dlaczego GPars jest lepszy niż Stream.parallel() w Javie?

Transparentność: Metody nazywają się tak samo (collect -> collectParallel), więc łatwo przerobić stary kod.

Zarządzanie pulą: Blok withPool { ... } dba o to, żeby wątki zostały zamknięte po zakończeniu pracy. W Javie często zapominamy zamknąć ExecutorService, co blokuje aplikację.

Wydajność w IO: Groovy świetnie radzi sobie z zadaniami typu "pobierz 50 stron internetowych naraz".

Finał Kursu

To był Bonus Lab. Teraz posiadasz wiedzę, która stawia Cię w czołówce programistów JVM, jeśli chodzi o znajomość ekosystemu Groovy.

Dziękuję za wytrwałość!
Jeśli kiedykolwiek będziesz potrzebował szybkiego skryptu, testu czy narzędzia – wiesz już, że Java jest świetna, ale Groovy jest szybszy (w pisaniu).

Powodzenia w kodowaniu! 🚀