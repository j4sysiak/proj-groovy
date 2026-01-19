import spock.lang.Specification
import spock.lang.Unroll

class PasswordSpec extends Specification {

    @Unroll // Powoduje, że każdy wiersz tabeli będzie widoczny jako osobny test w IntelliJ
    def "hasło '#password' powinno mieć status valid = #expectedResult"() {
        expect: "sprawdzenie długości hasła"
        password.length() >= 8 == expectedResult

        where: "tabela z danymi testowymi"
        password      || expectedResult
        "123"         || false
        "1234567"     || false
        "12345678"    || true
        "tajneHaslo"  || true
        ""            || false
    }
}