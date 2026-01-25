Wkraczamy w jeden z najbardziej unikalnych obszarów Groovy’ego. 
W Javie struktura obiektu (jego pola i metody) jest "wyryta w kamieniu" w momencie kompilacji. 
W Groovy obiekt może być "plasteliną", którą formujesz w trakcie działania programu.

Lab 10: Dynamizm "w locie" – Expando i Dynamic Dispatch
-------------------------------------------------------

Cel: 
Nauczysz się tworzyć obiekty bez definiowania klas (Expando) oraz dowiesz się, 
jak przechwytywać wywołania metod, które... nie istnieją (methodMissing).

Zadanie 1: Klasa Expando – Obiekt-Kameleon
------------------------------------------

Expando to wbudowana klasa w Groovy, która zachowuje się jak połączenie Mapy i Obiektu. 
Możesz do niej przypisać pole lub funkcję (Closure) w dowolnym momencie.

Co masz zrobić:
Stwórz plik `src/main/groovy/DynamicMagic.groovy`:

````
// Tworzymy obiekt bez żadnej klasy!
def person = new Expando()

// Dodajemy pola "w locie"
person.firstName = "Jan"
person.lastName = "Kowalski"

// Dodajemy metodę jako domknięcie (Closure)
person.sayHello = {
println "Cześć, jestem $firstName $lastName!"
}

// Dodajemy logikę obliczeniową
person.calculateBonus = { salary ->
salary * 1.10
}

// Użycie
person.sayHello()
println "Bonus: " + person.calculateBonus(5000)
````

Kiedy to się przydaje?
W testach, gdy potrzebujesz "szybkiego obiektu", który udaje skomplikowaną encję.
Przy parsowaniu danych z formatów, których struktury nie znasz (np. dynamiczne parametry z API).


Zadanie 2: methodMissing – Przechwytywanie "nieznanego"
-------------------------------------------------------

To jest "czarna magia" Groovy’ego. 
Możesz napisać klasę, która nie ma żadnych metod, ale potrafi odpowiedzieć na każde wywołanie.

Co masz zrobić:
Stwórz klasę `Translator.groovy`:


class Translator {

    // Ta metoda uruchomi się, gdy ktoś wywoła metodę, której nie ma w klasie
    def methodMissing(String name, args) {
        if (name.startsWith("sayIn")) {
            def language = name.substring(5) // np. "Spanish"
            println "Tłumaczę '${args[0]}' na język $language..."
            return "ZAKODOWANE_W_$language"
        }
        throw new MissingMethodException(name, this.class, args)
    }
}

// Test użycia w klasie testowej: `src/test/groovy/DynamicSpec.groovy`
def translator = new Translator()
translator.sayInSpanish("Dzień dobry") // Metoda sayInSpanish NIE istnieje, a jednak zadziała!
translator.sayInGerman("Guten Tag")



Zadanie 3: Test Spocka (Weryfikacja dynamizmu)
----------------------------------------------

Sprawdźmy, jak te dynamiczne byty zachowują się w testach. 
Przyklad w: `src/test/groovy/DynamicSpec.groovy`:

``````
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
``````



Wyzwanie Finałowe Lab 10:
-------------------------

Stwórz obiekt Expando, który udaje koszyk zakupowy (shoppingCart).
Dodaj do niego listę items (na starcie pustą).
Dodaj metodę addProduct(name, price), która dodaje Mapę [name: name, price: price] do listy items.
Dodaj metodę total(), która sumuje ceny wszystkich produktów w koszyku (użyj .price.sum(), którego nauczyłeś się w Lab 2!).
Napisz test Spocka, który doda dwa produkty (np. Chleb 5 i Mleko 4) i sprawdzi, czy total() wynosi 9
Daj znać, jak poszło z dynamicznym koszykiem!
To świetny przykład na to, jak unikać tworzenia klas dla prostych struktur danych.

Rozwiązanie wyzwania:
---------------------
Przyklad w: `src/test/groovy/DynamicSpec.groovy`:

````
def "powinien obliczyć total dla dynamicznego koszyka Expando"() {
    given: "dynamiczny koszyk"
    def shoppingCart = new Expando()
    shoppingCart.items = [] // Inicjalizacja listy produktów

    // 1. Definiujemy dodawanie produktu
    // 'items << ...' to skrót od 'items.add(...)'
    shoppingCart.addProduct = { name, price -> 
        items << [name: name, price: price] 
    }

    // 2. Definiujemy obliczanie sumy
    // 'items.price' tworzy listę samych cen, a '.sum()' je dodaje
    // '?: 0' to operator Elvisa - jeśli lista pusta, zwróć 0
    shoppingCart.total = { 
        items.price.sum() ?: 0 
    }

    when: "dodajemy produkty do koszyka"
    shoppingCart.addProduct("Chleb", 5)
    shoppingCart.addProduct("Mleko", 4)
    shoppingCart.addProduct("Czekolada", 10)

    then: "lista przedmiotów ma odpowiednią wielkość"
    shoppingCart.items.size() == 3

    and: "suma cen wszystkich produktów jest poprawna"
    shoppingCart.total() == 19
}
````
Dlaczego to jest "Groovy Way"? (Analiza składni)
Operator << (Left Shift): 
W Groovy zamiast list.add(element) niemal zawsze używamy list << element. 
Jest to czytelniejsze i bardzo popularne w skryptach.

Shorthand items.price: 
To jedna z najpotężniejszych cech Groovy'ego. 
Jeśli items jest listą map, a każda mapa ma klucz price, to wywołanie items.price automatycznie "wyciąga"
wszystkie ceny i tworzy z nich nową listę (np. [5, 4, 10]). 
W Javie musiałbyś użyć stream().map(...).collect().

Operator Elvisa ?: 0: 
Zabezpiecza nas przed sytuacją, gdy koszyk jest pusty. Jeśli sum() zwróciłoby null, operator podstawi 0.


Podsumowanie:
-------------
-------------

Dlaczego to są "Java-Groovy-Practices"?

1. Elastyczność: 
W Javie musiałbyś użyć wzorca "Strategy" lub "Visitor" i zdefiniować interfejsy dla każdego języka.
W Groovy rozwiązujesz to jednym methodMissing.

2. Prototypowanie: 
Zanim zaprojektujesz sztywną strukturę klas w Javie, możesz "wyklikać" logikę w Groovy przy użyciu Expando, aby sprawdzić, jakie pola są Ci naprawdę potrzebne.

3. Integracja z Javą: 
Możesz przekazać obiekt Expando do metody w Javie, która przyjmuje Map (bo Expando implementuje dostęp jak mapa) lub użyć go jako dynamicznego Proxy.





