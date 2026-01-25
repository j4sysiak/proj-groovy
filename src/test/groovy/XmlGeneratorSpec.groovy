import spock.lang.Specification
import groovy.xml.MarkupBuilder
import groovy.xml.XmlSlurper

class XmlGeneratorSpec extends Specification {

    def "generuje poprawny XML z MarkupBuilder"() {
        given:
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.records {
            user(id: 1) {
                username "jkowalski"
                email "j@k.pl"
            }
            user(id: 2) {
                username "anowak"
                email "a@n.pl"
            }
        }
        def xmlText = writer.toString()

        when:
        def root = new XmlSlurper().parseText(xmlText)

        then:
        root.name() == 'records'
        root.user.size() == 2
        root.user[0].@id.toString() == '1'
        root.user[0].username.text() == 'jkowalski'
        root.user[0].email.text() == 'j@k.pl'
        root.user[1].@id.toString() == '2'
        root.user[1].username.text() == 'anowak'
        root.user[1].email.text() == 'a@n.pl'
    }
}
