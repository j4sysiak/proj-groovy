W Lab 11 zajmiemy się danymi. 
----------------------------

W świecie Javy parsowanie XML (DOM/SAX) czy JSON (Jackson/Gson) często wymaga tworzenia wielu klas DTO i skomplikowanej 
konfiguracji. 
Groovy oferuje `Slurpery` (do czytania) i `Buildery` (do tworzenia), które sprawiają, że praca z tymi formatami 
to czysta przyjemność.

Lab 11: JSON & XML – Slurpery i Buildery
----------------------------------------

Cel: 
Nauczysz się błyskawicznie przetwarzać dane bez konieczności definiowania sztywnych struktur klas dla każdego formatu.

Zadanie 1: JsonSlurper – JSON bez POJO
--------------------------------------

W Javie, aby przeczytać JSON, potrzebujesz biblioteki `Jackson` i klasy Java. 
W Groovy czytasz JSON-a i używasz go jak zwykłego obiektu (kolekcji map i list).

Co masz zrobić:
Stwórz plik `src/main/groovy/DataProcessor.groovy`:

``````
import groovy.json.JsonSlurper

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
def data = slurper.parseText(jsonText)

// Dostęp do danych przez kropkę!
println "Firma: ${data.company}"
println "Pierwszy pracownik: ${data.employees[0].name}"

// Magia Groovy: znajdź skille wszystkich programistów
def allSkills = data.employees.findAll { it.role == "Dev" }.skills.flatten()
println "Umiejętności devów: $allSkills"
``````

i odpowiadajacy Groovy Test:
`src/main/groovy/DataProcessor.groovy`

Zadanie 2: JsonBuilder – Generowanie JSON przez DSL
---------------------------------------------------

Zamiast ręcznie składać Stringi, używamy DSL, który wygląda jak struktura JSON-a.

Zapisz test jako src/main/groovy/JsonBuilder.groovy.
````
import groovy.json.JsonBuilder

def builder = new JsonBuilder()
builder.response {
status "SUCCESS"
timestamp new Date().format("yyyy-MM-dd")
results (1..3).collect { "Item-$it" }
}

println "Wygenerowany JSON:"
println builder.toPrettyString()
````

JsonBuilder tak jak w przykładzie, parsuje wygenerowany JSON i weryfikuje status, timestamp 
(dzisiejsza data w formacie yyyy-MM-dd) oraz listę results.

i odpowiadajacy Groovy Test:
`src/test/groovy/JsonBuilderSpec.groovy`


Zadanie 3: MarkupBuilder – XML w wersji "light"
-----------------------------------------------

XML w Javie jest bolesny. 
W Groovy używamy `MarkupBuilder`, który pozwala generować XML-e tak łatwo jak JSON-y.

utworz klasę `src/main/groovy/XmlGenerator.groovy`:
 
````
import groovy.xml.MarkupBuilder

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

println "Wygenerowany XML:"
println writer.toString()
````

Test Spocka weryfikuje, że wygenerowany XML ma korzeń records i dokładnie dwóch użytkowników z poprawnymi atrybutami i polami.
i odpowiadajacy Groovy Test:
`src/test/groovy/XmlGeneratorSpec.groovy`



Zadanie 4: Test Spocka (Weryfikacja danych)
-------------------------------------------

Stwórz `src/test/groovy/DataSpec.groovy`:

``````
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
``````

Dlaczego to są "Practices"?

Brak redundancji: 
Nie musisz tworzyć klas Company, Employee, Skill tylko po to, by odebrać dane z API. 
Używasz Slurpera, wyciągasz co trzeba i idziesz dalej.

GPath:
Styl dobierania się do danych w XML (root.item.find { ... }) jest niesamowicie podobny do operacji na kolekcjach, co ujednolica Twój kod.

Readable Builders: 
Twoje buildery XML/JSON służą jako żywa dokumentacja formatu. 
Od razu widać, jak wygląda struktura wyjściowa.

Wyzwanie Finałowe Lab 11:
Mamy JSON z listą produktów: [{"n": "Laptop", "p": 4000}, {"n": "Mysz", "p": 100}].
Użyj JsonSlurper, aby go sparsować.
Użyj MarkupBuilder, aby na podstawie tych danych wygenerować XML o strukturze:


<shop>
<product name="Laptop">Cena: 4000</product>
<product name="Mysz">Cena: 100</product>
</shop>

Napisz test Spocka, który sprawdzi, czy wygenerowany XML zawiera tag <shop> i dokładnie dwa tagi <product>.
Daj znać, jak poszła konwersja JSON -> XML! To klasyczne zadanie zintegrowanych systemów (np. middleware).

Rozwiązanie wyzwania:
---------------------

Oto kompletne rozwiązanie wyzwania do Lab 11. To zadanie pokazuje, jak Groovy świetnie radzi sobie z transformacją danych między różnymi formatami ("Data Shuffling").

1. Rozwiązanie: Konwerter JSON na XML

Stwórz klasę ConverterService.groovy (lub umieść logikę bezpośrednio w teście, jak poniżej):

``````
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
``````

2. Test Spock: Weryfikacja transformacji

Stwórz plik src/test/groovy/JsonToXmlSpec.groovy. Wykorzystamy tu XmlSlurper, aby sprawdzić strukturę wygenerowanego tekstu.

code
Groovy
download
content_copy
expand_less
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
Analiza techniczna (Co tu się wydarzyło?):

GPath w XML: Zauważ, jak łatwo sprawdzamy atrybuty w teście: shop.product[0].@name. Znak @ w Groovy służy do dostępu do atrybutów XML. To znacznie prostsze niż używanie metod getAttribute() w Javie.

Dynamiczne metody Buildera: W MarkupBuilder, wywołanie product(...) nie jest zdefiniowaną metodą. Groovy używa mechanizmu methodMissing, aby zamienić każdą nazwę metody, którą wpiszesz, na tag XML.

Iteracja wewnątrz Buildera: Użyliśmy products.each { ... } bezpośrednio wewnątrz bloku xml.shop. To sprawia, że generowanie powtarzalnych elementów jest bardzo naturalne.

Co zyskaliśmy w Lab 11?

Potrafisz teraz pośredniczyć między systemami, które mówią różnymi "językami". JSON jest standardem dla nowoczesnych API (REST), a XML wciąż dominuje w starszych systemach (SOAP) oraz w konfiguracjach.

Gratulacje! Lab 11 zaliczony.

Propozycja na Lab 12: Groovy Currying & Functional Programming

W Lab 12 wejdziemy w świat programowania funkcyjnego, które Groovy wspiera od lat (zanim stało się to modne w Javie 8).
Dowiesz się:

Co to jest Currying (częściowe aplikowanie argumentów do funkcji).

Jak używać Memoization w praktyce (już o tym wspominaliśmy, ale teraz w kontekście funkcyjnym).

Jak składać funkcje (composition).

Czy interesuje Cię podejście funkcyjne, czy wolisz inny temat (np. File IO / praca z procesami systemowymi)?

