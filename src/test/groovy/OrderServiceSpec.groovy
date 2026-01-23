import spock.lang.Specification

class OrderServiceSpec extends Specification {

    // Tworzymy Mocka, który będzie udawał dostawcę płatności
    def paymentProviderMock = Mock(PaymentProvider)

    // Wstrzykujemy mocka do serwisu
    def orderService = new OrderService(paymentProvider: paymentProviderMock)

    def "powinien poprawnie przetworzyć zamówienie i wywołać płatność"() {
        when: "procesujemy poprawne zamówienie"
        orderService.processOrder(50)

        then: "metoda pay na mocku musi być wywołana dokładnie 1 raz z parametrem 50"
        1 * paymentProviderMock.pay(50)
    }

    def "powinien rzucić wyjątek i NIE wywoływać płatności dla ujemnej kwoty"() {
        when: "próbujemy zapłacić ujemną kwotę"
        orderService.processOrder(-10)

        then: "spodziewamy się konkretnego wyjątku"
        def ex = thrown(InvalidOrderException)
        ex.message == "Kwota musi być dodatnia!"

        and: "metoda pay na mocku NIE może być wywołana ani razu (0 *)"
        // '_' oznacza dowolny parametr
        0 * paymentProviderMock.pay(_)
    }
}