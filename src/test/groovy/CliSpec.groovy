import spock.lang.Specification
import groovy.cli.picocli.CliBuilder

class CliSpec extends Specification {

    def "powinien poprawnie sparsować wymagane parametry"() {
        given: "definicja buildera przy użyciu bloku 'with'"
        def cli = new CliBuilder()
        cli.with {
            // Definiujemy opcje wewnątrz bloku - to jest bezpieczne i czytelne
            f(longOpt: 'file', args: 1, required: true, 'Ścieżka do pliku')
            k(longOpt: 'keyword', args: 1, 'Słowo kluczowe')
        }

        when: "symulujemy wywołanie z terminala"
        def opts = cli.parse(['-f', 'test.log', '-k', 'ERROR'] as String[])

        then: "parametry są dostępne w obiekcie opts"
        opts != null
        opts.f == 'test.log'
        opts.k == 'ERROR'
    }

    def "powinien rozpoznać flagi typu boolean"() {
        given:
        def cli = new CliBuilder()
        cli.with {
            d(longOpt: 'dry-run', 'Tryb testowy')
            v(longOpt: 'verbose', 'Szczegółowe logi')
        }

        when: "podajemy tylko flagę -v"
        def opts = cli.parse(['-v'] as String[])

        then:
        opts.v == true
        opts.d == false // flaga nie została podana, więc jest false
    }
}