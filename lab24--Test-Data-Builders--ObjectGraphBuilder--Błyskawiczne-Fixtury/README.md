Lab 24: Test Data Builders (ObjectGraphBuilder) – Błyskawiczne Fixtury
-----------------------------------------------

Teraz czas na coś, co drastycznie przyspieszy Twoją pracę z testami.

W dużych systemach klasy są zagnieżdżone: 
Klient ma Adresy, Adres ma Kraj, Klient ma też Zamówienia. 
Tworzenie takich struktur w testach za pomocą new ... to dziesiątki linii nudnego kodu.
Nauczę Cię, jak użyć wbudowanego `ObjectGraphBuilder`, aby tworzyć całe drzewa obiektów w kilka sekund.


Wchodzimy w temat, który oszczędzi Ci tysiące linii nudnego kodu w testach integracyjnych. 
W świecie Javy tworzenie złożonych danych testowych (tzw. Test Data Fixtures) często kończy się pisaniem ogromnych metod pomocniczych.
Groovy oferuje `ObjectGraphBuilder`, który pozwala "narysować" strukturę obiektów za pomocą DSL,
a on sam zajmie się ich tworzeniem i łączeniem relacji.

Cel: 
Automatyczne budowanie drzewa obiektów (np. Firma -> Działy -> Pracownicy) 
bez ręcznego ustawiania relacji typu setParent czy addToList.

Krok 1: Przygotowanie Modelu (POGO)
-----------------------------------

Zdefiniujmy prostą strukturę zagnieżdżoną. Stwórz plik w pakiecie model: `src/main/groovy/CompanyModel.groovy`

```groovy
package model

import groovy.transform.ToString

@ToString
class model.Company {
String name
List<model.Department> departments = []
}

@ToString(includeNames = true, excludes = 'company')
class model.Department {
String title
model.Company company // Relacja zwrotna
List<model.Employee> employees = []
}

@ToString(includeNames = true, excludes = 'department')
class model.Employee {
String name
double salary
model.Department department // Relacja zwrotna
}
```

Krok 2: Konfiguracja ObjectGraphBuilder
---------------------------------------

`ObjectGraphBuilder` potrzebuje wiedzieć, w jakim pakiecie szukać klas. 
Stwórz plik `src/main/groovy/DataBuilderService.groovy`

```groovy
class DataBuilderService {

    static model.Company buildCompanyHierarchy() {
        // Inicjalizacja buildera z pakietem, w którym są klasy
        def builder = new ObjectGraphBuilder(basePackage: 'model') 
        // Uwaga: Jeśli nie używasz pakietów, zostaw puste lub podaj odpowiedni pakiet.
        
        // Budujemy graf obiektów
        model.Company acme = builder.company(name: 'ACME Corp') {
            department(title: 'IT') {
                employee(name: 'Jan', salary: 5000)
                employee(name: 'Anna', salary: 6500)
            }
            department(title: 'HR') {
                employee(name: 'Piotr', salary: 4000)
            }
        }
        
        return acme
    }
}
```

Krok 3: Test Spocka (Weryfikacja Grafu)
---------------------------------------

Sprawdźmy, czy Builder poprawnie połączył obiekty. 
Stwórz `src/test/groovy/ObjectGraphSpec.groovy`

```groovy
import spock.lang.Specification

class ObjectGraphSpec extends Specification {

    def "powinien zbudować pełne drzewo obiektów z automatycznymi relacjami"() {
        when: "budujemy hierarchię"
        // Jeśli klasy są w tym samym pakiecie co test, basePackage może być opcjonalne
        def builder = new ObjectGraphBuilder(basePackage: "") 
        
        model.Company company = builder.company(name: 'TechSolutions') {
            department(title: 'DevOps') {
                employee(name: 'Tomasz', salary: 8000)
            }
        }

        then: "Firma ma działy"
        company.name == 'TechSolutions'
        company.departments.size() == 1

        and: "Dział ma pracowników"
        def devOps = company.departments[0]
        devOps.title == 'DevOps'
        devOps.employees.size() == 1

        and: "RELACJA ZWROTNA: Pracownik automatycznie wie, w jakim jest dziale!"
        // To jest magia buildera - sam ustawił pole 'department' w obiekcie model.Employee
        devOps.employees[0].name == 'Tomasz'
        devOps.employees[0].department == devOps
    }
}
```

Dlaczego to jest "Practice" (Dobra praktyka)?

Automatyczne relacje (Parent/Child): 
Jeśli klasa model.Employee ma pole department, a klasa model.Department ma listę employees, 
   to `ObjectGraphBuilder` domyśli się, że należy je ze sobą powiązać. 
   Nie musisz pisać `emp.setDepartment(dept)`.

Czytelność: 
Zamiast 10 linii new i add, masz strukturę, która wygląda jak mapa drogowa Twoich danych.

