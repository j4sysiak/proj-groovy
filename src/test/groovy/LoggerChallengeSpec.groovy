import spock.lang.Specification
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LoggerChallengeSpec extends Specification {

    def "powinien zbudować potok logowania używając currying i kompozycji"() {
        given: "1. Bazowa funkcja logująca (3 parametry)"
        def log = { String level, String date, String msg ->
            "[$level] [$date] $msg"
        }

        and: "aktualna godzina (zamrożona na potrzeby testu)"
        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        and: "2. Tworzymy wyspecjalizowaną funkcję errorLog (Currying)"
        // Używamy curry podając dwa argumenty naraz.
        // Groovy przypisze je od lewej: level="ERROR", date=now.
        // Zostaje nam funkcja przyjmująca tylko jeden argument: message.
        def errorLog = log.curry("ERROR", now)

        and: "3. Funkcja dodająca prefiks"
        def addPrefix = { String msg -> "LOG: $msg" }

        when: "4. Budujemy rurociąg (Pipeline): Prefiks -> ErrorLog"
        // Najpierw dodaj prefiks, potem przekaż wynik do errorLog
        def processingPipeline = addPrefix >> errorLog

        and: "uruchamiamy potok z surowym tekstem"
        def result = processingPipeline("Nieudane połączenie z bazą")

        then: "wynik ma poprawny format, zawiera ERROR, godzinę i prefiks"
        result == "[ERROR] [$now] LOG: Nieudane połączenie z bazą"
    }
}