import spock.lang.Specification

class DslSpec extends Specification {

    def "powinien utworzyć zamówienie używając czytelnego DSL"() {
        when: "używamy naszego nowego języka do konfiguracji zamówienia"
        def myOrder = OrderBuilder.make {
            client "Jan Kowalski"
            item "Książka: Groovy in Action", 150.0
            item "Kawa", 15.5
            item "Myszka bezprzewodowa", 80.0
        }

        then: "obiekt FinalOrder został poprawnie wypełniony"
        myOrder.customerName == "Jan Kowalski"
        myOrder.items.size() == 3

        and: "suma obliczona przez POGO jest poprawna"
        myOrder.total == 245.5

        and: "toString wygląda czytelnie"
        println myOrder
        myOrder.toString().contains("Jan Kowalski")
    }
}