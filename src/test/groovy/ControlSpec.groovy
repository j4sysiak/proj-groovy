import spock.lang.Timeout
import java.util.concurrent.TimeUnit
import spock.lang.Specification


class ControlSpec extends Specification {

    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    def "ten test zawiedzie jeśli będzie trwał dłużej niż sekundę"() {
        when:
        Thread.sleep(500) // Zmień na 1500 żeby zobaczyć błąd

        then:
        true
    }

    @spock.lang.Ignore("Nieaktywne dopóki nie naprawimy błędu w API")
    def "ten test zostanie pominięty"() {
        expect:
        false
    }
}