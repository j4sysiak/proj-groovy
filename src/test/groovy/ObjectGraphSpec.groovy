import model.Company
import spock.lang.Specification

class ObjectGraphSpec extends Specification {

    def "powinien zbudować pełne drzewo obiektów z automatycznymi relacjami"() {
        given: "inicjalizacja buildera bez określonego pakietu (szuka w bieżącym)"
        def builder = new ObjectGraphBuilder()

        // Jeśli Twoje klasy są np. w pakiecie 'model', odkomentuj linię poniżej:
        builder.classNameResolver = 'model'

        when: "budujemy hierarchię"
        def company = builder.company(name: 'TechSolutions') {
            department(title: 'DevOps') {
                employee(name: 'Tomasz', salary: 8000)
            }
        }

        then: "Firma i relacje są poprawne"
        company.name == 'TechSolutions'
        company.departments[0].employees[0].department.title == 'DevOps'

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