Skoro Spock i kolekcje już śmigają, to czas na to, co czyni Groovy’ego "magicznym" 
i dlaczego jest tak chętnie używany do tworzenia narzędzi (jak Gradle czy Jenkins Pipeline).

Lab 4: Metaprogramowanie i DSL (Domain Specific Languages)
----------------------------------------------------------

Cel: Nauczysz się dodawać nowe metody do istniejących klas Javy (których nie możesz edytować, np. String czy Integer) 
oraz stworzysz własny mini-język (DSL).

Zadanie 1: ExpandoMetaClass – "Hacking" Javy
--------------------------------------------

W Javie, jeśli chcesz sprawdzić, czy String jest mailem, robisz StringUtils.isEmail(email). 
W Groovy możesz sprawić, żeby każdy String sam wiedział, czy jest mailem.

Co masz zrobić:
Stwórz plik `MetaprogrammingSpec.groovy` w folderze testowym:


```groovy
import spock.lang.Specification

class MetaprogrammingSpec extends Specification {

    def setupSpec() {
        // Dodajemy nową metodę do klasy String globalnie!
        String.metaClass.isEmail = { 
            delegate.contains("@") && delegate.contains(".") 
        }

        // Dodajemy metodę do klasy Integer
        Integer.metaClass.toPln = {
            return "${delegate},00 zł"
        }
    }

    def "powinien używać dynamicznie dodanych metod"() {
        expect:
        "kontakt@google.pl".isEmail() == true
        "zwykly-tekst".isEmail() == false
        
        and:
        150.toPln() == "150,00 zł"
    }
}
```

Dlaczego to jest ważne?
Pozwala to na pisanie kodu, który czyta się jak zdania w języku angielskim. 
W ten sposób zbudowane są biblioteki do testowania API czy konfiguracje serwerów.

Zadanie 2: Tworzenie własnego DSL (DelegatesTo)
-----------------------------------------------

To jest technika używana w Jenkinsfile lub build.gradle. 
Chcemy stworzyć strukturę, która pozwoli nam skonfigurować np. wysyłkę raportu w taki sposób:


// Tak ma docelowo wyglądać nasz kod:
raport {
tytul "Miesięczny"
odbiorca "szef@firma.pl"
priority 1
}

Co masz zrobić:
Dopisz poniższy kod do tego samego pliku (lub nowej klasy): `DslClosureSpec.groovy`:


```groovy
class EmailConfig {
    String title
    String to
    int prio

    void tytul(String text) { this.title = text }
    void odbiorca(String text) { this.to = text }
    void priority(int p) { this.prio = p }
}

class DslClosureSpec extends Specification {

        // Metoda pomocnicza udająca silnik DSL
        def emailBuilder(Closure block) {
            def config = new EmailConfig()
            block.delegate = config         // Wszystko co w bloku, "leci" do obiektu config
            block.resolveStrategy = Closure.DELEGATE_FIRST
            block()                         // Uruchomienie bloku
            return config
        }

        def "powinien zbudować konfigurację przez DSL"() {
            when:
            def result = emailBuilder {
                tytul "Alert"
                odbiorca "admin@dev.pl"
                priority 10
            }

            then:
            result.title == "Alert"
            result.to == "admin@dev.pl"
            result.prio == 10
        }
}
```



Twoje Wyzwanie (Zadanie 3):
---------------------------

Spróbuj rozbudować ten mechanizm.

Dodaj do klasy Integer metodę daysAgo(), która zwraca datę sprzed X dni (użyj java.time.LocalDate.now().minusDays(delegate)).

Napisz test w Spocku, który sprawdzi, czy wywołanie 7.daysAgo() zwraca poprawną datę.

Co zyskujesz w tym Labie?

Zaczynasz rozumieć, że Groovy to nie tylko "krótsza Java", ale silnik, który pozwala zmieniać zasady gry. 
Możesz "nauczyć" stare klasy Javy nowych sztuczek, co jest niesamowicie przydatne przy tworzeniu 
własnych frameworków testowych lub narzędzi automatyzacji.

Daj znać, jak poszło z daysAgo()! Jak to opanujesz, przejdziemy do integracji Java + Groovy w jednym projekcie (Lab 5).

To zadanie świetnie pokazuje, jak Groovy potrafi "rozszerzyć" możliwości czystej Javy bez dziedziczenia.

Oto kompletny kod do Twojego pliku `DaysAgoSpec.groovy` (lub nowego pliku).

```groovy
import spock.lang.Specification
import java.time.LocalDate

class DaysAgoSpec extends Specification {

    // Wykonuje się raz przed wszystkimi testami w tej klasie
    def setupSpec() {
        // Dodajemy nową metodę do klasy Integer
        // 'delegate' w tym miejscu to konkretna liczba, na której wywołujemy metodę
        Integer.metaClass.daysAgo = {
            LocalDate.now().minusDays(delegate)
        }
    }

    def "powinien zwrócić poprawną datę sprzed 7 dni"() {
        given: "oczekiwana data wyliczona standardowo"
        def expectedDate = LocalDate.now().minusDays(7)

        expect: "nasza nowa metoda na klasie Integer zwraca to samo"
        7.daysAgo() == expectedDate
    }

    def "powinien działać dla różnych wartości"() {
        expect:
        0.daysAgo() == LocalDate.now()
        1.daysAgo() == LocalDate.now().minusDays(1)
        365.daysAgo() == LocalDate.now().minusDays(365)
    }
}
```

Dlaczego to jest potężne?

Czytelność: 
Zobacz, jak wygląda kod: `7.daysAgo()`. 
Jest niemal identyczny z naturalnym językiem angielskim. To fundament tworzenia tzw. Fluent API.

Brak Utility Classes: 
W Javie musiałbyś stworzyć `DateUtils.daysAgo(7)`. 
W Groovy "uczysz" same liczby, co mają robić.

delegate: 
To kluczowe słowo w domknięciach (Closures) Groovy'ego. 
Reprezentuje obiekt, "na rzecz którego" działa funkcja. Jeśli wywołasz `10.daysAgo()`, to delegate będzie wynosił 10.

Uwaga techniczna (Ważne!):

Modyfikowanie metaClass na podstawowych klasach Javy (jak Integer, String, List) działa globalnie w obrębie Twojego uruchomienia. 
W dużych projektach należy robić to ostrożnie, ale w testach i skryptach automatyzacji (np. do generowania dat w testach) jest to praktyka numer 1.

 