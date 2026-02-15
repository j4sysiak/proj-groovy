Lab 23: Wydajność i Statyczna Kompilacja
----------------------------------------

Cel: 
Zrozumienie różnicy między `Dynamic Groovy` a `Static Groovy`. 
Dowiesz się, jak adnotacja `@CompileStatic` zmienia bajtkod na identyczny z Javą, eliminując narzut "magii" Groovy'ego.

Zadanie 1: Porównanie wydajności (Benchmark)
--------------------------------------------

Stworzymy algorytm liczący sumę liczb w pętli. 
Jeden będzie dynamiczny, drugi statyczny.

Stwórz plik `src/main/groovy/PerformanceService.groovy`

```groovy
import groovy.transform.CompileStatic

class PerformanceService {

    // Metoda DYNAMICZNA (domyślna w Groovy)
    // Każde wywołanie "+" przechodzi przez MetaClass (wolniejsze)
    def calculateDynamic(int iterations) {
        def sum = 0
        for (int i = 0; i < iterations; i++) {
            sum += i
        }
        return sum
    }

    // Metoda STATYCZNA
    // Kompilator generuje bajtkod identyczny z Javą (szybkie)
    @CompileStatic
    long calculateStatic(int iterations) {
        long sum = 0
        for (int i = 0; i < iterations; i++) {
            sum += i
        }
        return sum
    }
}
```


Zadanie 2: Test Spocka z Mikrobenchmarkiem
------------------------------------------

Stwórz `src/test/groovy/PerformanceSpec.groovy`
Sprawdzimy nie tylko czy wyniki są zgodne, ale też jak duża jest różnica w czasie.

```groovy
import spock.lang.Specification
import spock.lang.Shared

class PerformanceSpec extends Specification {

    @Shared def service = new PerformanceService()
    @Shared int iterations = 10_000_000 // 10 milionów operacji

    def "powinien pokazać, że @CompileStatic jest znacznie szybsze"() {
        when: "mierzymy czas dla metody dynamicznej"
        long startDyn = System.currentTimeMillis()
        service.calculateDynamic(iterations)
        long durationDyn = System.currentTimeMillis() - startDyn
        println ">>> Dynamic duration: ${durationDyn}ms"

        and: "mierzymy czas dla metody statycznej"
        long startStat = System.currentTimeMillis()
        service.calculateStatic(iterations)
        long durationStat = System.currentTimeMillis() - startStat
        println ">>> Static duration: ${durationStat}ms"

        then: "metoda statyczna powinna być co najmniej kilka razy szybsza"
        // W zależności od maszyny, różnica może być 5-krotna lub większa
        durationStat < durationDyn
        
        and: "wyniki są matematycznie poprawne"
        service.calculateStatic(10) == 45
    }
}
```


Dlaczego to jest ważne (The Practice)?

Mieszanie stylów: 
W Groovy możesz mieć klasę, w której 90% metod jest dynamicznych (dla wygody), a jedna, krytyczna wydajnościowo metoda, ma `@CompileStatic`.

Type Checking: 
`@CompileStatic` (lub lżejsza wersja `@TypeChecked`) sprawia, że kompilator wyłapie błędy typów (np. literówkę w nazwie metody) już na etapie kompilacji, 
a nie w trakcie działania programu.

Współpraca z Javą: 
Jeśli piszesz kod w Groovy, który ma być wołany miliony razy z poziomu Javy, zawsze używaj `@CompileStatic`.



Wyzwanie Finałowe Lab 23:
------------------------

Dodaj do PerformanceService metodę `processStrings(List<String> list)`.
Metoda ma łączyć wszystkie napisy z listy w jeden długi String, ale...
Użyj `@CompileStatic` oraz standardowego javowego `StringBuilder`, aby operacja była maksymalnie wydajna.
Napisz test w Spocku, który sprawdzi, czy dla listy ['A', 'B', 'C'] metoda zwróci "ABC".


Rozwiązanie Wyzwania Finałowego Lab 23.
---------------------------------------

To zadanie pozwoli Ci zobaczyć, jak Groovy potrafi stać się "przeźroczysty" i generować bajtkod o wydajności identycznej z czystą Javą, 
przy zachowaniu elegancji języka.

 

1. Rozwiązanie: Klasa PerformanceService.groovy
-----------------------------------------------

Dopisujemy metodę processStrings. 
Zastosowanie `@CompileStatic` sprawi, że wywołania metody append na `StringBuilder` zostaną skompilowane bezpośrednio (statycznie), 
bez użycia mechanizmu `Dynamic Dispatch` (MetaClass).

```groovy
import groovy.transform.CompileStatic

class PerformanceService {

    // ... poprzednie metody (calculateDynamic i calculateStatic) ...

    /**
     * Zadanie Finałowe: Wydajne łączenie stringów.
     * Dzięki @CompileStatic, Groovy wygeneruje bajtkod identyczny z Javą.
     */
    @CompileStatic
    String processStrings(List<String> list) {
        if (list == null) return ""
        
        // Używamy standardowego javowego StringBuilder dla max wydajności
        StringBuilder sb = new StringBuilder()
        
        // Tradycyjna pętla for-in w trybie statycznym jest bardzo szybka
        for (String s : list) {
            sb.append(s)
        }
        
        return sb.toString()
    }
}
```

2. Test Spock: Weryfikacja Poprawności
--------------------------------------

Dopisz poniższy test do pliku `src/test/groovy/PerformanceSpec.groovy`.
Sprawdzimy nie tylko czy łączy napisy, ale też czy radzi sobie z przypadkami brzegowymi.

```groovy
def "powinien wydajnie połączyć listę stringów używając StringBuilder i @CompileStatic"() {
given: "lista znaków do połączenia"
def input = ['A', 'B', 'C', 'D', 'E']

        when: "wywołujemy metodę statyczną"
        def result = service.processStrings(input)

        then: "wynik jest poprawnym ciągiem znaków"
        result == "ABCDE"

        and: "metoda radzi sobie z pustą listą lub nullem"
        service.processStrings([]) == ""
        service.processStrings(null) == ""
    }

    def "powinien udowodnić poprawność wyniku dla długiej listy"() {
        given: "lista 1000 powtórzeń litery 'X'"
        def input = (1..1000).collect { "X" }

        when:
        def result = service.processStrings(input)

        then:
        result.length() == 1000
        result.startsWith("XXX")
    }

```

Dlaczego to jest "Ekspercka Praktyka" (The Practice)?

Bytecode Identical to Java: 
Gdybyś zdekompilował tę metodę do czystej Javy, zobaczyłbyś dokładnie taki sam kod, jaki napisałbyś ręcznie w Javie. 
Nie ma tu żadnego narzutu "runtime" Groovy'ego.

Unikanie String Concatenation w pętli: 
W Groovy (podobnie jak w Javie) zapis str += s wewnątrz pętli tworzy za każdym razem nowy obiekt String. 
Przy 10 000 iteracji zabije to wydajność. Użycie StringBuilder w połączeniu z @CompileStatic to najszybszy możliwy sposób łączenia tekstów na maszynie JVM.

Bezpieczeństwo Typów: 
Gdybyś wewnątrz metody oznaczonej `@CompileStatic` próbował wywołać nieistniejącą metodę (np. sb.add(s) zamiast append), 
kompilator zgłosi błąd zanim w ogóle uruchomisz test. 
W dynamicznym Groovym błąd wystąpiłby dopiero podczas działania programu.

