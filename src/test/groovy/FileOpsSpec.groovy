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
}