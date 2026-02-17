Wkraczamy w obszar, który uczyni Twoje skrypty Groovy gotowymi do użytku profesjonalnego. 
Często piszemy skrypty do migracji danych, czyszczenia logów czy automatyzacji wdrożeń. 
Zamiast "hardkodować" ścieżki do plików, użyjemy CliBuilder, aby stworzyć narzędzia, 
które obsługują parametry tak samo jak `git`, `docker` czy `kubectl`.

Lab 25: CliBuilder – Profesjonalne narzędzia konsolowe (CLI)
------------------------------------------------------------

Cel: 
Dowiesz się, jak stworzyć skrypt, który:
 - parsuje parametry wejściowe
 - obsługuje flagi (np. -v dla verbose), 
 - argumenty opcjonalne/wymagane
 - automatycznie generuje czytelną pomoc (--help).

Przygotowanie (Zależność)

W Groovy 4 mechanizm CLI oparty jest na potężnej bibliotece Picocli. 
Upewnij się, że masz ją w build.gradle (zazwyczaj jest częścią groovy-all, ale warto sprawdzić):
testImplementation 'org.apache.groovy:groovy-cli-picocli:4.0.15'

Zadanie 1: Prosty skrypt z parametrami
--------------------------------------

Stwórz plik `src/main/groovy/GreeterTool.groovy`.
Zobacz, jak `CliBuilder` zamienia listę args w elegancki obiekt.

```groovy
import groovy.cli.picocli.CliBuilder

def cli = new CliBuilder(usage: 'GreeterTool [options] name')

// Definiujemy dostępne opcje
cli.with {
h longOpt: 'help', 'Wyświetla tę pomoc'
v longOpt: 'verbose', 'Włącza tryb szczegółowy'
l longOpt: 'lang', args: 1, argName: 'kod', 'Język (pl lub en), domyślnie en'
}

def options = cli.parse(args)

// 1. Obsługa pomocy
if (!options || options.h) {
cli.usage()
return
}

// 2. Pobieranie argumentów pozycyjnych (to co zostało po flagach)
def name = options.arguments()[0] ?: 'Nieznajomy'
def lang = options.l ?: 'en'

if (options.v) println "[DEBUG] Uruchamiam skrypt dla: $name (język: $lang)"

if (lang == 'pl') {
println "Cześć $name!"
} else {
println "Hello $name!"
}
```


Zadanie 2: Praktyczne narzędzie – LogAnalyzer
---------------------------------------------

Stworzymy narzędzie, które skanuje plik tekstowy i wypisuje linie zawierające określone słowo kluczowe.

Stwórz `src/main/groovy/LogAnalyzer.groovy`:

```groovy
import groovy.cli.picocli.CliBuilder

class LogAnalyzer {
static void main(String[] args) {
def cli = new CliBuilder(usage: 'LogAnalyzer -f <file> -k <keyword> [-o <output>]')

        cli.with {
            f longOpt: 'file', args: 1, required: true, 'Ścieżka do pliku wejściowego'
            k longOpt: 'keyword', args: 1, required: true, 'Słowo kluczowe do wyszukania'
            o longOpt: 'output', args: 1, 'Plik wynikowy (opcjonalnie)'
        }

        def opts = cli.parse(args)
        if (!opts) return // Błędy parsowania (np. brak wymaganych pól) są wypisywane automatycznie

        File inputFile = new File(opts.f)
        if (!inputFile.exists()) {
            System.err.println "BŁĄD: Plik ${opts.f} nie istnieje!"
            return
        }

        def foundLines = inputFile.readLines().findAll { it.contains(opts.k) }

        if (opts.o) {
            new File(opts.o).text = foundLines.join('\n')
            println "Wyniki zapisano do ${opts.o}"
        } else {
            foundLines.each { println it }
        }
    }
}
```

Zadanie 3: Testowanie CLI w Spocku
----------------------------------

Testowanie skryptów konsolowych jest trudne, bo operują na System.out. 
Ale możemy przetestować logikę parsowania, przekazując tablicę String[].

Stwórz `src/test/groovy/CliSpec.groovy`:

```groovy
import spock.lang.Specification
import groovy.cli.picocli.CliBuilder

class CliSpec extends Specification {

    def "powinien poprawnie sparsować wymagane parametry"() {
        given: "definicja buildera identyczna jak w skrypcie"
        def cli = new CliBuilder()
        cli.f(longOpt: 'file', args: 1, required: true)
        cli.k(longOpt: 'keyword', args: 1)

        when: "symulujemy wywołanie z terminala: -f test.log -k ERROR"
        def opts = cli.parse(['-f', 'test.log', '-k', 'ERROR'] as String[])

        then: "parametry są dostępne w obiekcie opts"
        opts.f == 'test.log'
        opts.k == 'ERROR'
    }

    def "powinien zwrócić null gdy brakuje wymaganego parametru"() {
        given:
        def cli = new CliBuilder()
        cli.f(longOpt: 'file', args: 1, required: true)

        when: "wywołujemy bez -f"
        def opts = cli.parse([] as String[])

        then: "wynik to null (automatycznie wypisując błąd na System.err)"
        opts == null
    }
}
```

