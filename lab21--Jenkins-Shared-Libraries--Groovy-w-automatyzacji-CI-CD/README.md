Wkraczamy w świat DevOps. 
Jeśli kiedykolwiek słyszałeś o "Pipeline as Code", to sercem tego rozwiązania w Jenkinsie jest właśnie Groovy.

W dużych firmach nie kopiuje się kodu pomiędzy dziesiątkami plików Jenkinsfile. 
Zamiast tego tworzy się `Jenkins Shared Libraries (JSL)` – wspólną bibliotekę skryptów, 
którą importuje się jedną linijką.

Lab 21: Jenkins Shared Libraries – Groovy w automatyzacji CI/CD
---------------------------------------------------------------

Cel: 
Nauczysz się specyficznej struktury bibliotek Jenkinsa, mechanizmu metody call() 
(dzięki której skrypt zachowuje się jak funkcja) oraz symulacji rurociągu (Pipeline).

Krok 1: Struktura biblioteki
----------------------------

Jenkins narzuca specyficzną strukturę folderów. My zasymulujemy ją w naszym projekcie:

src/ – tutaj trafiają normalne klasy Groovy (logika pomocnicza).

vars/ – tutaj trafiają skrypty, które definiują "kroki" (steps) rurociągu. Każdy plik .groovy w tym folderze staje się globalną komendą.

Zadanie A: Klasa pomocnicza (src)
---------------------------------

Stwórz plik `src/main/groovy/pl/devops/VersionTool.groovy`

```groovy
package pl.devops

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VersionTool {
    static String getSnapshotVersion(String baseVersion) {
        // Używamy nowoczesnego API daty z Javy
        def datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        return "${baseVersion}-SNAPSHOT-${datePart}"
    }
}
```

Zadanie B: Globalny krok rurociągu (vars)
-----------------------------------------

W Jenkinsie, jeśli stworzysz plik vars/buildApp.groovy, będziesz mógł go wywołać w Pipeline jako buildApp(). 
Kluczem jest metoda call.

Stwórz plik `src/main/groovy/buildApp.groovy` (w prawdziwym Jenkinsie byłoby to w folderze vars bez package):

```groovy
// Symulujemy krok rurociągu
def call(Map config = [:]) {
    def appName = config.name ?: "Unnamed-App"
    def env = config.env ?: "dev"

    println "[JENKINS] Rozpoczynam budowanie aplikacji: ${appName.toUpperCase()}"
    println "[JENKINS] Środowisko docelowe: ${env}"

    if (env == "prod") {
        println "[WARNING] Budowanie na PRODUKCJĘ wymaga dodatkowych uprawnień!"
    }

    // Wykorzystujemy klasę z src/
    // W Jenkinsie trzeba by zaimportować: import pl.devops.VersionTool
    return "BUILD_SUCCESS_${appName}_${env}"
}
```
 
Krok 2: Symulacja Jenkinsfile (Skrypt)
--------------------------------------

Teraz stworzymy skrypt, który udaje prawdziwy rurociąg Jenkinsa, wykorzystując nasze nowe narzędzia.

Stwórz `src/main/groovy/JenkinsfileSimulator.groovy`

```groovy
// 1. Definiujemy "Pipeline" (DSL style)
def runPipeline() {
    println "--- START CI/CD PIPELINE ---"

    // Wywołujemy nasz "globalny krok" (tak jakbyśmy byli w Jenkinsie)
    def status = new buildApp().call(name: "OrderService", env: "prod")

    println "Status końcowy: $status"
    println "--- KONIEC CI/CD PIPELINE ---"
}

runPipeline()
```


Krok 3: Testowanie biblioteki w Spocku
--------------------------------------
Największym błędem w DevOps jest brak testów dla skryptów automatyzacji. 
My przetestujemy nasz "krok rurociągu" jak zwykły kod.

Stwórz `src/test/groovy/JenkinsLibrarySpec.groovy`

```groovy
import spock.lang.Specification
import pl.devops.VersionTool

class JenkinsLibrarySpec extends Specification {

    def "VersionTool powinien generować poprawny format snapshotu"() {
        expect:
        VersionTool.getSnapshotVersion("1.0") ==~ /1.0-SNAPSHOT-\d{8}/
    }

    def "buildApp powinien poprawnie przetworzyć konfigurację"() {
        given: "nasz skrypt z vars/"
        def buildStep = new buildApp()

        when: "wywołujemy go z parametrami"
        def result = buildStep.call(name: "PaymentApi", env: "staging")

        then: "zwraca oczekiwany status"
        result == "BUILD_SUCCESS_PaymentApi_staging"
    }

    def "buildApp powinien użyć domyślnych wartości gdy brakuje mapy"() {
        given:
        def buildStep = new buildApp()

        when:
        def result = buildStep.call()

        then:
        result.contains("Unnamed-App")
        result.contains("dev")
    }
}
```

Dlaczego to jest ważne (The "Practice")?

Metoda call(): 
To serce JSL. 
Dzięki niej plik skryptu staje się "callable". 
Możesz przekazywać do niej Mapę parametrów lub nawet Closure (blok kodu).

Podział src vs vars: 
Logika biznesowa (np. obliczanie wersji, parsowanie logów) powinna być w src (testowalne, obiektowe). 
Skrypty w vars powinny być tylko "łącznikami" wywołującymi kroki Jenkinsa.

Testowalność: 
Programiści DevOps często testują skrypty bezpośrednio na serwerze Jenkins ("push i módl się"). 
Dzięki Groovy możesz przetestować logikę rurociągu lokalnie w Spocku, oszczędzając godziny pracy.




Wyzwanie Finałowe Lab 21:

Stwórz nowy krok w vars/ o nazwie notify.groovy.

Metoda call powinna przyjmować Mapę z kluczami: status (np. 'SUCCESS', 'FAILURE') oraz channel (np. '#alerts').

Jeśli status to FAILURE, niech wypisuje: [SLACK] Wysyłam alert do #alerts: Build FAILED!.

Jeśli status to SUCCESS, niech wypisuje: [SLACK] Build OK.

Napisz test Spocka, który sprawdzi oba scenariusze.

Daj znać, jak poszło z Twoim pierwszym "krokiem" DevOpsowym! To umiejętność bardzo poszukiwana na rynku.