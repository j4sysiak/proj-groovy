import spock.lang.Specification

// deklarujesz usługę, która zwraca kurs jako BigDecimal.
interface RateService {
    BigDecimal getRate(String currency)
}

class DiscountService {
    RateService rateService

    BigDecimal calculatePrice(BigDecimal pricePln) {
        // Pobierz kurs z rateService (BigDecimal) i podziel w BigDecimal
        def rate = rateService.getRate("USD")
        // Użyj domyślnego skalowania z MathContext.DECIMAL64 dla bezpiecznych obliczeń
        return pricePln.divide(rate, java.math.MathContext.DECIMAL64)
    }
}

class DiscountSpec extends Specification {

    def "powinien obliczyć cenę w USD używając mocka"() {
        given: "mock usługi pobierania kursów"
        def rateServiceMock = Mock(RateService)
        def service = new DiscountService(rateService: rateServiceMock)

        when: "wyliczamy cenę"
        def result = service.calculatePrice(100.0G)

        then: "oczekujemy, że mock zwróci kurs 4.0"
        1 * rateServiceMock.getRate("USD") >> 4.0G

        and: "wynik powinien być poprawny"
        result == 25.0G
    }
}