import spock.lang.Specification

class DslSpec extends Specification {

    def "powinien utworzyć zamówienie używając czytelnego DSL"() {
        when: "używamy naszego nowego języka do konfiguracji zamówienia"
        def myOrder = OrderBuilder.make {
            client "Jan Kowalski"
            item ("Książka: Groovy in Action", 150.0) // Możemy użyć nawiasów, ale nie musimy
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

    def "powinien utworzyć zamówienie z zagnieżdżonymi kategoriami"() {
        when:
        def myOrder = OrderBuilder.make {
            client "Anna Nowak"

            category("Elektronika") {
                item "Telefon", 2000
                item "Etui", 50
            }

            category("Dom") {
                item "Lampa", 120
            }

            item "Torba na zakupy", 5 // To wpadnie do kategorii domyślnej ("Ogólna")
        }

        then: "sprawdzamy poprawność przypisania kategorii"
        myOrder.customerName == "Anna Nowak"
        myOrder.items.size() == 4

        and: "produkty z elektroniki mają poprawną kategorię"
        def elektronika = myOrder.items.findAll { it.category == "Elektronika" }
        elektronika.size() == 2
        elektronika.name.containsAll(["Telefon", "Etui"])

        and: "produkt poza blokiem ma kategorię domyślną"
        myOrder.items.find { it.name == "Torba na zakupy" }.category == "Ogólna"

        and: "całkowity koszt się zgadza"
        myOrder.total == 2175
    }
}