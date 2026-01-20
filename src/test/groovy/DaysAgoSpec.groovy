import spock.lang.Specification
import java.time.LocalDate

class DaysAgoSpec extends Specification {

    // Wykonuje się raz przed wszystkimi testami w tej klasie
    def setupSpec() {
        // Dodajemy nową metodę do klasy Integer
        // 'delegate' w tym miejscu to konkretna liczba, na której wywołujemy metodę
        Integer.metaClass.daysAgo = {
            LocalDate.now().minusDays(delegate)
        }
    }

    def "powinien zwrócić poprawną datę sprzed 7 dni"() {
        given: "oczekiwana data wyliczona standardowo"
        def expectedDate = LocalDate.now().minusDays(7)

        expect: "nasza nowa metoda na klasie Integer zwraca to samo"
        7.daysAgo() == expectedDate
    }

    def "powinien działać dla różnych wartości"() {
        expect:
        0.daysAgo() == LocalDate.now()
        1.daysAgo() == LocalDate.now().minusDays(1)
        365.daysAgo() == LocalDate.now().minusDays(365)
    }
}