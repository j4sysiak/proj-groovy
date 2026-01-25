import spock.lang.Specification

class DynamicSpec extends Specification {

    def "Expando powinien zachowywać się jak pełnoprawny obiekt"() {
        given: "dynamiczny obiekt konfiguracji"
        def config = new Expando()
        config.apiId = "123-ABC"
        config.active = true
        config.formatDate = { date -> date.format("yyyy") }

        expect:
        config.apiId == "123-ABC"
        config.active == true
        config.formatDate(new Date()) == new Date().format("yyyy")
    }

    def "Translator powinien dynamicznie obsługiwać metody sayInX"() {
        given:
        def translator = new Translator()

        when:
        def result = translator.sayInEnglish("Hello")

        then:
        result == "ZAKODOWANE_W_English"

        when: "wywołamy metodę, która nie pasuje do wzorca"
        translator.doSomethingElse()

        then:
        thrown(MissingMethodException)
    }
}