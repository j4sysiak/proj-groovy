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
-------------------------------------

Często musimy pobrać dane z wielu źródeł, a potem je zsumować. W GPars robisz to w bloku withPool.
Oto profesjonalny test w Spocku, który weryfikuje Twoją logikę przetwarzania współbieżnego.
W teście sprawdzimy nie tylko liczbę elementów, ale też ich poprawność matematyczną (skoro filtrujemy liczby parzyste i mnożymy je przez 2, to każdy wynik musi być podzielny przez 4).

Stwórz test:
`src/test/groovy/GparsFunctionalSpec.groovy`

```groovy
import spock.lang.Specification
import groovyx.gpars.GParsPool

class GparsFunctionalSpec extends Specification {

    def "powinien poprawnie przefiltrować i przemnożyć duży zbiór danych równolegle"() {
        given: "zakres danych od 1 do 1000"
        def data = (1..1000)

        when: "uruchamiamy przetwarzanie w puli wątków GPars"
        def result = GParsPool.withPool {
            // 1. Znajdź parzyste (równolegle) -> zostanie 500 liczb: [2, 4, 6... 1000]
            def evens = data.findAllParallel { it % 2 == 0 }
            
            // 2. Przemnóż przez 2 (równolegle) -> [4, 8, 12... 2000]
            return evens.collectParallel { it * 2 }
        }

        then: "rozmiar wynikowej listy to dokładnie 500"
        result.size() == 500

        and: "pierwszy i ostatni element są poprawne"
        result[0] == 4          // (2 * 2)
        result[-1] == 2000      // (1000 * 2)

        and: "każda liczba w wyniku jest podzielna przez 4"
        // Metoda .every { ... } sprawdza czy warunek jest spełniony dla wszystkich elementów
        result.every { it % 4 == 0 }
    }

    def "powinien udowodnić, że kolejność jest zachowana mimo pracy równoległej"() {
        given: "krótka lista"
        def input = [10, 20, 30, 40, 50]

        when:
        def output = GParsPool.withPool {
            input.collectParallel { it + 1 }
        }

        then: "mimo że wątki mogły skończyć pracę w różnym czasie, lista końcowa jest posortowana jak wejściowa"
        output == [11, 21, 31, 41, 51]
    }
}
```

Dlaczego ten test jest ważny (Analiza):
Metoda every { ... }: To potężne narzędzie Groovy'ego do testowania kolekcji. 
Zamiast pisać pętlę, sprawdzamy jednym poleceniem, czy wszystkie 500 wyników spełnia nasze założenie biznesowe 
(podzielność przez 4).

Determinizm: 
Testujemy, czy collectParallel zachowuje kolejność. 
To kluczowe – w wielu bibliotekach wielowątkowych (jak np. niektóre implementacje w innych językach) 
wyniki mogą wrócić w losowej kolejności (kto pierwszy skończy, ten pierwszy na liście). 
GPars gwarantuje zachowanie kolejności, co sprawdzamy w drugim teście.

Horyzontalne skalowanie: 
Jeśli uruchomisz ten test na procesorze z 2 rdzeniami, a potem na procesorze z 16 rdzeniami, 
test zawsze przejdzie, ale GPars automatycznie wykryje sprzęt i użyje odpowiedniej liczby wątków.

Przypomnienie:
--------------
Upewnij się, że w build.gradle masz:
implementation 'org.codehaus.gpars:gpars:1.2.1'


Zadanie 3: Test Spocka (Weryfikacja poprawności)
------------------------------------------------
Największym ryzykiem wielowątkowości jest to, że wyniki wrócą w losowej kolejności lub błędne. 
Sprawdźmy, czy collectParallel zachowuje się deterministycznie.

Stwórz test:
`src/test/groovy/GparsSpec.groovy`

`````groovy
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
`````

Uruchom testy (Run GparsSpec) i upewnij się, że wszystko przechodzi.
Dlaczego GPars jest lepszy niż Stream.parallel() w Javie?

Transparentność: 
Metody nazywają się tak samo (collect -> collectParallel), więc łatwo przerobić stary kod.

Zarządzanie pulą: 
Blok withPool { ... } dba o to, żeby wątki zostały zamknięte po zakończeniu pracy. W Javie często zapominamy zamknąć ExecutorService, co blokuje aplikację.

Wydajność w IO: 
Groovy świetnie radzi sobie z zadaniami typu "pobierz 50 stron internetowych naraz".

Podsumowanie
------------
`GPars` to potężne narzędzie do wielowątkowości w Groovy, które upraszcza kod i zwiększa wydajność. 
Dzięki prostym metodom jak collectParallel czy findAllParallel możesz łatwo przetwarzać duże zbiory danych równolegle, bez konieczności ręcznego zarządzania wątkami.   
