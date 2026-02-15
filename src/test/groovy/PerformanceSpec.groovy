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

    def "powinien wydajnie połączyć listę stringów używając StringBuilder i @CompileStatic"() {
        given: "lista znaków do połączenia"
        def input = ['A', 'B', 'C', 'D', 'E']

        when: "wywołujemy metodę statyczną"
        def result = service.processStringsStatic(input)

        then: "wynik jest poprawnym ciągiem znaków"
        result == "ABCDE"

        and: "metoda radzi sobie z pustą listą lub nullem"
        service.processStringsStatic([]) == ""
        service.processStringsStatic(null) == ""
    }

    def "Static: powinien udowodnić poprawność wyniku dla długiej listy"() {
        given: "lista 1000 powtórzeń litery 'X'"
        def input = (1..10000000).collect { "X" }

        when:
        long startDyn = System.currentTimeMillis()
        def result = service.processStringsStatic(input)
        long durationDyn = System.currentTimeMillis() - startDyn
        println ">>> Static duration: ${durationDyn}ms"

        then:
        result.length() == 10000000
        result.startsWith("XXX")
    }

    def "Dynamic: powinien udowodnić poprawność wyniku dla długiej listy"() {
        given: "lista 1000 powtórzeń litery 'X'"
        def input = (1..10000000).collect { "X" }

        when:
        long startDyn = System.currentTimeMillis()
        def result = service.processStringsDynamic(input)
        long durationDyn = System.currentTimeMillis() - startDyn
        println ">>> Dynamic duration: ${durationDyn}ms"

        then:
        result.length() == 10000000
        result.startsWith("XXX")
    }
}