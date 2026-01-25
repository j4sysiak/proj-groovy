import spock.lang.Specification
import groovy.xml.XmlSlurper

class JsonToXmlSpec extends Specification {

    def "powinien poprawnie skonwertować listę produktów z JSON na XML"() {
        given: "wejściowy JSON"
        def json = '[{"n": "Laptop", "p": 4000}, {"n": "Mysz", "p": 100}]'

        when: "wykonujemy konwersję"
        def xmlResult = ConverterService.convertJsonToXml(json)
        println "Wygenerowany XML:\n$xmlResult"

        and: "parsujemy wynikowy XML do weryfikacji"
        def shop = new XmlSlurper().parseText(xmlResult)

        then: "główny tag to <shop>"
        shop.name() == "shop"

        and: "istnieją dokładnie dwa tagi <product>"
        shop.product.size() == 2

        and: "pierwszy produkt ma poprawny atrybut i treść"
        shop.product[0].@name == "Laptop"
        shop.product[0].text() == "Cena: 4000"

        and: "drugi produkt ma poprawną cenę"
        shop.product[1].text() == "Cena: 100"
    }
}