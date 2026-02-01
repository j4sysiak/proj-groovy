Lab 16: Operacje na Plikach i CSV (IO Magic)
--------------------------------------------

Cel: 
W Javie czytanie plików, trawersowanie katalogów i parsowanie CSV to sporo kodu (BufferedReader, Files.walk, Stream). 
W Groovy zrobisz to, jakbyś pisał skrypt w Bashu, ale z mocą Javy.

Zadanie 1: Trawersowanie katalogów
----------------------------------

Chcemy znaleźć wszystkie pliki .txt w projekcie, które mają w nazwie słowo "error".

Co masz zrobić:
Stwórz FileOps.groovy:

```groovy
import groovy.io.FileType

class FileOps {
static List<File> findErrorLogs(String path) {
def foundFiles = []
def dir = new File(path)

        if (!dir.exists()) return []

        // Magia Groovy: traverse
        dir.traverse(type: FileType.FILES, nameFilter: ~/.*error.*\.txt/) { file ->
            println "Znalazłem plik: ${file.name}"
            foundFiles << file
        }
        return foundFiles
    }
}
```

Test do zadania 1:
------------------
Oto profesjonalny test Spock dla Zadania 1 (Trawersowanie katalogów).
Aby przetestować operacje na plikach (File I/O) w sposób bezpieczny, 
musimy stworzyć tymczasową strukturę katalogów tylko na potrzeby testu, 
a po zakończeniu ją usunąć. 
Dzięki temu test nie zaśmieca Twojego dysku.

Stwórz plik `src/test/groovy/FileOpsSpec.groovy`

```groovy
import spock.lang.Specification

class FileOpsSpec extends Specification {

    def "powinien rekurencyjnie znaleźć pliki .txt zawierające 'error' w nazwie"() {
        given: "tymczasowy folder główny"
        // Groovy dodaje metodę createTempDir do klasy File
        def tempRoot = File.createTempDir() 

        and: "zagnieżdżony podkatalog (żeby sprawdzić czy traverse wchodzi głębiej)"
        def subDir = new File(tempRoot, "backend/logs")
        subDir.mkdirs()

        and: "tworzymy pliki PASUJĄCE do wzorca (error + .txt)"
        new File(tempRoot, "critical-error.txt").createNewFile()
        new File(subDir, "api_error_2024.txt").createNewFile()

        and: "tworzymy pliki NIEPASUJĄCE (szum)"
        new File(tempRoot, "info.txt").createNewFile()        // brak "error"
        new File(tempRoot, "error.png").createNewFile()       // złe rozszerzenie
        new File(subDir, "system.log").createNewFile()        // złe rozszerzenie i brak "error"

        when: "uruchamiamy naszą metodę szukającą"
        def foundFiles = FileOps.findErrorLogs(tempRoot.absolutePath)

        then: "znaleziono dokładnie 2 pliki"
        foundFiles.size() == 2

        and: "wyniki zawierają poprawne nazwy plików"
        def fileNames = foundFiles.collect { it.name }
        fileNames.contains("critical-error.txt")
        fileNames.contains("api_error_2024.txt")

        and: "nie znaleziono plików ignorowanych"
        !fileNames.contains("info.txt")
        !fileNames.contains("error.png")

        cleanup: "sprzątamy po teście (usuwa folder ze wszystkimi plikami w środku)"
        tempRoot.deleteDir()
    }

    def "powinien zwrócić pustą listę dla nieistniejącego katalogu"() {
        when:
        def result = FileOps.findErrorLogs("C:/katalog/ktory/nie/istnieje/123")

        then:
        result.isEmpty()
    }
}
```

Dlaczego ten test jest "Practice"?

Izolacja: 
Używamy File.createTempDir(). 
Test jest niezależny od tego, co masz na pulpicie czy w dokumentach. 
Działa na każdym komputerze tak samo.

Scenariusz "Szum": 
Nie wystarczy stworzyć tylko poprawnego pliku. 
Dobry test musi sprawdzić, czy algorytm odrzuca pliki, które są podobne (np. error.png), ale nie spełniają wszystkich kryteriów.

cleanup: 
W bloku cleanup używamy tempRoot.deleteDir(). 
To metoda Groovy'ego, która rekurencyjnie usuwa katalog wraz z całą zawartością. 
Bez tego po 100 uruchomieniach testu miałbyś 100 śmieciowych folderów w systemie.

 

