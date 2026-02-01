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