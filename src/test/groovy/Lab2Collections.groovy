import spock.lang.Specification

class Lab2Collections extends Specification {

    def "Zadanie 1 - Podstawy list"() {
        given: "Lista liczb"
        def liczby = [1, 5, 10, 15, 20]

        expect: "Ostatni element to 20 (użyj ujemnego indeksu)"  //W Groovy [-1] to ostatni element listy
        liczby[-1] == 20

        and: "Suma liczb to 51"
        liczby.sum() == 51
    }

    def "Zadanie 2 - Magia findAll i collect"() {
        given: "Lista zakupów (mapy)"
        def koszyk = [
                [name: 'Piwo', price: 5, alcohol: true],
                [name: 'Mleko', price: 4, alcohol: false],
                [name: 'Wino', price: 30, alcohol: true],
                [name: 'Chleb', price: 6, alcohol: false]
        ]

        when: "Szukamy tylko alkoholu"
        // findAll - szuka wszystkich elementów spełniających warunek
        def alkohole = koszyk.findAll { it.alcohol == true }

        then: "Znaleziono 2 produkty"
        alkohole.size() == 2

        when: "Wyciągamy same nazwy produktów alkoholowych"
        // collect - transformuje każdy element (odpowiednik map w Javie)
        def nazwy = alkohole.collect { it.name }

        then: "Nazwy to Piwo i Wino"
        nazwy == ["Piwo", "Wino"]
    }

    def "Zadanie 3 - Suma cen drogich produktów"() {
        given: "lista zakupów z poprzedniego zadania"
        def koszyk = [
                [name: 'Piwo', price: 5, alcohol: true],
                [name: 'Mleko', price: 4, alcohol: false],
                [name: 'Wino', price: 30, alcohol: true],
                [name: 'Chleb', price: 6, alcohol: false]
        ]

        when: "filtrujemy produkty droższe niż 5, wyciągamy ich ceny i sumujemy"

        // Wariant A: Krok po kroku (czytelny dla początkujących)
        def drogieProdukty
                = koszyk.findAll { it.price > 5 }
        def cenyDrogich = drogieProdukty.collect { it.price }
        def sumaA = cenyDrogich.sum()

        // Wariant B: "The Groovy Way" - jedna linijka (profesjonalny)
        // .price wywołane na liście obiektów automatycznie zbiera wszystkie ceny (tzw. spread operator shorthand)
        def sumaB = koszyk.findAll { it.price > 5 }.price.sum()

        then: "wynik w obu przypadkach powinien wynosić 36"
        sumaA == 36
        sumaB == 36
    }
}