To jest temat, który oddziela programistów od architektów systemów. 
Wiele nowoczesnych platform (np. systemy bankowe, silniki BPM jak Camunda czy narzędzia ETL) pozwala na tzw. Runtime Scripting.

Wyobraź sobie, że Twój system musi wyliczać zniżkę, ale biznes chce zmieniać zasady co tydzień. 
Zamiast wydawać nową wersję aplikacji, pozwalasz im wgrać skrypt Groovy, który aplikacja uruchamia "w locie".

Lab 22: Dynamiczne Reguły Biznesowe (GroovyShell & Security)
------------------------------------------------------------

Cel: 
Nauczysz się bezpiecznie uruchamiać skrypty Groovy przekazane jako String, przekazywać do nich dane i odbierać wyniki.

Krok 1: Podstawowy Silnik Reguł (GroovyShell)
---------------------------------------------

GroovyShell to brama do dynamicznego świata. Pozwala skompilować i uruchomić kod przekazany w czasie działania aplikacji.

Stwórz plik `src/main/groovy/DiscountEngine.groovy`

```groovy
import groovy.lang.Binding
import groovy.lang.GroovyShell

class DiscountEngine {

    static BigDecimal applyRule(String scriptSource, Map context) {
        // Binding służy do przekazywania zmiennych do skryptu
        Binding binding = new Binding(context)
        
        // GroovyShell kompiluje i uruchamia skrypt
        GroovyShell shell = new GroovyShell(binding)
        
        return (BigDecimal) shell.evaluate(scriptSource)
    }
}
```

Krok 2: Bezpieczeństwo (SecureASTCustomizer)
--------------------------------------------

Uwaga! Uruchamianie surowych skryptów od użytkownika jest skrajnie niebezpieczne. 
Ktoś mógłby napisać: System.exit(0) i wyłączyć Ci serwer.

Musimy stworzyć "piaskownicę" (sandbox), która zabroni używania niebezpiecznych metod.

Dodaj do DiscountEngine.groovy metodę z zabezpieczeniami:

```groovy
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

class SecureDiscountEngine {

    static BigDecimal applySecureRule(String scriptSource, Map context) {
        // Konfiguracja bezpieczeństwa
        def secure = new SecureASTCustomizer()
        
        // Zabraniamy używania System, Runtime, itp.
        secure.with {
            receiversClassesBlackList = [System, Runtime, File, Thread] as List
            // Możemy też zabronić importów
            importsBlacklist = ['java.io.*', 'java.lang.reflect.*']
        }

        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(secure)

        Binding binding = new Binding(context)
        // Shell z naszą bezpieczną konfiguracją
        GroovyShell shell = new GroovyShell(binding, config)
        
        return (BigDecimal) shell.evaluate(scriptSource)
    }
}
```

Krok 3: Test Spocka (Weryfikacja Reguł)
---------------------------------------

Stwórz `src/test/groovy/RuleEngineSpec.groovy`

```groovy
class RuleEngineSpec extends Specification {

    def "powinien obliczyć zniżkę na podstawie dynamicznego skryptu"() {
        given: "skrypt z logiką biznesową (np. z bazy danych)"
        def script = """
            if (amount > 1000) {
                return amount * 0.10 // 10% zniżki
            } else {
                return amount * 0.05 // 5% zniżki
            }
        """

        and: "kontekst z danymi"
        def context = [amount: 2000]

        when:
        def discount = DiscountEngine.applyRule(script, context)

        then:
        discount == 200.0
    }

    def "bezpieczny silnik powinien zablokować próbę wyłączenia systemu"() {
        given: "złośliwy skrypt"
        def maliciousScript = "System.exit(0)"
        
        when:
        SecureDiscountEngine.applySecureRule(maliciousScript, [amount: 100])

        then: "powinien zostać rzucony błąd bezpieczeństwa"
        // Groovy rzuci SecurityException lub błąd kompilacji
        thrown(Exception)
    }
}
```

Dlaczego to jest ważne (The "Practice")?

Flexibility (Elastyczność): 
Twoja aplikacja staje się platformą. 
Możesz zmieniać algorytmy (podatki, zniżki, walidacje) bez rekompilacji kodu w Javie.

Binding (Kontekst): 
Dzięki Binding skrypt nie musi wiedzieć, skąd pochodzą dane. 
On po prostu używa zmiennej amount. 
To pozwala na czyste oddzielenie danych od logiki.

Sandboxing (Bezpieczeństwo): 
To kluczowy aspekt. 
Prawdziwe systemy muszą używać SecureASTCustomizer, aby uniemożliwić wykonanie kodu, który mógłby uszkodzić serwer (tzw. RCE - Remote Code Execution).



Wyzwanie Finałowe Lab 22:
-------------------------

Stwórz system "Alertów Temperatury".

Skrypt od użytkownika ma sprawdzać temperaturę i zwracać String: "NORMAL", "WARM" lub "DANGER".

Przykład skryptu: if (temp > 50) return "DANGER" else return "NORMAL".

Napisz test w Spocku, który prześle do SecureDiscountEngine (możesz go nazwać AlertEngine) temperaturę 60 stopni i sprawdzi, czy wynik to "DANGER".

Dodaj do SecureASTCustomizer zakaz używania słowa kluczowego while (aby zapobiec nieskończonym pętlom, które zawieszą serwer).

Podpowiedź: secure.whileAnswers = false.

To jest zadanie na poziomie architektonicznym. Daj znać, jak poszło z blokowaniem pętli while!