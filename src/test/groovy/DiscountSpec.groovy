import spock.lang.Specification

// deklarujesz usługę, która zwraca kurs jako BigDecimal.
interface RateService {
    BigDecimal getRate(String currency)
}

class DiscountService {
    RateService rateService

    double calculatePrice(double pricePln) {
        def rate = rateService.getRate("USD")
        return pricePln / rate
    }
}

class DiscountSpec extends Specification {
    // Test Spocka (DiscountSpec) robi to tak:
    //    given: — tworzysz mocka rateServiceMock i obiekt serwisu z wstrzykniętym mockiem (new DiscountService(rateService: rateServiceMock)).
    //    when: — wywołujesz service.calculatePrice(100.0).
    //    then: — deklarujesz oczekiwaną interakcję i stubbing:
    //        1 * rateServiceMock.getRate("USD") >> 4.0 oznacza „oczekuj jeden wywołania getRate("USD") i zwróć 4.0”.
    //        Spock wykonuje najpierw kod z when, potem w then weryfikuje interakcje i stosuje stub.
    //     and: — sprawdzenie wyniku result == 25.0.

    def "powinien obliczyć cenę w USD używając mocka"() {
        given: "mock usługi pobierania kursów"
        def rateServiceMock = Mock(RateService)
        def service = new DiscountService(rateService: rateServiceMock)

        when: "wyliczamy cenę"
        def result = service.calculatePrice(100.0)

        then: "oczekujemy, że mock zwróci kurs 4.0" // i tylko raz wejdzie do metody getRate  1 * rateServiceMock.getRate("USD")
        1 * rateServiceMock.getRate("USD") >> 4.0G // ">>" oznacza: zwróć tę wartość

        and: "wynik powinien być poprawny"
        result == 25.0
    }
}