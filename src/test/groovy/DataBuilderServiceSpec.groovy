import model.Company
import spock.lang.Specification

class DataBuilderServiceSpec extends Specification {

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
    }

    def "powinien wygenerować kompletną strukturę ACME Corp z poprawnymi danymi"() {
        when: "wywołujemy serwis budujący dane"
        def company = DataBuilderService.buildCompanyHierarchy()

        then: "główny obiekt firmy jest poprawny"
        company instanceof Company
        company.name == 'ACME Corp'
        company.departments.size() == 1

        and: "dział IT ma poprawnych pracowników i pensje"
        def itDept = company.departments.find { it.title == 'IT' }
        itDept != null
        itDept.employees.size() == 2

        // Magia Groovy: wyciągamy imiona wszystkich pracowników działu IT
        itDept.employees.name.containsAll(['Jan', 'Anna'])

        // Sprawdzamy konkretną pensję
        itDept.employees.find { it.name == 'Jan' }.salary == 5000

        and: "relacje zwrotne są automatycznie ustawione (Magia ObjectGraphBuilder)"
        // Sprawdzamy, czy pracownik "pamięta" swój dział
        itDept.employees.every { it.department == itDept }

        // Sprawdzamy, czy dział "pamięta" swoją firmę
        itDept.company == company
    }

    def "powinien poprawnie wyliczyć sumę pensji w całej firmie"() {
        given:
        def company = DataBuilderService.buildCompanyHierarchy()

        expect: "suma wszystkich pensji (5000 + 6500)"
        // wykorzystuje składnię GPath w Groovy do nawigacji po strukturze obiektów i obliczania
        // sumy pensji wszystkich pracowników w firmie.
        // GPath: idziemy głęboko w dół drzewa i zbieramy wszystkie pensje
        company.departments.employees.flatten().salary.sum() == 11500
    }
}