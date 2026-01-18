import spock.lang.Specification

class UserSpec extends Specification {

    def "powinien poprawnie utworzyć obiekt User używając nazwanych argumentów"() {
        given: "dane użytkownika"
        def name = "Jan"
        def surname = "Kowalski"
        def userAge = 30

        when: "tworzymy nowego użytkownika"
        def user = new User(firstName: name, lastName: surname, age: userAge)

        then: "pola powinny być poprawnie ustawione"
        user.firstName == name
        user.lastName == surname
        user.age == userAge
    }

    def "powinien wygenerować czytelny toString dzięki adnotacji @Canonical"() {
        given:
        def user = new User(firstName: "Anna", lastName: "Nowak", age: 25)

        expect:
        user.toString() == "User(Anna, Nowak, 25)"
    }
}