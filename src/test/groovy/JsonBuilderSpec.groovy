// groovy
import spock.lang.Specification
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JsonBuilderSpec extends Specification {

    def "generuje poprawny JSON z JsonBuilder"() {
        given:
        def today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        def items = (1..3).collect { "Item-$it" }          // jawna lista stringów
        def builder = new JsonBuilder()
        builder.response {
            status "SUCCESS"
            timestamp today
            results items                                // użycie wcześniej przygotowanej listy
        }

        when:
        def jsonText = builder.toPrettyString()
        def data = new JsonSlurper().parseText(jsonText)

        then:
        data.response.status == 'SUCCESS'
        data.response.timestamp == today
        data.response.results == ['Item-1', 'Item-2', 'Item-3']
        data.response.results.size() == 3
    }
}
