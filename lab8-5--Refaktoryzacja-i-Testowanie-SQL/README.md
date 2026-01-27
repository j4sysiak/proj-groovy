A jak przetestować klasę:  SqlQuickstart  ?
-------------------------------------------

Testowanie kodu, który operuje bezpośrednio na bazie danych (jak SqlQuickstart), to idealny moment, aby wprowadzić pojęcie Refaktoryzacji pod testy (Testing-Friendly Design).

Jeśli SqlQuickstart jest luźnym skryptem, trudno go przetestować automatycznie. 
Aby to zrobić profesjonalnie, musimy zamienić go w klasę (serwis), której będziemy mogli "podłożyć" bazę danych w teście.

Oto jak to zrobić w ramach Lab 8.5: Refaktoryzacja i Testowanie SQL:

Krok 1: 
Refaktoryzacja SqlQuickstart

Zamiast trzymać wszystko w luźnym skrypcie, zamknijmy logikę w klasie EmployeeRepository. 
Dzięki temu będziemy mogli wstrzyknąć obiekt Sql (Dependency Injection).

Zmień plik src/main/groovy/EmployeeRepository.groovy:

```groovy
import groovy.sql.Sql

class EmployeeRepository {
private Sql sql

    // Konstruktor przyjmuje obiekt Sql - to pozwala nam podpiąć inną bazę w teście!
    EmployeeRepository(Sql sql) {
        this.sql = sql
    }

    void initDatabase() {
        sql.execute "CREATE TABLE Employee (id INT PRIMARY KEY, name VARCHAR(100), salary INT)"
    }

    void addEmployee(int id, String name, int salary) {
        sql.execute "INSERT INTO Employee (id, name, salary) VALUES ($id, $name, $salary)"
    }

    List<String> getRichEmployeesNames(int minSalary) {
        return sql.rows("SELECT name FROM Employee WHERE salary > $minSalary").collect { it.name }
    }

    int getTotalSalary() {
        return sql.firstRow("SELECT SUM(salary) as total FROM Employee").total ?: 0
    }
}
```

Krok 2: Testowanie w Spocku
---------------------------

Teraz stworzymy test, który faktycznie uruchamia tę bazę w pamięci i sprawdza, czy logika EmployeeRepository działa.

Stwórz `src/test/groovy/EmployeeRepositorySpec.groovy`:

```groovy
import groovy.sql.Sql
import spock.lang.Specification
import spock.lang.Shared

class EmployeeRepositorySpec extends Specification {

    @Shared Sql sql = Sql.newInstance("jdbc:h2:mem:test_repo;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver")
    EmployeeRepository repository = new EmployeeRepository(sql)

    def setup() {
        // Przed każdym testem tworzymy czystą tabelę
        repository.initDatabase()
    }

    def cleanup() {
        // Po każdym teście usuwamy tabelę, żeby testy były odizolowane
        sql.execute "DROP TABLE Employee"
    }

    def "powinien poprawnie wyliczyć sumę pensji"() {
        given: "mamy kilku pracowników w bazie"
        repository.addEmployee(1, "Jan", 3000)
        repository.addEmployee(2, "Anna", 5000)

        when: "pobieramy sumę"
        def suma = repository.getTotalSalary()

        then: "wynik powinien być poprawny"
        suma == 8000
    }

    def "powinien filtrować bogatych pracowników"() {
        given:
        repository.addEmployee(1, "Taniej", 1000)
        repository.addEmployee(2, "Drożej", 9000)

        expect:
        repository.getRichEmployeesNames(5000) == ["Drożej"]
    }
}
```

Co zyskaliśmy dzięki takiemu podejściu?

Odizolowanie logiki: Testujemy "co" robi nasza klasa, a nie "jak" łączy się z bazą (bo bazę dostajemy gotową).

Powtarzalność: Dzięki cleanup { DROP TABLE } każdy test startuje z pustą bazą. Nie ma ryzyka, że dane z poprzedniego testu zepsują wynik obecnego.

Brak Mockowania (Unit vs Integration): Kod SQL najlepiej testować testami integracyjnymi (z prawdziwą bazą H2), a nie Mockami. Mockowanie obiektu Sql jest bardzo trudne i mało sensowne, bo nie sprawdzisz wtedy, czy Twoje zapytania SQL są poprawne składniowo.

Bonus: Jak przetestować skrypt, który tylko wypisuje coś przez println?

Jeśli bardzo chcesz przetestować stary skrypt SqlQuickstart bez zmieniania go w klasę, możesz przechwycić to, co wypisuje na konsolę. W Groovym robimy to tak:

````groovy
def "powinien wypisać coś na konsolę"() {
given:
def output = new ByteArrayOutputStream()
System.setOut(new PrintStream(output))

    when:
    // Tu wywołujesz swój skrypt lub metodę z println
    println "Testujemy konsolę"

    then:
    output.toString().contains("Testujemy konsolę")

    cleanup:
    System.setOut(System.out) // Koniecznie przywróć standardowe wyjście!
}
````

[2]
Czy chcesz teraz przejść do Lab 9 (Transformacje AST), czy masz jeszcze pytania do testowania bazy danych?

Sources
help
stackoverflow.com
medium.com
Google Search Suggestions
Display of Search Suggestions is required when using Grounding with Google Search. Learn more
how to test groovy script with groovy.sql.Sql in Spock
refactoring groovy script for testing dependency injection sql
spock test capture println output groovy
test groovy.sql.Sql integration test h2 spock