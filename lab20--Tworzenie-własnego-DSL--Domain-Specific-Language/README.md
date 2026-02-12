Lab 20: Tworzenie własnego DSL (Domain Specific Language) – Mechanizm @DelegatesTo
----------------------------------------------------------------------------------
 
To jest tzw. Killer Feature Groovy'ego. 
Dzięki niemu powstał Gradle czy Jenkins Pipeline. 
Nauczysz się pisać kod, który nie wygląda jak kod, ale jak konfiguracja, którą rozumie biznes.

Cel: 
Stworzenie PizzaBuilder, który pozwoli na taki zapis:

```groovy
pizza {
    base 'Thin'
    size 32
    toppings {
        cheese 'Mozzarella'
        meat 'Salami'
        veggies 'Olives', 'Onion'
    }
}
```

Wchodzimy na najwyższy poziom wtajemniczenia. 
DSL (Domain Specific Language) to powód, dla którego Groovy wygrał z innymi językami na JVM w dziedzinie automatyzacji 
(Gradle, Jenkins, Spock – to wszystko to DSL-e).

Lab 20: Tworzenie własnego DSL – Mechanizm @DelegatesTo
-------------------------------------------------------

Cel:
Nauczysz się pisać kod, który wygląda jak czysta konfiguracja. 
Kluczem do tego jest zrozumienie, że w Groovy możesz zmienić "właściciela" (delegata) bloku kodu, 
tak aby metody wywoływane wewnątrz niego odnosiły się do Twojego Buildera.

Krok 1: Model Danych (POGO)
---------------------------

Stwórz plik `src/main/groovy/OrderModel.groovy`. 
To są zwykłe klasy, które nasz DSL będzie wypełniał danymi.

```groovy
import groovy.transform.ToString

@ToString
class OrderItem {
String name
double price
}

@ToString
class FinalOrder {
String customerName
List<OrderItem> items = []

    double getTotal() { items.price.sum() ?: 0 }
}
```

Krok 2: Serce DSL – Builder
---------------------------

Teraz stworzymy klasę, która będzie "rozumiała" komendy naszego języka. 
Stwórz `src/main/groovy/OrderBuilder.groovy`

Zwróć uwagę na adnotację @DelegatesTo – to ona podpowiada IDE (IntelliJ), jakie metody są dostępne wewnątrz klamerek.

```groovy
class OrderBuilder {
private FinalOrder order = new FinalOrder()

    // Metoda ustawiająca nazwę klienta
    void client(String name) {
        order.customerName = name
    }

    // Metoda obsługująca zagnieżdżony blok 'item'
    void item(String name, double price) {
        order.items << new OrderItem(name: name, price: price)
    }

    // Statyczna metoda wejściowa do DSL
    static FinalOrder make(@DelegatesTo(value = OrderBuilder, strategy = Closure.DELEGATE_FIRST) Closure code) {
        def builder = new OrderBuilder()
        
        // Magia: Przypisujemy builder jako delegata dla bloku kodu
        code.delegate = builder
        code.resolveStrategy = Closure.DELEGATE_FIRST
        
        code() // Wykonujemy blok kodu użytkownika
        
        return builder.order
    }
}
```

Krok 3: Test Spocka (Weryfikacja DSL)
-------------------------------------

To zadanie pokaże Ci, jak pięknie wygląda teraz tworzenie zamówień. 
Stwórz `src/test/groovy/DslSpec.groovy`

```groovy
import spock.lang.Specification

class DslSpec extends Specification {

    def "powinien utworzyć zamówienie używając czytelnego DSL"() {
        when: "używamy naszego nowego języka do konfiguracji zamówienia"
        def myOrder = OrderBuilder.make {
            client "Jan Kowalski"
            item "Książka: Groovy in Action", 150.0
            item "Kawa", 15.5
            item "Myszka bezprzewodowa", 80.0
        }

        then: "obiekt FinalOrder został poprawnie wypełniony"
        myOrder.customerName == "Jan Kowalski"
        myOrder.items.size() == 3
        
        and: "suma obliczona przez POGO jest poprawna"
        myOrder.total == 245.5
        
        and: "toString wygląda czytelnie"
        println myOrder
        myOrder.toString().contains("Jan Kowalski")
    }
}
```

Co tu się wydarzyło? (Analiza mechanizmu)

delegate: 
To najważniejszy koncept. 
Kiedy w bloku { ... } piszesz client "Jan", Groovy normalnie szukałby metody client w klasie testowej (gdzie jej nie ma). 
My jednak mówimy: "Hej, delegatem tego bloku jest OrderBuilder".

resolveStrategy = DELEGATE_FIRST: 
Mówimy Groovy'emu: "Najpierw szukaj metod w Builderze, a dopiero potem w otoczeniu". 
To pozwala na używanie tych samych nazw metod bez konfliktów.

Brak nawiasów: 
W Groovy, jeśli metoda ma parametry, możesz pominąć nawiasy: client "Jan" zamiast client("Jan"). 
To sprawia, że kod wygląda jak plik konfiguracyjny.

