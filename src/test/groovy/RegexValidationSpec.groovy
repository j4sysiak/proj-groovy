import spock.lang.Specification
import spock.lang.Unroll

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
        "samocaleliter1"  || false // Błąd: brak dużej litery
        "BEZCYFR"         || false // Błąd: brak cyfry
        "Krot1"           || false // Błąd: za krótkie (<8 znaków)
        "HasloBezCyfr"    || false // Błąd: brak cyfry
    }
}