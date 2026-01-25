import spock.lang.Specification
import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper

class DataSpec extends Specification {

    def "powinien sparsować JSON i wyciągnąć dane"() {
        given:
        def json = '{"scores": [10, 20, 30], "user": {"id": 5}}'

        when:
        def data = new JsonSlurper().parseText(json)

        then:
        data.user.id == 5
        data.scores.sum() == 60
    }

    def "powinien czytać XML używając GPath"() {
        given: "prosty XML"
        def xmlText = '''
            <root>
                <item priority="high">Zadanie 1</item>
                <item priority="low">Zadanie 2</item>
            </root>
        '''

        when:
        def root = new XmlSlurper().parseText(xmlText)

        then: "dostęp do atrybutów przez @, do tagów przez kropkę"
        root.item.size() == 2
        root.item[0].text() == "Zadanie 1"
        root.item.find { it.@priority == "low" }.text() == "Zadanie 2"
    }
}