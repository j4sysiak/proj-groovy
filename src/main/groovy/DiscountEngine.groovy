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