import spock.lang.Specification
import spock.lang.Unroll

class RegexDemoSpec extends Specification {

    @Unroll // Rozwija każdy wiersz tabeli jako osobny test w raporcie
    def "Zadanie 1: powinien wyciągnąć numery zamówień z tekstu '#logInput'"() {
        when: "uruchamiamy ekstraktor na podanym tekście"
        def ids = RegexDemo.extractOrders(logInput)

        then: "wynikowa lista zawiera tylko oczekiwane numery ID"
        ids == expectedIds

        where: "zdefiniowane scenariusze testowe"
        logInput                                            || expectedIds
        "System created Order-1234 successfully."           || ["1234"]
        "Error in Order-55. Retrying Order-99... Success."  || ["55", "99"]
        "Order-1, Order-2, Order-3"                         || ["1", "2", "3"]
        "Brak zamówień w tym logu."                         || []
        "Order-ABC (błędny format), Order- (pusty)"         || []
        "Order-100 mixed with Order-200"                    || ["100", "200"]
    }

    class RegexValidationSpec extends Specification {

        @Unroll
        def "Zadanie 2: kod pocztowy '#code' powinien być walidowany jako #expectedResult"() {
            expect: "metoda walidująca zwraca oczekiwany rezultat"
            RegexDemo.isValidPostalCode(code) == expectedResult

            where: "zestaw kodów poprawnych i błędnych"
            code       || expectedResult
            "00-950"   || true
            "12-345"   || true
            "12345"    || false // Błąd: brak myślnika
            "12-34"    || false // Błąd: za krótki drugi człon
            "12-3456"  || false // Błąd: za długi drugi człon
            "AB-CDE"   || false // Błąd: litery zamiast cyfr
            "123-45"   || false // Błąd: zły format (3 cyfry na początku)
            ""         || false // Błąd: pusty ciąg
        }

        @Unroll
        def "Zadanie 2: hasło '#password' (secure: #isSecure) - weryfikacja regex"() {
            expect:
            RegexDemo.isSecurePassword(password) == isSecure

            where: "tabela przypadków brzegowych dla regexa /(?=.*\\d)(?=.*[A-Z]).{8,}/"
            password          || isSecure
            "Haslo123"        || true  // OK: 8 znaków, duża litera, cyfra
            "SuperTajne1!"    || true  // OK: spełnia warunki + znak specjalny
            "12345678"        || false // Błąd: brak dużej litery
            "samoMaleLiter1"  || false // Błąd: brak dużej litery
            "BEZCYFR"         || false // Błąd: brak cyfry
            "Krot1"           || false // Błąd: za krótkie (<8 znaków)
            "HasloBezCyfr"    || false // Błąd: brak cyfry
        }
    }
}