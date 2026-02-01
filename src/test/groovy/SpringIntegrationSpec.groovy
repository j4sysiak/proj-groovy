import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

// W prawdziwym projekcie Spring Boot wystarczy @SpringBootTest
// Tu robimy symulację kontekstu
@ContextConfiguration(classes = [CurrencyService])
class SpringIntegrationSpec extends Specification {

    @Autowired
    CurrencyService currencyService

    def "powinien wstrzyknąć beana Springa do testu Spocka"() {
        expect: "serwis został wstrzyknięty (nie jest null)"
        currencyService != null

        and: "metoda działa"
        currencyService.getCurrency() == "PLN"
    }
}