import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import groovy.lang.Binding
import groovy.lang.GroovyShell

class AlertEngine {

    static String evaluateAlert(String scriptSource, Map context) {
        // 1. Konfiguracja bezpiecznego Customizera
        def secure = new SecureASTCustomizer()

        secure.with {
            // Blokada groźnych klas (Sandbox)
            receiversClassesBlackList = [System, Runtime, File, Thread, Object] as List

            // Blokada importów
            indirectImportCheckEnabled = true

            // WYZWANIE: Blokada pętli while (zapobieganie zawieszeniu procesora)
            whileStatementsAllowed = false

            // Dobra praktyka: blokujemy też inne pętle dla pełnego bezpieczeństwa
            forStatementsAllowed = false
        }

        // 2. Podpięcie customizera do konfiguracji kompilatora
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(secure)

        // 3. Przygotowanie danych i uruchomienie
        def binding = new Binding(context)
        def shell = new GroovyShell(binding, config)

        // Evaluate zwraca wynik ostatniej instrukcji skryptu
        return shell.evaluate(scriptSource).toString()
    }
}