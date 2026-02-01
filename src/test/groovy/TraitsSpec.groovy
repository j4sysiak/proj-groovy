import spock.lang.Specification

class TraitsSpec extends Specification {
    def "UserHandler powinien mieć metody z obu Traitów"() {
        given:
        def handler = new TraitsDemo.UserHandler()

        when: "próbujemy zarejestrować błędnego użytkownika"
        handler.register("Jo")

        then: "rzuca wyjątek walidacji (z Validatable)"
        def ex = thrown(RuntimeException)
        ex.message.contains("Nazwa za krótka")

        and: "pole stanu z Traitu zostało zmienione"
        handler.isValid == false
    }
}