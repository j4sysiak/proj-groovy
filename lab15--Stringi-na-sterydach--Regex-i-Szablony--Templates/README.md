Lab 15: Stringi na sterydach – Regex i Szablony (Templates)
-----------------------------------------------------------

W Javie praca z wyrażeniami regularnymi (Regex) jest toporna 
(Pattern, Matcher, uciekanie backslashy \\d).
W Groovy to jest wbudowane w język. 
Do tego dołożymy `Silniki Szablonów`, żebyś mógł generować maile lub raporty HTML.


Cel: 
Nauczysz się operatorów ==~ (match) i =~ (find) oraz dowiesz się, jak generować dynamiczne dokumenty (np. e-maile) 
przy użyciu `SimpleTemplateEngine`.

Zadanie 1: Regex "Groovy Style" – Operator Find (=~)
----------------------------------------------------

Wyobraź sobie, że dostajesz brudny tekst (logi, nieustrukturyzowany zrzut) i musisz wyciągnąć z niego konkretne dane, np. numery zamówień.

Co masz zrobić:
Stwórz plik `src/main/groovy/RegexDemo.groovy`:

```groovy
class RegexDemo {
static List extractOrders(String logText) {
// Wzorzec: Słowo "Order", myślnik, cyfry.
// W Groovy używamy /.../ zamiast "...", żeby nie musieć pisać podwójnych slashy (\\d)
def pattern = /Order-(\d+)/

        // Operator =~ tworzy Matcher
        def matcher = (logText =~ pattern)
        
        // matcher[0] -> całe dopasowanie i grupy pierwszego znalezienia
        // matcher[0][1] -> zawartość pierwszej grupy nawiasowej (\d+)
        
        // Pobieramy wszystkie numery (grupa 1) z każdego dopasowania
        def orders = []
        matcher.each { match, number ->
            orders << number
        }
        
        return orders
    }
}
```

Stwórz plik `src/test/groovy/RegexDemoSpec.groovy`:

Oto profesjonalny test w Spocku dla Zadania 1.
Zastosujemy tu podejście Data Driven Testing (tabela where:), 
aby w jednym teście sprawdzić od razu kilka scenariuszy:

Pojedyncze zamówienie.
Wiele zamówień w jednym tekście.
Brak zamówień.
Błędny format (np. "Order-ABC", który nie powinien zostać złapany).


```groovy
import spock.lang.Specification
import spock.lang.Unroll

class RegexDemoSpec extends Specification {

    @Unroll // Rozwija każdy wiersz tabeli jako osobny test w raporcie
    def "Zadanie 1: powinien wyciągnąć numery zamówień z tekstu '#logInput'"() {
        when: "uruchamiamy ekstraktor na podanym tekście"
        def ids = RegexDemo.extractOrders(logInput)

        then: "wynikowa lista zawiera tylko oczekiwane numery ID"
        ids == expectedIds

        where: "zdefiniowane scenariusze testowe"
        logInput                                            || expectedIds
        "System created Order-1234 successfully."           || ["1234"]
        "Error in Order-55. Retrying Order-99... Success."  || ["55", "99"]
        "Order-1, Order-2, Order-3"                         || ["1", "2", "3"]
        "Brak zamówień w tym logu."                         || []
        "Order-ABC (błędny format), Order- (pusty)"         || [] 
        "Order-100 mixed with Order-200"                    || ["100", "200"]
    }
}
```

Dlaczego ten test jest dobry?
Pokrycie przypadków brzegowych: 

 - Ostatnie wiersze tabeli sprawdzają, czy Twój Regex Order-(\d+) jest poprawny.
 - Order-ABC nie zostanie złapany (bo \d+ wymaga cyfr).
 - Order- nie zostanie złapany (bo + wymaga co najmniej jednej cyfry).

Czytelność: W kolumnie expectedIds od razu widzisz, co funkcja ma zwrócić.

`@Unroll`: 
Dzięki tej adnotacji, jeśli test wywali się np. tylko dla trzeciego wiersza, 
w IntelliJ zobaczysz dokładnie, który przypadek zawiódł, a reszta pozostanie zielona.




Zadanie 2: Walidacja Formatu – Operator Match (==~)
---------------------------------------------------

Operator ==~ zwraca true tylko wtedy, gdy cały ciąg pasuje do wzorca (dokładne dopasowanie). 
To idealne do walidacji.

