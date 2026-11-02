Lab 18: Spock + Spring Boot (Integration Testing)
-------------------------------------------------

Cel: 
To jest "Święty Graal". 
Większość pracy w Javie to Spring Boot. 
Pokażę Ci, jak wstrzyknąć Beany Springa do Spocka.

Krok 1: Dodaj zależność do build.gradle (konieczne dla integracji):
-------------------------------------------------------------------

```groovy
dependencies {
    // Wersja pochodzi ze spock-bom:2.4-groovy-4.0 w build.gradle
    testImplementation 'org.spockframework:spock-spring'
    // Jeśli nie masz Springa w projekcie, normalnie byś go tu miał.
    // Zakładamy symulację lub dodaj: implementation 'org.springframework.boot:spring-boot-starter:3.1.0'
}
```


Krok 2: Symulacja komponentu Springa
------------------------------------

Stwórz `src/main/groovy/MySpringBean.groovy`

```groovy
import org.springframework.stereotype.Service

@Service
class CurrencyService {
String getCurrency() { return "PLN" }
}
```


Krok 3: Test ze Spring Context
------------------------------
Stwórz test:  `src/test/groovy/SpringIntegrationSpec.groovy`

```groovy
import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

// W prawdziwym projekcie Spring Boot wystarczy @SpringBootTest
// Tu robimy symulację kontekstu
@ContextConfiguration(classes = [CurrencyService])
class SpringIntegrationSpec extends Specification {

    @Autowired
    CurrencyService currencyService

    def "powinien wstrzyknąć beana Springa do testu Spocka"() {
        expect: "serwis został wstrzyknięty (nie jest null)"
        currencyService != null

        and: "metoda działa"
        currencyService.getCurrency() == "PLN"
    }
}
```


Uwaga: 
Ten Lab wymaga, aby Twój projekt miał zależności Springa. 
Jeśli masz czysty projekt Groovy, Krok 3 może wymagać dodania bibliotek `Spring Core/Context` do build.gradle.



Wyzwanie Finałowe (Lab 16-18):
------------------------------

Stwórz klasę DataImporter, która:
Implementuje trait Loggable.
Przyjmuje plik CSV.
Przetwarza go (IO) i jeśli znajdzie błędną linię (np. ujemna cena), używa log() z traitu do zgłoszenia błędu.
Napisz test, który przygotuje plik CSV z błędem i sprawdzi, czy metoda log została wywołana (możesz użyć Spy na klasie DataImporter!).

Rozwiązanie zadania finałowego (Lab 16-18):
-------------------------------------------
To zadanie jest świetnym sprawdzianem, bo łączy trzy różne umiejętności:

IO: 
Czytanie pliku CSV linia po linii.

OOP/Traits: 
Użycie Traita do wstrzyknięcia metody log.

Testing: 
Użycie Spy w Spocku, aby sprawdzić, czy metoda wewnętrzna (z Traita) została wywołana.

Krok 1: Kod Produkcyjny (DataImporter.groovy)
---------------------------------------------

Stwórz plik w `src/main/groovy/DataImporter.groovy`

Mamy tu Trait Loggable oraz klasę DataImporter, która go implementuje. Zauważ, że w klasie po prostu wywołujemy log(), jakby była to jej własna metoda.

```groovy
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
```


Krok 2: Test Spock (DataImporterSpec.groovy)
--------------------------------------------

Stwórz plik w `src/test/groovy/DataImporterSpec.groovy`

Tutaj kluczowe jest użycie Spy.


Mock: Udaje obiekt, nie wykonuje prawdziwego kodu (chyba że mu każesz).

Spy: To prawdziwy obiekt, który wykonuje prawdziwy kod, ale pozwala "podglądać" (szpiegować) wywołania metod. Idealny tutaj, bo chcemy, żeby logika pliku zadziałała, a jedynie chcemy sprawdzić, czy log() się odpalił.

Szpieg (Spy) wprowadzany jest w celu monitorowania i kontrolowania zachowania obiektu w trakcie testów. 
W Twoim przypadku szpieg na obiekcie DataImporter pozwala:
1. Śledzenie interakcji: Możesz sprawdzić, czy i jak często wywoływana jest metoda log() oraz jakie argumenty są do niej przekazywane. 
   Dzięki temu upewniasz się, że metoda działa zgodnie z oczekiwaniami.

2. Zachowanie oryginalnego obiektu: W przeciwieństwie do mocków, szpieg działa na prawdziwym obiekcie, więc zachowuje jego oryginalne metody i logikę. 
   Możesz testować rzeczywiste zachowanie obiektu, jednocześnie monitorując wybrane interakcje.

3. Izolacja błędów: Szpieg pozwala skupić się na testowaniu konkretnego aspektu (np. logowania błędów), bez konieczności 
   uruchamiania pełnej logiki aplikacji.

W tym teście szpieg umożliwia sprawdzenie, czy metoda log() została wywołana dla błędnych danych (ujemna cena) i czy nie została wywołana 
 dla poprawnych danych.


```groovy
import spock.lang.Specification

class DataImporterSpec extends Specification {

    def "powinien zalogować błąd gdy cena w CSV jest ujemna"() {
        given: "plik CSV z prawidłowymi i błędnymi danymi"
        def tempDir = File.createTempDir()
        def csvFile = new File(tempDir, "data.csv")
        
        csvFile.text = """Product,Price
Mleko,3.50
Chleb,-5.00
Masło,8.00"""

        and: "SZPIEG (Spy) na klasie DataImporter"
        // Tworzymy prawdziwy obiekt, ale owinięty w Spy
        def importer = Spy(new DataImporter())

        when: "uruchamiamy import"
        importer.importCsv(csvFile)

        then: "metoda log() z Traita została wywołana dla błędnej linii"
        // Sprawdzamy interakcję: 1 raz, z argumentem zawierającym słowo 'Chleb'
        1 * importer.log({ msg -> 
            msg.contains("Chleb") && msg.contains("ujemną cenę") 
        })

        and: "dla poprawnych linii log NIE został wywołany (opcjonalne sprawdzenie)"
        0 * importer.log({ msg -> msg.contains("Mleko") })

        cleanup: "sprzątamy pliki"
        tempDir.deleteDir()
    }
}
```

Dlaczego to jest ważne?

Ten wzorzec (Spy + Trait) jest bardzo częsty w Legacy Code. 
Masz starą klasę, która robi trudne rzeczy (IO, Baza). 
Chcesz sprawdzić, czy obsługuje błędy, ale nie masz łatwego sposobu na sprawdzenie "czy błąd został obsłużony" (bo metoda zwraca void).
Dzięki Spy, możesz sprawdzić efekt uboczny (wywołanie metody log), nie zmieniając ani linijki kodu produkcyjnego pod testy.

 