Kluczowe pojęcia (The Practice):

longOpt: 
Pozwala używać zarówno krótkich (-f), jak i długich (--file) nazw. 
Daje to profesjonalny "feel" narzędzia.

args: 1: 
Informuje, że po flagi musi nastąpić dokładnie jeden argument (wartość).

required: 
true: `CliBuilder` sam zadba o walidację. 
Jeśli użytkownik zapomni o fladze, skrypt nie ruszy i wyświetli czytelną instrukcję.

cli.usage(): 
Generuje sformatowaną pomoc. 
Nigdy nie pisz "pomocy" ręcznie jako println!



Wyzwanie Finałowe Lab 25: Narzędzie "LogMasker"
-----------------------------------------------

Stwórz narzędzie FileShredder (bezpieczne usuwanie plików).

Parametr -f (wymagany) – ścieżka do folderu.

Flaga -d (dry-run) – jeśli podana, skrypt tylko wypisuje "Znalazłem plik X do usunięcia", ale nic nie usuwa.

Flaga -f (force) – jeśli nie podana, skrypt pyta o potwierdzenie (w teście możesz to uprościć do wypisania "Deleting...").

Zadanie: Napisz test Spocka, który zweryfikuje, czy flaga d jest poprawnie rozpoznawana jako boolean (użyj opts.d w if).


Rozwiązanie:
------------

Skoro masz już działający mechanizm w Gradle i terminalu, to czas na Wyzwanie Finałowe Lab 25, 
które przetestuje Twoje umiejętności tworzenia narzędzi przydatnych w codziennej pracy programisty/DevOpsa.

Stworzymy narzędzie `LogMasker`, które służy do anonimizacji logów.

Scenariusz:
Twoja firma nie może wysyłać logów do systemów zewnętrznych, jeśli zawierają one wrażliwe dane (np. numery kart kredytowych lub e-maile). 
Musisz napisać narzędzie CLI, które "wymaskuje" te dane w pliku.

Wymagania techniczne:

Parametry CLI:

-f (wymagany): ścieżka do pliku wejściowego.

-o (opcjonalny): ścieżka do pliku wynikowego (jeśli brak, wypisz na konsolę).

-m (flaga): tryb "mask-email" (jeśli podany, zamień e-maile na ****@****).

-c (flaga): tryb "mask-credit-card" (jeśli podany, zamień numery 16-cyfrowe na XXXX-XXXX-XXXX-1234).

Mechanizm:

Użyj `CliBuilder` z blokiem with { ... } (zgodnie z Groovy 4).

Użyj Regexów (=~ lub replaceAll) do podmiany tekstów.

Testy:
Napisz test Spocka, który sprawdzi, czy po podaniu flagi -m e-mail test@wp.pl faktycznie został zamieniony na maskę.

Podpowiedź (Szkielet kodu):

Klasa LogMasker.groovy:

```groovy
import groovy.cli.picocli.CliBuilder

class LogMasker {
static void main(String[] args) {
def cli = new CliBuilder(usage: 'LogMasker -f <file> [-m] [-c] [-o <output>]')
cli.with {
f(longOpt: 'file', args: 1, required: true, 'Plik wejściowy')
m(longOpt: 'mask-email', 'Maskowanie adresów e-mail')
c(longOpt: 'mask-card', 'Maskowanie kart kredytowych')
o(longOpt: 'output', args: 1, 'Plik wyjściowy')
}

        def opts = cli.parse(args)
        if (!opts) return

        File input = new File(opts.f)
        String content = input.text

        if (opts.m) {
            // Regex dla emaila: /[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}/
            content = content.replaceAll(/[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}/, "****@****")
        }
        
        if (opts.c) {
            // Regex dla karty (uproszczony): /\d{4}-\d{4}-\d{4}-(\d{4})/
            content = content.replaceAll(/\d{4}-\d{4}-\d{4}-(\d{4})/, "XXXX-XXXX-XXXX-\$1")
        }

        if (opts.o) {
            new File(opts.o).text = content
            println "Zapisano zamaskowany plik w: ${opts.o}"
        } else {
            println content
        }
    }
}
```

Twoje kroki do wykonania:

Zaimplementuj LogMasker w src/main/groovy.

Zaktualizuj build.gradle – dodaj nowy task runLogMasker (lub zmień istniejący), pamiętając o javaLauncher i mainClass.set('LogMasker').

Stwórz plik test.log z treścią:

```text
User jan@kowalski.pl logged in.
Payment processed for card 1111-2222-3333-4444.
```
Uruchom z terminala:

./gradlew runLogMasker -PappArgs="-f test.log -m -c"

Sprawdź wynik – e-mail powinien zniknąć, a karta powinna mieć widoczne tylko ostatnie 4 cyfry.




Test Spook:
-----------
Aby testowanie narzędzi CLI było profesjonalne i zgodne z zasadą "Java-Groovy-Practices", 
musimy zastosować mały trik architektoniczny: 
wydzielić logikę przetwarzania tekstu do oddzielnej metody.