Wyzwanie Finałowe Lab 20:
------------------------

Rozbuduj swój DSL o zagnieżdżone kategorie. Chcemy, aby można było pisać tak:

```groovy
OrderBuilder.make {
    
  client "Anna Nowak"
  category("Elektronika") {
      item "Telefon", 2000
      item "Etui", 50
  }
    
}
```


Rozwiązanie zadania:
--------------------

To zadanie to prawdziwy sprawdzian Twoich umiejętności architektonicznych w Groovym. 
Zagnieżdżone bloki wymagają od Buildera umiejętności zarządzania swoim stanem wewnętrznym.

Oto jak zrealizować to wyzwanie krok po kroku.

1. Aktualizacja Modelu (OrderModel.groovy)
------------------------------------------

Dodajemy pole `category` do klasy `OrderItem`, aby wiedzieć, do której sekcji produkt został przypisany.

```groovy
import groovy.transform.ToString

@ToString
class OrderItem {
String name
double price
String category // Dodane pole
}

@ToString
class FinalOrder {
String customerName
List<OrderItem> items = []

double getTotal() { items.price.sum() ?: 0 }
}
```

2. Aktualizacja Buildera (OrderBuilder.groovy)
----------------------------------------------

Tutaj dzieje się magia. 
Wprowadzamy pole `activeCategory`, które przechowuje nazwę aktualnie otwartego bloku. 
Zastosujemy technikę "Save and Restore", aby obsłużyć poprawne zagnieżdżanie.

```groovy
class OrderBuilder {
private FinalOrder order = new FinalOrder()
private String activeCategory = "Ogólna" // Domyślna kategoria

    void client(String name) {
        order.customerName = name
    }

    // Zmieniona metoda item - teraz korzysta z activeCategory
    void item(String name, double price) {
        order.items << new OrderItem(name: name, price: price, category: activeCategory)
    }

    // NOWA METODA: Obsługa bloku category
    void category(String name, @DelegatesTo(value = OrderBuilder, strategy = Closure.DELEGATE_FIRST) Closure code) {
        // 1. Zapamiętujemy poprzednią kategorię (ważne przy głębokim zagnieżdżaniu!)
        def previousCategory = activeCategory
        
        // 2. Ustawiamy nową kategorię dla elementów wewnątrz tego bloku
        activeCategory = name
        
        // 3. Wykonujemy blok kodu (ponownie delegujemy do tego samego buildera)
        code.delegate = this
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        
        // 4. PRZYWRACAMY poprzednią kategorię po wyjściu z klamerek
        activeCategory = previousCategory
    }

    static FinalOrder make(@DelegatesTo(value = OrderBuilder, strategy = Closure.DELEGATE_FIRST) Closure code) {
        def builder = new OrderBuilder()
        code.delegate = builder
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()
        return builder.order
    }
}
```

3. Test w Spocku (DslSpec.groovy)
---------------------------------

Teraz sprawdzimy, czy produkty "pamiętają", w jakiej kategorii zostały stworzone.

```groovy
def "powinien utworzyć zamówienie z zagnieżdżonymi kategoriami"() {
when:
def myOrder = OrderBuilder.make {
client "Anna Nowak"

        category("Elektronika") {
            item "Telefon", 2000
            item "Etui", 50
        }
        
        category("Dom") {
            item "Lampa", 120
        }

        item "Torba na zakupy", 5 // To wpadnie do kategorii domyślnej ("Ogólna")
    }

    then: "sprawdzamy poprawność przypisania kategorii"
    myOrder.customerName == "Anna Nowak"
    myOrder.items.size() == 4

    and: "produkty z elektroniki mają poprawną kategorię"
    def elektronika = myOrder.items.findAll { it.category == "Elektronika" }
    elektronika.size() == 2
    elektronika.name.containsAll(["Telefon", "Etui"])

    and: "produkt poza blokiem ma kategorię domyślną"
    myOrder.items.find { it.name == "Torba na zakupy" }.category == "Ogólna"
    
    and: "całkowity koszt się zgadza"
    myOrder.total == 2175
}
```

Dlaczego to rozwiązanie jest profesjonalne?

Zasada Stack (Stosu): 
Przechowywanie `previousCategory` i przywracanie go po wykonaniu domknięcia to technika, która pozwala na nieskończone zagnieżdżanie 
(kategoria wewnątrz kategorii). To dokładnie tak działają profesjonalne DSL-e.

Fluent API: 
Dzięki temu, że `category` deleguje z powrotem do OrderBuilder, wewnątrz klamerek masz dostęp do tych samych metod (item), 
ale działają one w innym kontekście (zmienił się stan pola activeCategory).

Readability: 
Zauważ, jak czysto wygląda teraz tworzenie danych. 
Zamiast tworzyć obiekty new Item(...), pisać listy, dodawać je do zamówienia – piszesz to jak wiersz lub plik konfiguracyjny.

 