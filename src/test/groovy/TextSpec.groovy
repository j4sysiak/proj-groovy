import spock.lang.Specification

class TextSpec extends Specification {

    def "powinien wyciągnąć numery zamówień z brudnego logu"() {
        given:
        def log = "INFO: User logged in. Created Order-5521. Error in Order-99. Retry Order-101 success."

        when:
        def ids = RegexDemo.extractOrders(log)

        then:
        ids.size() == 3
        ids == ["5521", "99", "101"]
    }

    def "powinien walidować kody pocztowe operatorem match (==~)"() {
        expect:
        RegexDemo.isValidPostalCode(code) == result

        where:
        code      | result
        "00-950"  | true
        "00950"   | false  // brak myślnika
        "A1-200"  | false  // litera zamiast cyfry
        "12-3456" | false  // za długi
    }

    def "powinien wygenerować e-mail z listy produktów"() {
        when:
        def email = TemplateGenerator.generateWelcomeEmail("Jan", ["Laptop", "Myszka"])

        then:
        println email // Zobacz w konsoli jak ładnie wygląda!

        email.contains("Witaj Jan!")
        email.contains("- Laptop")
        email.contains("- Myszka")
        !email.contains("<%") // Upewniamy się, że kod szablonu zniknął
    }
}