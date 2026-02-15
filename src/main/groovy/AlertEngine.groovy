import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import groovy.lang.Binding
import groovy.lang.GroovyShell

class AlertEngine {

    static String evaluateAlert(String scriptSource, Map context) {
        def secure = new SecureASTCustomizer()

        secure.with {
            // Tutaj małe 'l' (zgodnie z podpowiedzią błędu)
            statementsBlacklist = [WhileStatement, ForStatement, DoWhileStatement]

            // Tutaj duże 'L' (zgodnie z podpowiedzią z poprzedniego kroku)
            receiversClassesBlackList = [System, Runtime, File, Thread]

            indirectImportCheckEnabled = true
        }

        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(secure)

        def binding = new Binding(context)
        def shell = new GroovyShell(binding, config)

        return shell.evaluate(scriptSource).toString()
    }
}