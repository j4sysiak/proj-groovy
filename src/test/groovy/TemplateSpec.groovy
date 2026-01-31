import spock.lang.Specification

class TemplateSpec extends Specification {

    def "powinien wygenerować spersonalizowany e-mail używając SimpleTemplateEngine"() {
        given: "dane wejściowe do szablonu"
        def userName = "Krzysztof"
        def productList = ["Kawa", "Mleko", "Ciasteczka"]

        when: "generujemy treść maila"
        String emailContent = TemplateGenerator.generateWelcomeEmail(userName, productList)

        // Wypiszmy wynik na konsolę, żebyś zobaczył efekt końcowy!
        println "--- WYGENEROWANY EMAIL ---\n$emailContent\n--------------------------"

        then: "treść zawiera powitanie z imieniem"
        emailContent.contains("Witaj Krzysztof!")

        and: "treść zawiera wylistowane produkty (efekt pętli w szablonie)"
        emailContent.contains("- Kawa")
        emailContent.contains("- Mleko")
        emailContent.contains("- Ciasteczka")

        and: "treść zawiera poprawnie obliczoną sumę"
        emailContent.contains("Suma pozycji: 3")

        and: "w wynikowym tekście NIE MA już surowych znaczników szablonu"
        !emailContent.contains("<%")
        !emailContent.contains('${')
    }

    def "powinien poprawnie obsłużyć pustą listę produktów"() {
        given:
        def user = "Anna"
        def emptyList = []

        when:
        def email = TemplateGenerator.generateWelcomeEmail(user, emptyList)

        then:
        email.contains("Witaj Anna!")
        email.contains("Suma pozycji: 0")
        !email.contains("- ") // Nie powinno być żadnych myślników z listy
    }
}