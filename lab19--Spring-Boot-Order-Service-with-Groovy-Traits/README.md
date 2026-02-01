lab19--Spring-Boot-Order-Service-with-Groovy-Traits
---------------------------------------------------

Tutaj nie ma już "zabawek". To jest kod, jaki pisze się w komercyjnych projektach:

1. Spring Boot zarządza cyklem życia obiektów (Beans).
2. Groovy dostarcza zwięzłą logikę biznesową i Traity.
3. Spock testuje całość integracyjnie (podnosząc kontekst Springa).

Zbudujmy ten system importu zamówień.

Krok 1: Trait do audytu (AuditLog.groovy)
-----------------------------------------

Stwórz plik `src/main/groovy/AuditLog.groovy`
Ten Trait będzie "doklejany" do każdego serwisu, który potrzebuje zostawiać ślad w historii.

```groovy
trait AuditLog {
// Wewnętrzna lista logów (w prawdziwej aplikacji to byłaby baza danych)
List<String> history = []

    void audit(String message) {
        String entry = "[AUDIT] ${java.time.LocalTime.now()} - $message"
        println entry // Wypisz na konsolę
        history << entry // Dodaj do listy (żebyśmy mogli to przetestować)
    }

    // Metoda pomocnicza dla testów - sprawdza czy dany wpis istnieje
    boolean hasAuditEntry(String fragment) {
        return history.any { it.contains(fragment) }
    }
}
```


Krok 2: Serwis Biznesowy (OrderProcessingService.groovy)
----------------------------------------------

Stwórz plik `src/main/groovy/OrderProcessingService.groovy`
To jest serce aplikacji. 
Używamy adnotacji `@Service` ze Springa, aby stał się Beanem, oraz implementujemy Trait (interface javovy) AuditLog.

```groovy
import org.springframework.stereotype.Service

@Service
class OrderProcessingService implements AuditLog {

    /**
     * Logika:
     * 1. Loguje start.
     * 2. Odrzuca zamówienia poniżej 100 PLN.
     * 3. Sumuje resztę.
     * 4. Loguje wynik.
     */
    BigDecimal processOrders(List<Map> orders) {
        audit("Rozpoczynam przetwarzanie ${orders.size()} zamówień.")

        // Groovy Magic: findAll zwraca nową listę spełniającą warunek
        def validOrders = orders.findAll { it.amount >= 100 }
        
        if (validOrders.size() < orders.size()) {
            audit("Odrzucono ${orders.size() - validOrders.size()} zamówień (kwota < 100).")
        }

        // Groovy Magic: *.amount (spread operator) wyciąga kwoty, .sum() je dodaje
        // Operator Elvis (?: 0) zabezpiecza przed nullem (gdy lista pusta)
        BigDecimal total = validOrders*.amount.sum() ?: 0

        audit("Zakończono. Łączna wartość: $total")
        
        return total
    }
}
```

Krok 3: Test Integracyjny (OrderIntegrationSpec.groovy)

Stwórz plik src/test/groovy/OrderIntegrationSpec.groovy.
To jest test "Heavyweight". Podnosimy Springa, wstrzykujemy prawdziwy serwis i sprawdzamy, czy logika + trait działają razem.

code
Groovy
download
content_copy
expand_less
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

// Podnosimy kontekst Springa tylko z naszym serwisem (szybki start)
@SpringBootTest(classes = OrderService)
class OrderIntegrationSpec extends Specification {

    @Autowired
    OrderService orderService

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
Twoje Zadanie:

Uruchom test OrderIntegrationSpec.

Obserwuj konsolę – powinieneś zobaczyć logi Springa (startowanie kontekstu), a potem Twoje logi [AUDIT].

Jeśli zobaczysz zielony pasek, to oznacza, że właśnie stworzyłeś kompletną, nowoczesną, testowalną architekturę przy użyciu najlepszych praktyk Java i Groovy.

Czekam na wieści, czy Finał zaliczony! 🏆