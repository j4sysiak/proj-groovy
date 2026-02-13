import spock.lang.Specification

class RuleEngineSpec extends Specification {

    def "powinien obliczyć zniżkę na podstawie dynamicznego skryptu"() {
        given: "skrypt z logiką biznesową (np. z bazy danych)"
        def script = """
            if (amount > 1000) {
                return amount * 0.10 // 10% zniżki
            } else {
                return amount * 0.05 // 5% zniżki
            }
        """

        and: "kontekst z danymi"
        def context = [amount: 2000]

        when:
        def discount = DiscountEngine.applyRule(script, context)

        then:
        discount == 200.0
    }

    def "bezpieczny silnik powinien zablokować próbę wyłączenia systemu"() {
        given: "złośliwy skrypt"
        def maliciousScript = "System.exit(0)"

        when:
        SecureDiscountEngine.applySecureRule(maliciousScript, [amount: 100])

        then: "powinien zostać rzucony błąd bezpieczeństwa"
        // Groovy rzuci SecurityException lub błąd kompilacji
        thrown(Exception)
    }
}