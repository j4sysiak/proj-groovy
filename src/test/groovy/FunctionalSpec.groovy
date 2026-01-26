import spock.lang.Specification

class FunctionalSpec extends Specification {

    def "powinien poprawnie zastosować curry do filtracji"() {
        given: "ogólna funkcja filtrująca"
        def filterBy = { minLength, text -> text.length() >= minLength }

        and: "funkcja wyspecjalizowana dla długich słów"
        def isLongWord = filterBy.curry(10)

        expect:
        isLongWord("Groovy") == false
        isLongWord("Java-Groovy-Practices") == true
    }

    def "powinien składać funkcje matematyczne"() {
        given:
        def multiplyBy2 = { it * 2 }
        def add10 = { it + 10 }

        when: "składamy: (x + 10) * 2"
        def pipe = add10 >> multiplyBy2

        then:
        pipe(5) == 30 // (5 + 10) * 2
    }
}