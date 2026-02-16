import model.Company
import groovy.util.ObjectGraphBuilder

class DataBuilderService {

    static Company buildCompanyHierarchy() {
        // Inicjalizacja buildera z pakietem, w którym są klasy
        def builder = new ObjectGraphBuilder()
        // Tak wskazujemy pakiet w ObjectGraphBuilder
        // Jeśli Twoje klasy są w pakiecie 'model', wpisz 'model'
        // Jeśli nie mają pakietu, możesz to pominąć lub wpisać ""
        builder.classNameResolver = "model"

        // Budujemy graf (metody muszą być małą literą, odpowiadają nazwom klas)
        Company acme = builder.company(name: 'ACME Corp') {
            department(title: 'IT') {
                employee(name: 'Jan', salary: 5000)
                employee(name: 'Anna', salary: 6500)
            }
        }

        return acme
    }
}