Testowanie bezpośrednio metody static void main jest trudne, bo wymaga symulowania plików na dysku i przechwytywania konsoli. 
Jeśli wydzielimy logikę maskowania, będziemy mogli ją przetestować błyskawicznie w pamięci.

Krok 1: utworzenie analogicznej klasy `LogMaskerWithTestingPossibility.groovy`

Zmodyfikuj swoją klasę tak, aby zawierała metodę `maskContent`. 
Dzięki temu "serce" Twojego programu będzie łatwo testowalne.

```groovy
import groovy.cli.picocli.CliBuilder

class LogMaskerWithTestingPossibility {

    // Wydzielona logika - to będziemy testować!
    static String maskContent(String content, boolean maskEmail, boolean maskCard) {
        if (maskEmail) {
            // Regex dla emaila
            content = content.replaceAll(/[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}/, "****@****")
        }
        
        if (maskCard) {
            // Regex dla karty - $1 odnosi się do grupy (\d{4})
            content = content.replaceAll(/\d{4}-\d{4}-\d{4}-(\d{4})/, "XXXX-XXXX-XXXX-\$1")
        }
        return content
    }

    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'LogMasker -f <file> [-m] [-c] [-o <output>]')
        cli.with {
            f(longOpt: 'file', args: 1, required: true, 'Plik wejściowy')
            m(longOpt: 'mask-email', 'Maskowanie adresów e-mail')
            c(longOpt: 'mask-card', 'Maskowanie kart kredytowych')
            o(longOpt: 'output', args: 1, 'Plik wyjściowy')
        }

        def opts = cli.parse(args)
        if (!opts) return

        File input = new File(opts.f)
        if (!input.exists()) {
            println "BŁĄD: Plik nie istnieje"
            return
        }

        // Wywołujemy naszą testowalną metodę
        String result = maskContent(input.text, (boolean)opts.m, (boolean)opts.c)

        if (opts.o) {
            new File(opts.o).text = result
            println "Zapisano w: ${opts.o}"
        } else {
            println result
        }
    }
}
```

Krok 2: Test Spock (LogMaskerSpec.groovy)
-----------------------------------------

Teraz stworzymy test, który sprawdzi wszystkie kombinacje flag. 
Dzięki temu masz pewność, że Twoje wyrażenia regularne (Regex) działantją poprawnie.

Stwórz plik `src/test/groovy/LogMaskerSpec.groovy`:

```groovy
import spock.lang.Specification
import spock.lang.Unroll

class LogMaskerSpec extends Specification {

    @Unroll
    def "powinien poprawnie maskować dane: email=#maskEmail, card=#maskCard"() {
        given: "tekst wejściowy z wrażliwymi danymi"
        def input = "User jan@wp.pl kupił bilet kartą 1111-2222-3333-4444."

        when: "uruchamiamy logikę maskowania"
        def result = LogMasker.maskContent(input, maskEmail, maskCard)

        then: "wynik zawiera oczekiwane maski"
        result == expectedOutput

        where: "różne kombinacje flag"
        maskEmail | maskCard || expectedOutput
        true      | false    || "User ****@**** kupił bilet kartą 1111-2222-3333-4444."
        false     | true     || "User jan@wp.pl kupił bilet kartą XXXX-XXXX-XXXX-4444."
        true      | true     || "User ****@**** kupił bilet kartą XXXX-XXXX-XXXX-4444."
        false     | false    || "User jan@wp.pl kupił bilet kartą 1111-2222-3333-4444."
    }

    def "nie powinien nic zmieniać jeśli tekst nie zawiera maili ani kart"() {
        given:
        def cleanText = "To jest bezpieczny log systemowy."

        expect:
        LogMasker.maskContent(cleanText, true, true) == cleanText
    }
}
```

Dlaczego tak to testujemy? (Kluczowe wnioski)

Testability (Testowalność): To najważniejsza lekcja tego kursu. Dobry kod to taki, który da się przetestować w izolacji. Wydzielając maskContent od main, przetestowaliśmy logikę w 50ms, zamiast bawić się w tworzenie plików na dysku.

Regex w Spocku: Sprawdziliśmy, czy nasz regex poprawnie przechwycił grupę $1. Zauważ, że w kodzie Groovy piszemy \$1 (z backslashem), aby Groovy nie myślał, że to zmienna (GString).

Tabela Prawdy (where:): Dzięki niej w jednym teście sprawdziliśmy wszystkie 4 możliwe stany Twojego programu (oba pola na true, oba na false i mieszane).

Co teraz?

Uruchom testy w IntelliJ.

Uruchom gradlew clean build.

Jeśli wszystko jest na zielono, to znaczy, że Twój LogMasker jest w 100% gotowy do pracy na produkcji. Stałeś się programistą, który nie tylko pisze skrypty, ale pisze je zgodnie z najlepszymi praktykami inżynierskimi.

To jest idealny moment, aby ostatecznie zamknąć nasz warsztat! Gratulacje! 🚀🎓✅