Dodaj do klasy `RegexDemo.groovy`:

```groovy
static boolean isValidPostalCode(String code) {
  // Format XX-XXX
  return code ==~ /\d{2}-\d{3}/
}

static boolean isSecurePassword(String pass) {
   // Min 8 znaków, duża litera, cyfra (uproszczone)
   return pass ==~ /(?=.*\d)(?=.*[A-Z]).{8,}/
}
```

Test Spock dla Zadania 2
------------------------

sprawdzamy dwie metody walidacyjne: `isValidPostalCode` oraz `isSecurePassword` 
Użyjemy tutaj adnotacji `@Unroll`, dzięki której każdy wiersz tabeli where: zostanie zaraportowany jako osobny test. 
To pozwala błyskawicznie zobaczyć, który konkretnie przypadek (np. brak dużej litery w haśle) nie działa.

Dopisz to do pliku `src/test/groovy/RegexDemoSpec.groovy` 

```groovy
import spock.lang.Specification
import spock.lang.Unroll

class RegexValidationSpec extends Specification {

    @Unroll
    def "Zadanie 2: kod pocztowy '#code' powinien być walidowany jako #expectedResult"() {
        expect: "metoda walidująca zwraca oczekiwany rezultat"
        RegexDemo.isValidPostalCode(code) == expectedResult

        where: "zestaw kodów poprawnych i błędnych"
        code       || expectedResult
        "00-950"   || true
        "12-345"   || true
        "12345"    || false // Błąd: brak myślnika
        "12-34"    || false // Błąd: za krótki drugi człon
        "12-3456"  || false // Błąd: za długi drugi człon
        "AB-CDE"   || false // Błąd: litery zamiast cyfr
        "123-45"   || false // Błąd: zły format (3 cyfry na początku)
        ""         || false // Błąd: pusty ciąg
    }

    @Unroll
    def "Zadanie 2: hasło '#password' (secure: #isSecure) - weryfikacja regex"() {
        expect:
        RegexDemo.isSecurePassword(password) == isSecure

        where: "tabela przypadków brzegowych dla regexa /(?=.*\\d)(?=.*[A-Z]).{8,}/"
        password          || isSecure
        "Haslo123"        || true  // OK: 8 znaków, duża litera, cyfra
        "SuperTajne1!"    || true  // OK: spełnia warunki + znak specjalny
        "12345678"        || false // Błąd: brak dużej litery
        "samoMaleLiter1"  || false // Błąd: brak dużej litery
        "BEZCYFR"         || false // Błąd: brak cyfry
        "Krot1"           || false // Błąd: za krótkie (<8 znaków)
        "HasloBezCyfr"    || false // Błąd: brak cyfry
    }
}
```

Co dokładnie testujemy?
Dla kodu pocztowego (\d{2}-\d{3}):
Sprawdzamy dokładnie ten format: dwie cyfry, myślnik, trzy cyfry. Każde odstępstwo (litery, brak myślnika, zła długość) musi zwrócić false.

Dla hasła ((?=.*\d)(?=.*[A-Z]).{8,}):

(?=.*\d) - Czy zawiera cyfrę? (Testujemy przypadek HasloBezCyfr -> false)

(?=.*[A-Z]) - Czy zawiera dużą literę? (Testujemy samoMaleLiter1 -> false)

.{8,} - Czy ma minimum 8 znaków? (Testujemy Krot1 -> false)

Uruchom ten test. Dzięki @Unroll w wynikach testów w IntelliJ zobaczysz piękną listę 15 osobnych "ptaszków" ✅ dla każdego przypadku z tabeli.



Zadanie 3: Template Engine – Generowanie e-maili
------------------------------------------------

Zamiast sklejać Stringi plusem ("Witaj " + name + "!"), użyjemy silnika szablonów. 
To podstawa generowania raportów, maili i konfiguracji.

utwórz klasę: `src/main/groovy/TemplateGenerator.groovy`

```groovy
import groovy.text.SimpleTemplateEngine

class TemplateGenerator {
static String generateWelcomeEmail(String userName, List products) {
// Szablon wygląda jak zwykły tekst z wstawkami ${...} lub kodem <% ... %>
def templateText = '''
Witaj ${user}!

            Dziękujemy za zakupy w naszym sklepie.
            Twoje produkty:
            <% products.each { prod -> %>
              - ${prod}
            <% } %>
            
            Suma pozycji: ${products.size()}
            Pozdrawiamy, Zespół.
        '''

        // Dane do wstrzyknięcia (Binding)
        def binding = [
            user: userName,
            products: products
        ]

        def engine = new SimpleTemplateEngine()
        return engine.createTemplate(templateText).make(binding).toString()
    }
}
```

