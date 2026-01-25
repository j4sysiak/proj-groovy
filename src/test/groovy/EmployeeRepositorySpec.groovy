import groovy.sql.Sql
import spock.lang.Specification
import spock.lang.Shared

class EmployeeRepositorySpec extends Specification {

    @Shared
    Sql sql = Sql.newInstance("jdbc:h2:mem:test_repo;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver")
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