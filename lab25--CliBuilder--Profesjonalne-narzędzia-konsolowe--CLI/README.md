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



Wyzwanie Finałowe Lab 25:
-------------------------

Stwórz narzędzie FileShredder (bezpieczne usuwanie plików).

Parametr -f (wymagany) – ścieżka do folderu.

Flaga -d (dry-run) – jeśli podana, skrypt tylko wypisuje "Znalazłem plik X do usunięcia", ale nic nie usuwa.

Flaga -f (force) – jeśli nie podana, skrypt pyta o potwierdzenie (w teście możesz to uprościć do wypisania "Deleting...").

Zadanie: Napisz test Spocka, który zweryfikuje, czy flaga d jest poprawnie rozpoznawana jako boolean (użyj opts.d w if).

Daj znać, jak poszło z Twoim pierwszym profesjonalnym narzędziem CLI! Czy Twój skrypt wyświetla już piękną pomoc po wpisaniu --help?