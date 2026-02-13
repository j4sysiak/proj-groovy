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