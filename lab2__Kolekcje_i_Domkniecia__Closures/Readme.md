Skoro mamy już skonfigurowane środowisko (Gradle + Spock), 
czas na serce Groovy’ego, czyli to, co sprawia, że programiści Javy go uwielbiają.

Lab 2: Kolekcje i Domknięcia (Closures)
---------------------------------------

Cel: 
Nauczysz się operować na listach i mapach z prędkością światła, zapominając o pętlach for i skomplikowanych Streamach z Javy.

1. Składnia "Literal" (Listy i Mapy)
------------------------------------

W Javie tworzenie mapy to ból. W Groovym to po prostu nawiasy kwadratowe.

Zadanie 1 (do sprawdzenia w Main.groovy lub w teście):
------------------------------------------------------

Zauważ różnicę w czytelności:

```
// LISTY
def lista = [1, 2, 3, 4, 5] // java.util.ArrayList
println lista[0]            // dostęp przez indeks
println lista[-1]           // ostatni element! (składnia Groovy)

// MAPY
def kolory = [red: "#FF0000", green: "#00FF00", blue: "#0000FF"] // java.util.LinkedHashMap
println kolory.red          // dostęp jak do pola (Property access)
println kolory['green']     // dostęp przez klucz
```

2. Domknięcia (Closures) – Magiczne it
--------------------------------------

Closure to blok kodu, który możesz przypisać do zmiennej lub przekazać do metody. 
Jeśli closure ma tylko jeden parametr, Groovy nazywa go domyślnie it.

Przykład:

``````
def powitanie = { name -> println "Cześć $name!" }
powitanie("Tomek")

def kwadrat = { it * it } // 'it' to domyślny parametr
println kwadrat(5) // 25
``````

3. "Groovy Collections API" (Zamiast Streamów)
----------------------------------------------

W Javie mamy stream().filter().map().collect(). 

W Groovym mamy metody bezpośrednio na kolekcjach.

Zadanie 2: Operacje na danych
-----------------------------

Załóżmy, że mamy klasę Product (możesz ją szybko stworzyć jak User w Lab 1).

``````  
import groovy.transform.Canonical

@Canonical
class Product {
String name
double price
String category
}

def products = [
new Product("Chleb", 5.50, "Food"),
new Product("Mleko", 4.00, "Food"),
new Product("Monitor", 1200.00, "Tech"),
new Product("Myszka", 150.00, "Tech")
]


// 1. findAll (odpowiednik filter)
def cheapFood = products.findAll { it.price < 10 && it.category == "Food" }

// 2. collect (odpowiednik map)
def names = products.collect { it.name.toUpperCase() }

// 3. groupBy (grupuje w Mapę!)
def byCategory = products.groupBy { it.category }

// 4. Any / Every
boolean hasExpensiveTech = products.any { it.price > 1000 }
``````


Zadanie Praktyczne dla Ciebie (w Spocku):
-----------------------------------------

Otwórz/stwórz `src/test/groovy/CollectionSpec.groovy` i zaimplementuj następujący test. 
Twoim zadaniem jest dopisać brakujący kod, aby test przeszedł:

``````
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
``````
Co zyskujesz w tym Labie?

Nauczysz się pisać kod, który zajmuje 1 linię zamiast 10 w Javie.

Zrozumiesz, że it w Groovym to najszybszy sposób na manipulację danymi.

Zobaczysz, jak Mapy i Listy współpracują ze Spockiem.

Daj znać, jak uzupełnisz techPrices i czy testy w Spocku zaświeciły się na zielono! (Podpowiedź: products.findAll { ... }.collect { ... })