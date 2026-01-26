import spock.lang.Specification

class CompositionSpec extends Specification {

    def "powinien przetworzyć tekst w rurociągu (Pipeline) używając operatora >>"() {
        given: "małe, niezależne funkcje"
        def trim = { String s -> s.trim() }            // Usuwa spacje
        def upper = { String s -> s.toUpperCase() }    // Zamienia na wielkie litery
        def wrap = { String s -> "[ $s ]" }            // Dodaje nawiasy

        when: "łączymy funkcje w jeden proces: trim -> upper -> wrap"
        // Operator >> przekazuje wynik jednej funkcji jako argument kolejnej
        def cleanAndFormat = trim >> upper >> wrap

        then: "tekst wejściowy przechodzi przez wszystkie etapy"
        cleanAndFormat("   java-groovy-practices   ") == "[ JAVA-GROOVY-PRACTICES ]"
    }

    def "powinien pokazać różnicę między kompozycją w przód (>>) i w tył (<<)"() {
        given: "funkcje matematyczne"
        def multiplyBy2 = { it * 2 }
        def add10 = { it + 10 }

        when: "Forward Composition (>>): Kolejność czytania"
        // Najpierw * 2, potem + 10
        def forward = multiplyBy2 >> add10

        and: "Backward Composition (<<): Kolejność matematyczna f(g(x))"
        // Najpierw + 10 (prawa strona), potem * 2 (lewa strona)
        def backward = multiplyBy2 << add10

        then:
        forward(5) == 20   // (5 * 2) + 10 = 10 + 10 = 20
        backward(5) == 30  // (5 + 10) * 2 = 15 * 2 = 30
    }
}