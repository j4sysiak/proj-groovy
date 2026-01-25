import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder

class ConverterService {

    static String convertJsonToXml(String jsonInput) {
        // 1. Parsowanie JSON
        def products = new JsonSlurper().parseText(jsonInput)

        // 2. Budowanie XML
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.shop {
            products.each { p ->
                // 'product' to nazwa taga, 'name' to atrybut, reszta to zawartość taga
                product(name: p.n, "Cena: ${p.p}")
            }
        }
        return writer.toString()
    }

}