Zadanie 2: Przetwarzanie CSV "na piechotę"
------------------------------------------

Często musisz szybko naprawić dane w pliku CSV bez zaciągania wielkich bibliotek.

Co masz zrobić:
Dodaj do `FileOps.groovy`

```groovy
static void generateReportFromCsv(File csvFile, File reportFile) {
// Czyścimy plik wynikowy
reportFile.write("RAPORT CENOWY\n================\n")

        // Czytamy CSV linia po linii (pomijając nagłówek)
        def lines = csvFile.readLines()
        if (lines.size() < 2) return 

        def header = lines[0] // np. Product,Price,Qty
        
        lines.drop(1).each { line ->
            def parts = line.split(',')
            def product = parts[0]
            def price = parts[1].toBigDecimal()
            def qty = parts[2].toInteger()
            
            def total = price * qty
            
            // Dopisujemy do pliku (append)
            reportFile.append("Produkt: $product | Wartość magazynowa: $total PLN\n")
        }
    }
```

Rozwiązanie zadania 2:
----------------------
Oto test Spock dla Zadania 2, który weryfikuje przetwarzanie pliku CSV.
W tym teście kluczowe jest to, że nie musimy mieć fizycznego pliku CSV na dysku przed uruchomieniem testu. 
Tworzymy go "w locie" w folderze tymczasowym, wypełniamy danymi, uruchamiamy logikę i sprawdzamy wynik.

Możesz dopisać ten test do istniejącego pliku `src/test/groovy/FileOpsSpec.groovy`

```groovy
def "powinien przetworzyć plik CSV i wygenerować raport z poprawnymi obliczeniami"() {
given: "środowisko tymczasowe"
def tempDir = File.createTempDir()
def csvFile = new File(tempDir, "produkty.csv")
def reportFile = new File(tempDir, "wynik.txt")

        and: "tworzymy plik CSV z danymi testowymi (Nagłówek + 3 linie)"
        // Groovy pozwala zapisać tekst do pliku operatorem przypisania (=)
        csvFile.text = """Product,Price,Qty
Mleko,3.50,10
Chleb,5.00,20
Masło,8.20,2"""

        when: "uruchamiamy generator raportu"
        FileOps.generateReportFromCsv(csvFile, reportFile)

        then: "plik raportu został utworzony"
        reportFile.exists()

        and: "zawartość raportu jest zgodna z oczekiwaniami"
        def content = reportFile.text // Odczyt całego pliku do zmiennej

        // Sprawdzamy nagłówek
        content.contains("RAPORT CENOWY")
        
        // Sprawdzamy obliczenia:
        // Mleko: 3.50 * 10 = 35.00
        content.contains("Mleko | Wartość magazynowa: 35.0") 
        
        // Chleb: 5.00 * 20 = 100.00
        content.contains("Chleb | Wartość magazynowa: 100.0")
        
        // Masło: 8.20 * 2 = 16.40
        content.contains("Masło | Wartość magazynowa: 16.4")

        cleanup: "sprzątamy po sobie"
        tempDir.deleteDir()
    }

    def "nie powinien generować danych, jeśli plik CSV ma tylko nagłówek"() {
        given:
        def tempDir = File.createTempDir()
        def csvFile = new File(tempDir, "pusty.csv")
        def reportFile = new File(tempDir, "pusty_raport.txt")

        and: "plik zawierający tylko nagłówek"
        csvFile.text = "Product,Price,Qty"

        when:
        FileOps.generateReportFromCsv(csvFile, reportFile)

        then:
        reportFile.exists()
        // Powinien zawierać tylko nagłówek raportu, bez linii produktów
        reportFile.text.trim().endsWith("================")

        cleanup:
        tempDir.deleteDir()
    }
```

Co w tym teście jest "Groovy Style"?

csvFile.text = "...": 
W Javie musiałbyś otwierać FileWriter, pisać, zamykać, obsługiwać wyjątki. 
W Groovy przypisanie stringa do właściwości .text pliku automatycznie zapisuje treść i zamyka strumień.

def content = reportFile.text: 
Analogicznie w drugą stronę – to najszybszy sposób na wczytanie całego małego pliku do pamięci w celu asercji.

Multiline String ("""..."""): 
Pozwala nam łatwo sformatować zawartość CSV w kodzie testu, zachowując podział na linie.