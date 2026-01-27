import spock.lang.Specification
import groovyx.gpars.GParsPool

class GparsSpec extends Specification {

    def "powinien znaleźć parzyste liczby i przemnożyć je przez 2 równolegle"() {
        given: "zakres liczb od 1 do 1000"
        def data = (1..1000)

        when: "znajdujemy parzyste liczby i przemnażamy je przez 2 równolegle"
        def result = GParsPool.withPool {
            def evens = data.findAllParallel { it % 2 == 0 }
            evens.collectParallel { it * 2 }
        }

        then: "wynik zawiera poprawną liczbę elementów"
        result.size() == 500

        and: "wszystkie elementy są parzyste i podzielne przez 4"
        result.every { it % 4 == 0 }

        and: "wynik zawiera poprawne wartości (podwojone parzyste liczby)"
        result.contains(4)   // 2 * 2
        result.contains(100) // 50 * 2
        result.contains(2000) // 1000 * 2
    }
}
