Lab 6: Zastosowanie Groovy'ego do automatyzacji budowania projektu.
-------------------------------------------------------------------

To już ostatni etap naszej drogi: Lab 6 – Groovy w automatyzacji.

W tym module wyjdziemy poza pisanie klas biznesowych i testów. Zobaczymy, dlaczego Groovy stał się standardem w świecie DevOps (Jenkins, Gradle).

Lab 6: Groovy jako "Szwajcarski Scyzoryk" Automatyzacji

Cel: Stworzenie własnego zadania w Gradle oraz skryptu do przetwarzania raportów (JSON), co symuluje pracę w potokach CI/CD (Continuous Integration).

Zadanie 1: Własny Task w Gradle (build.gradle)
----------------------------------------------

W Gradle wszystko jest obiektem Groovy'ego. 
Możemy napisać własną logikę bezpośrednio w pliku budującym, aby zautomatyzować powtarzalne czynności.

Co masz zrobić:
Otwórz swój plik `build.gradle` i dopisz na samym końcu poniższy kod:

```
// Rejestrujemy nowy task o nazwie 'generateBuildInfo'
tasks.register('generateBuildInfo') {
group = 'praktyki'
description = 'Generuje plik z informacjami o buildzie'

    doLast {
        def reportFile = file("${buildDir}/build-info.txt")
        
        // Magia Groovy: tworzymy treść używając Multiline String i interpolacji
        def info = """
            BUILD REPORT
            ------------
            Project: ${project.name}
            Version: ${project.version}
            Date: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
            Java Version: ${System.getProperty('java.version')}
            Groovy Version: ${GroovySystem.version}
        """.stripIndent()

        // Zapis do pliku jedną komendą
        reportFile.parentFile.mkdirs()
        reportFile.text = info
        
        println ">>> Raport wygenerowany w: ${reportFile.path}"
    }
}
```

Jak to uruchomić?

Otwórz terminal w IntelliJ (na dole zakładka Terminal).

Wpisz: `./gradlew generateBuildInfo` (lub użyj panelu Gradle po prawej stronie, znajdziesz tam nową grupę praktyki).

Sprawdź folder build/ – powinien tam być plik `build-info.txt`.


Zadanie 2: Przetwarzanie JSON (Jenkins-style)
---------------------------------------------

W potokach Jenkins (Jenkinsfile) często musimy sparsować wynik testów lub API, aby zdecydować, 
czy "uwalić" build. 
Do tego służy `JsonSlurper`.

Co masz zrobić:
Stwórz w `src/main/groovy` plik `ReportScanner.groovy`:

code
Groovy
download
content_copy
expand_less
import groovy.json.JsonSlurper

class ReportScanner {
static void main(String[] args) {
// Symulacja wyniku z zewnętrznego systemu (np. SonarQube lub Testy)
def jsonResponse = '''
{
"status": "SUCCESS",
"metrics": {
"coverage": 75.5,
"bugs": 2,
"vulnerabilities": 0
},
"environment": "production"
}
'''

        def slurper = new JsonSlurper()
        def result = slurper.parseText(jsonResponse)

        // Logika automatyzacji
        println "Analiza raportu dla środowiska: ${result.environment}"
        
        if (result.metrics.bugs > 0) {
            println "[WARNING] Znaleziono błędy: ${result.metrics.bugs}"
        }

        if (result.metrics.coverage < 80) {
            println "[FAILURE] Coverage ${result.metrics.coverage}% jest poniżej progu 80%!"
        } else {
            println "[SUCCESS] Jakość kodu zaakceptowana."
        }
    }
}
Zadanie 3: Wyzwanie Finałowe (Połączenie klocków)

Spróbuj połączyć wiedzę ze wszystkich Labów:

Zmodyfikuj task generateBuildInfo w build.gradle tak, aby przed zapisem do pliku sprawdził, czy folder src/test/groovy istnieje.

Jeśli istnieje, niech dopisze do raportu informację: Testy znalezione: TAK.

Wykorzystaj operator bezpiecznej nawigacji ?. lub safe indexing przy sprawdzaniu jakiejś właściwości systemowej wewnątrz taska.


Oto gotowe rozwiązanie Wyzwania Finałowego. Zmodyfikujemy plik build.gradle, aby pokazać, jak w praktyce łączy się logikę biznesową z automatyzacją budowania.

Wklej poniższy kod do swojego pliku build.gradle (zastępując poprzednią wersję taska lub dodając go jako nowy):

