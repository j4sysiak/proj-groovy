import spock.lang.Specification
import groovy.json.JsonSlurper

class DataProcessorSpec extends Specification {

    def "parsowanie jsonText z DataProcessor"() {
        given:
        def jsonText = '''
        {
            "company": "DevStudio",
            "employees": [
                {"name": "Adam", "role": "Dev", "skills": ["Java", "Groovy"]},
                {"name": "Ewa", "role": "Tester", "skills": ["Selenium"]}
            ],
            "active": true
        }
        '''
        def slurper = new JsonSlurper()

        when:
        def data = slurper.parseText(jsonText)

        // Dostęp do danych przez kropkę!
        then:
        data.company == 'DevStudio'
        data.active == true
        data.employees.size() == 2
        data.employees[0].name == 'Adam'

        // Magia Groovy: znajdź skille wszystkich programistów
        and:
        data.employees.find { it.role == 'Dev' }.skills.containsAll(['Java', 'Groovy'])
    }
}
