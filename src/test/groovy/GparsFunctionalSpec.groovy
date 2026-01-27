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