Rozwiązanie Zadania 3: Test Spocka: TemplateSpec.groovy
-------------------------------------------------------

Oto test Spock dla Zadania 3, który weryfikuje działanie silnika szablonów.
Test sprawdzi trzy kluczowe rzeczy:
1. Czy zmienne proste (${user}) zostały podmienione.
2. Czy logika pętli (<% products.each ... %>) wygenerowała listę.
3. Czy nie zostały śmieci w tekście (czyli surowy kod szablonu).

kod w pliku `src/test/groovy/TemplateSpec.groovy` 

```groovy
import spock.lang.Specification

class TemplateSpec extends Specification {

    def "powinien wygenerować spersonalizowany e-mail używając SimpleTemplateEngine"() {
        given: "dane wejściowe do szablonu"
        def userName = "Krzysztof"
        def productList = ["Kawa", "Mleko", "Ciasteczka"]

        when: "generujemy treść maila"
        String emailContent = TemplateGenerator.generateWelcomeEmail(userName, productList)

        // Wypiszmy wynik na konsolę, żebyś zobaczył efekt końcowy!
        println "--- WYGENEROWANY EMAIL ---\n$emailContent\n--------------------------"

        then: "treść zawiera powitanie z imieniem"
        emailContent.contains("Witaj Krzysztof!")

        and: "treść zawiera wylistowane produkty (efekt pętli w szablonie)"
        emailContent.contains("- Kawa")
        emailContent.contains("- Mleko")
        emailContent.contains("- Ciasteczka")

        and: "treść zawiera poprawnie obliczoną sumę"
        emailContent.contains("Suma pozycji: 3")

        and: "w wynikowym tekście NIE MA już surowych znaczników szablonu"
        !emailContent.contains("<%")
        !emailContent.contains('${')
    }

    def "powinien poprawnie obsłużyć pustą listę produktów"() {
        given:
        def user = "Anna"
        def emptyList = []

        when:
        def email = TemplateGenerator.generateWelcomeEmail(user, emptyList)

        then:
        email.contains("Witaj Anna!")
        email.contains("Suma pozycji: 0")
        !email.contains("- ") // Nie powinno być żadnych myślników z listy
    }
}
```

Co ten test udowadnia?

Udowadnia, że oddzieliłeś widok (szablon tekstowy) od logiki (kod Java/Groovy). 
To jest fundament architektury MVC. 
W Javie generowanie takiego tekstu wymagałoby sklejania Stringów w pętlach (StringBuilder), 
co jest mało czytelne. 
Tutaj szablon wygląda prawie jak gotowy e-mail.



Zadanie 4: Test Spocka (Regex & Templates)
------------------------------------------

Sprawdźmy, czy nasze wyrażenia regularne i szablony działają poprawnie.
Stwórz `src/test/groovy/TextSpec.groovy`

```groovy
import spock.lang.Specification

class TextSpec extends Specification {

    def "powinien wyciągnąć numery zamówień z brudnego logu"() {
        given:
        def log = "INFO: User logged in. Created Order-5521. Error in Order-99. Retry Order-101 success."

        when:
        def ids = RegexDemo.extractOrders(log)

        then:
        ids.size() == 3
        ids == ["5521", "99", "101"]
    }

    def "powinien walidować kody pocztowe operatorem match (==~)"() {
        expect:
        RegexDemo.isValidPostalCode(code) == result

        where:
        code      | result
        "00-950"  | true
        "00950"   | false  // brak myślnika
        "A1-200"  | false  // litera zamiast cyfry
        "12-3456" | false  // za długi
    }

    def "powinien wygenerować e-mail z listy produktów"() {
        when:
        def email = TemplateGenerator.generateWelcomeEmail("Jan", ["Laptop", "Myszka"])

        then:
        println email // Zobacz w konsoli jak ładnie wygląda!
        
        email.contains("Witaj Jan!")
        email.contains("- Laptop")
        email.contains("- Myszka")
        !email.contains("<%") // Upewniamy się, że kod szablonu zniknął
    }
}
```

