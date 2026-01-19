import spock.lang.Specification

class CollectionSpec extends Specification {

    def "powinien przefiltrować i zsumować ceny produktów"() {
        given: "lista produktów"

        // To krótkie wyjaśnienie:
        // w Groovym to jest lista (List) map (Map) — każdy element to mapa z kluczami name, price, cat.
        // Mapy tworzy się składnią [key: value, ...].
        // Do pól można dostać się jako item.name lub item['name'].
        // Poniżej przykład pokazujący utworzenie, odczyt i filtrowanie (użyto findAll i collect, oraz operatora rozproszenia \*.\ w kodzie).

        def products = [
                [name: 'Laptop', price: 3500, cat: 'Tech'],
                [name: 'Klawiatura', price: 200, cat: 'Tech'],
                [name: 'Sok', price: 5, cat: 'Food']
        ]

        when: "wybieramy tylko produkty Tech i pobieramy ich ceny"
        def techPrices
                = products.findAll { it.cat == 'Tech' }
                                   .collect { it.price }
        // alternatywnie: def techPrices = products.findAll { it.cat == 'Tech' }*.price

        then: "lista cen tech powinna mieć 2 elementy"
        techPrices == [3500, 200]

        and: "suma cen tech powinna wynosić 3700"
        techPrices.sum() == 3700
    }


    def "powinien zademonstrować bezpieczną nawigację na mapie"() {
        given: "mapa zagnieżdżona"
        def user = [profile: [address: [city: 'Warszawa']]]

        expect: "dostęp do nieistniejącego klucza nie rzuca NullPointerException"
        // TODO: Użyj operatora ?. (Safe Navigation), aby dostać się do 'street'
        // user.profile.address.street
        user.profile.address.street == null
    }
}