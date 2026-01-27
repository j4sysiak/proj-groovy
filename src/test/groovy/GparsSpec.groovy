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