Dlaczego to jest ważne?
Slashy Strings (/pattern/): 
W Javie musisz pisać Pattern.compile("\\d+"). 
W Groovy piszesz /\d+/. 
Koniec z "piekłem backslashy".

Extraction: 
Wyciąganie danych (=~) w Groovy jest banalnie proste dzięki iteratorowi matcher.each { fullMatch, group1 -> ... }.

Szablony: 
Wbudowany `SimpleTemplateEngine` to potężne narzędzie. 
Nie musisz zaciągać wielkich bibliotek jak Velocity czy Freemarker do prostych zadań.

Wyzwanie Finałowe Lab 15:
-------------------------

Twój szef chce prostego parsera plików konfiguracyjnych.

Dostajesz tekst:

```text
    db.host=localhost
    db.port=5432
    # To jest komentarz
    app.name=GroovyApp
```

Napisz metodę `parseConfig(String text)`, która użyje Regexa, aby:
Ignorować linie zaczynające się od #.
Wyciągnąć pary klucz i wartość (Regex: /(.+)=(.+)/).
Zwróć wynik jako Map.
Napisz test Spocka, który sprawdzi, czy map.db.port == "5432" oraz czy mapa nie zawiera klucza z komentarza.


Rozwiązanie:
------------

Oto kompletne rozwiązanie Wyzwania Finałowego Lab 15.
To zadanie łączy w sobie przetwarzanie tekstu linia po linii (eachLine) 
z potęgą wyrażeń regularnych do ekstrakcji danych (=~).

1. Rozwiązanie: Klasa ConfigParser
Stwórz plik `src/main/groovy/ConfigParser.groovy`.

```groovy
class ConfigParser {

    static Map<String, String> parseConfig(String configText) {
        def config = [:]

        // Przetwarzamy tekst linia po linii
        configText.eachLine { line ->
            line = line.trim() // Usuwamy białe znaki z początku/końca

            // 1. Ignorujemy puste linie i komentarze (zaczynające się od #)
            if (line.isEmpty() || line.startsWith("#")) {
                return // idź do następnej linii
            }

            // 2. Definiujemy Regex z dwiema grupami: (klucz)=(wartość)
            def pattern = /(.+)=(.+)/
            def matcher = (line =~ pattern)

            // 3. Jeśli linia pasuje do wzorca, wyciągamy dane
            if (matcher) {
                // matcher[0] to całe dopasowanie
                // matcher[0][1] to pierwsza grupa (klucz)
                // matcher[0][2] to druga grupa (wartość)
                def key = matcher[0][1].trim()
                def value = matcher[0][2].trim()

                config[key] = value
            }
        }

        return config
    }
}
```

2. Test Spock: Weryfikacja parsera

Stwórz plik src/test/groovy/ConfigParserSpec.groovy.

```groovy
import spock.lang.Specification

class ConfigParserSpec extends Specification {

    def "powinien sparsować plik konfiguracyjny ignorując komentarze"() {
        given: "tekst konfiguracyjny (np. wczytany z pliku)"
        def rawConfig = """
            db.host=localhost
            db.port=5432
            
            # To jest komentarz - to powinno zostać pominięte
            # Kolejny komentarz
            
            app.name=GroovyApp
            timeout = 3000 
        """

        when: "parsujemy konfigurację"
        def result = ConfigParser.parseConfig(rawConfig)

        then: "mapa zawiera poprawne pary klucz-wartość"
        result['db.host'] == 'localhost'
        result['db.port'] == '5432'
        result['app.name'] == 'GroovyApp'
        
        and: "parser poradził sobie ze spacjami (dzięki .trim())"
        result['timeout'] == '3000'

        and: "mapa ma rozmiar 4 (host, port, name, timeout)"
        result.size() == 4

        and: "mapa NIE zawiera treści komentarzy"
        !result.keySet().any { it.contains("komentarz") }
    }
}
```

Analiza rozwiązania (Dlaczego to jest "Groovy Style"?):

eachLine: 
Zamiast tworzyć BufferedReader, InputStreamReader i pętle while, 
metoda eachLine na Stringu robi to automatycznie.

Regex Extraction (=~):
matcher[0][1] – to esencja wygody Groovy'ego. 
W Javie musiałbyś robić matcher.find(), a potem matcher.group(1). 
Tutaj traktujesz wynik dopasowania jak tablicę wielowymiarową.

Elastyczność mapy: 
Zapis config[key] = value jest znacznie czytelniejszy niż config.put(key, value).