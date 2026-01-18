import spock.lang.Specification

class CollectionSpec extends Specification {

    def "powinien przefiltrować i zsumować ceny produktów"() {
        given: "lista produktów"
        def products = [
                [name: 'Laptop', price: 3500, cat: 'Tech'],
                [name: 'Klawiatura', price: 200, cat: 'Tech'],
                [name: 'Sok', price: 5, cat: 'Food']
        ]

        when: "wybieramy tylko produkty Tech i pobieramy ich ceny"
        // TODO: Użyj findAll oraz collect na liście 'products'
        def techPrices = []

        then: "lista cen tech powinna mieć 2 elementy"
        techPrices == [3500, 200]

        and: "suma cen tech powinna wynosić 3700"
        // Tip: Użyj techPrices.sum()
        techPrices.sum() == 3700
    }

    def "powinien zademonstrować bezpieczną nawigację na mapie"() {
        given: "mapa zagnieżdżona"
        def user = [profile: [address: [city: 'Warszawa']]]

        expect: "dostęp do nieistniejącego klucza nie rzuca NPE"
        // TODO: Użyj operatora ?. (Safe Navigation), aby dostać się do 'street'
        // user.profile.address.street
        user.profile.address.street == null
    }
}