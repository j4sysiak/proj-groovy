import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

// Podnosimy kontekst Springa tylko z naszym serwisem (szybki start)
//@SpringBootTest(classes = OrderProcessingService)
@ContextConfiguration(classes = [OrderProcessingService]) // Wymuszamy załadowanie tej konkretnej klasy
class OrderIntegrationSpec extends Specification {

    @Autowired
    OrderProcessingService orderService

    def "powinien przetworzyć zamówienia, odrzucić małe kwoty i zostawić ślad w audycie"() {
        given: "lista zamówień (Mapy udające obiekty DTO)"
        def orders = [
                [id: 1, amount: 50],   // Odrzucone (< 100)
                [id: 2, amount: 200],  // OK
                [id: 3, amount: 300],  // OK
                [id: 4, amount: 20]    // Odrzucone (< 100)
        ]

        when: "uruchamiamy logikę biznesową"
        BigDecimal result = orderService.processOrders(orders)

        then: "suma obejmuje tylko zamówienia > 100 (czyli 200 + 300)"
        result == 500

        and: "Trait AuditLog zadziałał poprawnie (sprawdzamy historię)"
        // Dzięki 'implements AuditLog' serwis ma metodę hasAuditEntry!
        orderService.hasAuditEntry("Rozpoczynam przetwarzanie 4 zamówień")
        orderService.hasAuditEntry("Odrzucono 2 zamówień")
        orderService.hasAuditEntry("Łączna wartość: 500")

        and: "nie ma wpisów o błędach"
        !orderService.hasAuditEntry("ERROR")
    }

    def "powinien obsłużyć pustą listę bez błędu"() {
        when:
        def result = orderService.processOrders([])

        then:
        result == 0
        orderService.hasAuditEntry("Łączna wartość: 0")
    }
}