import spock.lang.Specification

class DynamicSpec extends Specification {

    def "Expando powinien zachowywać się jak pełnoprawny obiekt"() {
        given: "dynamiczny obiekt konfiguracji"
        def config = new Expando()
        config.dupa = "siara"
        config.apiId = "123-ABC"
        config.active = true
        config.formatDate = { date -> new java.text.SimpleDateFormat("yyyy").format(date) }

        expect:
        config.dupa == "siara"
        config.apiId == "123-ABC"
        config.active == true
        config.formatDate(new Date()) == new java.text.SimpleDateFormat("yyyy").format(new Date())
    }

    def "Translator powinien dynamicznie obsługiwać metody sayInX"() {
        given:
        def translator = new Translator()

        when:
        def result1 = translator.sayInEnglish("Hello") // Metoda sayInEnglish NIE istnieje, a jednak zadziała!
        def result2 = translator.sayInSpanish("Dzień dobry") // Metoda sayInSpanish NIE istnieje, a jednak zadziała!

        then:
        result1 == "ZAKODOWANE_W_English"
        result2 == "ZAKODOWANE_W_Spanish"

        when: "wywołamy metodę, która nie pasuje do wzorca"
        translator.doSomethingElse()

        then:
        thrown(MissingMethodException)
    }

    def "powinien obliczyć total dla dynamicznego koszyka Expando"() {
        given: "dynamiczny koszyk"
        def shoppingCart = new Expando()
        shoppingCart.items = []

        // definicja metody dodającej produkt
        // 'items << ...' to skrót od 'items.add(...)'
        shoppingCart.addProduct = { String name, Number price ->
            shoppingCart.items << [name: name, price: price]
        }

        // definicja metody zwracającej sumę cen
        shoppingCart.total = { ->
            (shoppingCart.items.collect { it.price }.sum() ?: 0) as int
        }

        // lub alternatywnie, używając 'delegate' wewnątrz domknięcia:
        // 'items.price' tworzy listę samych cen, a '.sum()' je dodaje
        // '?: 0' to operator Elvisa - jeśli lista pusta, zwróć 0
           /*
             shoppingCart.total = {
             items.price.sum() ?: 0
           */


        when: "dodajemy produkty"
        shoppingCart.addProduct("Chleb", 5)
        shoppingCart.addProduct("Mleko", 4)
        shoppingCart.addProduct("Czekolada", 10)

        then: "lista przedmiotów ma odpowiednią wielkość"
        shoppingCart.items.size() == 3

        and: "suma się zgadza"
        shoppingCart.total() == 19
    }
}