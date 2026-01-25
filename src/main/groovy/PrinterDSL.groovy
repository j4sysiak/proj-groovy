import groovy.lang.Delegate
import groovy.transform.ToString

class SimplePrinter {
    void printLowRes(String text) { println "Niska jakość: $text" }
    void connect() { println "Połączono z drukarką." }
}

@ToString(includes = ['model']) // AST wygeneruje toString() tylko dla pola model
class SmartPrinter {
    String model = "HP Laser 5000"

    // Wszystkie metody z SimplePrinter pojawią się w SmartPrinter!
    @Delegate
    SimplePrinter basicPrinter = new SimplePrinter()

    void printHighRes(String text) { println "Model $model drukuje w 4K: $text" }
}