code
Groovy
download
content_copy
expand_less
tasks.register('generateBuildInfo') {
group = 'praktyki'
description = 'Generuje zaawansowany raport o projekcie z wykorzystaniem logiki Groovy'

    doLast {
        // 1. Sprawdzanie czy folder testów istnieje (Zadanie: Połączenie klocków)
        def testFolder = file("src/test/groovy")
        def testsFound = testFolder.exists() ? "TAK" : "NIE"

        // 2. Safe Navigation Operator (?.) - Zadanie: Wykorzystanie bezpiecznej nawigacji
        // Próbujemy pobrać właściwość 'custom.env', która może nie istnieć.
        // Jeśli nie istnieje, bezpiecznie przechodzimy do toUpperCase() (które się nie wykona)
        // a operator elvis (?:) podstawi wartość domyślną.
        def envTag = System.getProperty('custom.env')?.toUpperCase() ?: "DEVELOPMENT"

        // 3. Budowanie treści raportu (GString + interpolacja)
        def reportFile = file("${layout.buildDirectory.get()}/final-report.txt")
        
        def reportContent = """
            =========================================
            PROJEKT: ${project.name.toUpperCase()}
            =========================================
            Data buildu:    ${new Date().format('dd.MM.yyyy HH:mm')}
            Środowisko:     ${envTag}
            Testy Groovy:   ${testsFound}
            -----------------------------------------
            Statystyki plików:
            - Klasy Java:   ${fileTree('src/main/java').files.size()}
            - Klasy Groovy: ${fileTree('src/main/groovy').files.size()}
            =========================================
        """.stripIndent()

        // 4. Zapis do pliku (Groovy File Sugar)
        reportFile.parentFile.mkdirs()
        reportFile.text = reportContent

        println "------------------------------------------"
        println ">>> Finałowy raport został wygenerowany!"
        println ">>> Lokalizacja: ${reportFile.absolutePath}"
        println ">>> Wynik testów w raporcie: ${testsFound}"
        println "------------------------------------------"
    }
}
Co tutaj udowodniliśmy?

Integracja z systemem plików: Użyliśmy file("...").exists(), co jest podstawą w skryptach CI/CD (np. sprawdzenie, czy istnieją artefakty przed ich wysłaniem).

Safe Navigation (?.): Pokazałeś, że potrafisz bezpiecznie dobierać się do właściwości systemowych (System.getProperty), które często są null w różnych środowiskach. To zapobiega błędom typu NullPointerException podczas budowania projektu.

Operator Elvis (?:): Użyliśmy go, aby dostarczyć domyślną wartość ("DEVELOPMENT"), gdyby właściwość nie została podana.

Składnia Groovy w Gradle: Użyliśmy stripIndent(), GStrings oraz potężnego fileTree, który potrafi "przeskanować" projekt w jednej linijce.

Jak to przetestować?

Uruchom standardowo:

 
`./gradlew generateBuildInfo`

(W raporcie zobaczysz Testy znalezione: TAK).

Uruchom z przekazaniem parametru systemowego (żeby sprawdzić Safe Navigation):

 
`./gradlew generateBuildInfo -Dcustom.env=production`

(W raporcie zobaczysz Środowisko: PRODUCTION).

Gratulacje! Tym samym ukończyłeś pełny cykl Java-Groovy-Practices. Masz teraz solidne fundamenty, by swobodnie poruszać się w projektach używających Spocka, Gradla, czy dynamicznych skryptów Groovy wewnątrz aplikacji Java. 🚀



Podsumowanie kursu Java-Groovy-Practices
----------------------------------------

Przeszedłeś przez pełną ścieżkę:

Lab 1: Pozbyłeś się boilerplate'u (POGO).
Lab 2: Opanowałeś kolekcje i Closures (moc Groovy'ego).
Lab 3: Napisałeś profesjonalne testy w Spocku (tabele prawdy i mocki).
Lab 4: "Zhacksowałeś" Javę przez Metaprogramowanie i DSL.
Lab 5: Połączyłeś Javę z Groovym w jednym procesie.
Lab 6: Zastosowałeś Groovy'ego do automatyzacji budowania projektu.

Co teraz?
Groovy to brama do Gradle i Jenkinsa. 
Jeśli planujesz iść w stronę Seniora lub DevOpsa, znajomość tej "magii" pod maską sprawi, 
że będziesz rozumiał, jak działają nowoczesne systemy budowania, zamiast tylko kopiować skrypty z internetu.
 