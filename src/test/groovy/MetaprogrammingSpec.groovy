import spock.lang.Specification

class MetaprogrammingSpec extends Specification {

    // Dlaczego to jest ważne?
    // Pozwala to na pisanie kodu, który czyta się jak zdania w języku angielskim.
    // W ten sposób zbudowane są biblioteki do testowania API czy konfiguracje serwerów.

    def setupSpec() {
        // Dodajemy nową metodę do klasy String globalnie!
        String.metaClass.isEmail = {
            delegate.contains("@") && delegate.contains(".")
        }

        // Dodajemy metodę do klasy Integer
        Integer.metaClass.toPln = {
            return "${delegate},00 zł"
        }
    }

    def "powinien używać dynamicznie dodanych metod"() {
        expect:
        "kontakt@google.pl".isEmail() == true
        "zwykly-tekst".isEmail() == false

        and:
        150.toPln() == "150,00 zł"
    }
}