// 1. Definicja Traita
trait Loggable {
    void log(String message) {
        // W prawdziwym życiu tu byłby Log4j / Slf4j
        println "[SYSTEM LOG]: $message"
    }
}

// 2. Klasa implementująca Trait
class DataImporter implements Loggable {

    void importCsv(File csvFile) {
        if (!csvFile.exists()) return

        // Czytamy plik linia po linii (Groovy IO)
        csvFile.eachLine { line, count ->
            // Pomijamy nagłówek (zakładamy, że linia nr 1 to nagłówek)
            if (count == 1) return

            // dziełimy linię na części
            def parts = line.split(',')

            // Zakładamy format: Produkt,Cena
            if (parts.size() >= 2) {
                def productName = parts[0].trim()
                // Konwersja String -> BigDecimal
                def price = parts[1].trim().toBigDecimal()

                // WALIDACJA: Jeśli cena ujemna -> wołamy log() z Traita
                if (price < 0) {
                    log("BŁĄD w linii $count: Produkt '$productName' ma ujemną cenę ($price)")
                } else {
                    // Tu byłaby logika zapisu do bazy...
                    // println "Importuję: $productName"
                }
            }
        }
    }
}