Łatwość zmian: 
Chcesz dodać trzeciego pracownika? 
To tylko jedna linijka wewnątrz klamerek, a nie nowa zmienna i wywołania setterów.



Wyzwanie Finał owe Lab 24:
--------------------------

Stwórz model dla Biblioteki (library) 
Klasa Library ma pole name i listę books.
Klasa Book ma pole title i obiekt Author.
Klasa Author ma pole lastName.

Zadanie:
Użyj `ObjectGraphBuilder`, aby stworzyć bibliotekę "Książnica" z dwiema książkami tego samego autora (np. Lem).

W teście Spocka sprawdź:
1. Czy biblioteka ma 2 książki.
2. Czy obie książki mają tego samego autora (sprawdź referencję do obiektu).

Bonus: 
Użyj identyfikatorów w builderze (atrybut id), 
aby przypisać tego samego autora do obu książek bez tworzenia go dwa razy.

Podpowiedź: 
author(id: 'lem', lastName: 'Lem') a potem w drugiej książce author(refId: 'lem').

Rozwiązanie:
------------

To zadanie nauczy Cię jednej z najważniejszych funkcji `ObjectGraphBuilder` – zarządzania referencjami. 
W rzeczywistych systemach obiekty rzadko tworzą czyste drzewo; 
częściej jest to graf, gdzie wiele obiektów wskazuje na ten sam element (np. wiele książek jednego autora).

W Groovy robimy to za pomocą atrybutów `id` oraz `refId`.

Krok 1: Model Danych (POGO)
---------------------------

Stwórz plik `src/main/groovy/Library.groovy` w pakiecie `model`

```groovy
package model

import groovy.transform.ToString
import groovy.transform.Canonical

@Canonical
class Library {
String name
List<Book> books = []
}

@Canonical
class Book {
String title
Author author
}

@Canonical
class Author {
String lastName
}
```

Krok 2: Wyzwanie – Test ze współdzielonym autorem
-------------------------------------------------

Stwórz plik `src/test/groovy/LibraryBuilderSpec.groovy`

Zauważ użycie `id` przy tworzeniu pierwszej książki i `refId` przy drugiej. 
Dzięki temu nie tworzymy drugiego obiektu klasy `Author`, lecz wskazujemy na ten już istniejący.

```groovy
import spock.lang.Specification

class LibraryBuilderSpec extends Specification {

    def "powinien stworzyć bibliotekę z wieloma książkami tego samego autora używając refId"() {
        given: "inicjalizacja buildera"
        def builder = new ObjectGraphBuilder()
        builder.classNameResolver = "model" // Ustawiamy pakiet, w którym są nasze klasy

        when: "budujemy graf obiektów z użyciem identyfikatorów"
        Library mojaBiblioteka = builder.library(name: 'Książnica') {
            // Pierwsza książka tworzy autora i nadaje mu ID 'lem'
            book(title: 'Solaris') {
                author(id: 'lem', lastName: 'Lem')
            }
            // Druga książka tylko referuje do istniejącego autora 'lem'
            book(title: 'Cyberiada') {
                author(refId: 'lem')
            }
        }

        then: "biblioteka ma dwa elementy"
        mojaBiblioteka.books.size() == 2

        and: "obie książki mają autora o nazwisku Lem"
        mojaBiblioteka.books[0].author.lastName == 'Lem'
        mojaBiblioteka.books[1].author.lastName == 'Lem'

        and: "KLUCZOWY TEST: To jest dokładnie ten sam obiekt w pamięci!"
        // W Groovy 'is()' sprawdza referencję (czy to ten sam adres w RAM)
        mojaBiblioteka.books[0].author.is(mojaBiblioteka.books[1].author)
        
        // Alternatywnie w Spocku:
        System.identityHashCode(mojaBiblioteka.books[0].author) == System.identityHashCode(mojaBiblioteka.books[1].author)
    }
}
```

Dlaczego to jest "Expert Practice"?

Referential Integrity: 
W bazach danych (JPA/Hibernate) bardzo ważne jest, abyś nie tworzył dwóch kopii tego samego wiersza. 
Używając `id` i `refId`, Twój graf obiektów w testach zachowuje się dokładnie tak, jak dane zaciągnięte z prawdziwej bazy danych.

Zwięzłość: 
Wyobraź sobie, że masz 100 książek Lema. 
Zamiast za każdym razem pisać author(lastName: 'Lem'), piszesz to raz, a potem tylko author(refId: 'lem').

Tożsamość (is()): Metoda is() to odpowiednik == w Javie dla obiektów (porównanie referencji). 
W Groovy samo == zazwyczaj wywołuje .equals(), więc is() jest niezbędne do sprawdzenia tożsamości obiektu.

Co zyskaliśmy w Lab 24?

Umiesz budować skomplikowane struktury danych do testów, które są:
1. Czytelne (DSL).
2. Logicznie poprawne (relacje dwustronne).
3. Wydajne i spójne (referencje).