import spock.lang.Specification
import spock.lang.Unroll
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class AlertEngineSpec extends Specification {

    @Unroll
    def "powinien zwrócić status #expectedStatus dla temperatury #temperature"() {
        given: "skrypt reguły temperaturowej"
        def script = """
            if (temp > 50) return "DANGER"
            else if (temp > 30) return "WARM"
            else return "NORMAL"
        """

        expect: "silnik poprawnie ocenia warunki"
        AlertEngine.evaluateAlert(script, [temp: temperature]) == expectedStatus

        where:
        temperature || expectedStatus
        60          || "DANGER"
        40          || "WARM"
        20          || "NORMAL"
    }

    def "powinien zablokować skrypt zawierający niedozwoloną pętlę while"() {
        given: "złośliwy skrypt z nieskończoną pętlą"
        def evilScript = """
            def x = 0
            while(true) { 
                x++ 
            }
            return "DONE"
        """

        when: "próbujemy uruchomić skrypt"
        AlertEngine.evaluateAlert(evilScript, [temp: 10])

        then: "kompilator rzuca błąd zanim skrypt w ogóle wystartuje"
        // W Groovy 4 błąd składni/bezpieczeństwa to MultipleCompilationErrorsException
        thrown(MultipleCompilationErrorsException)
    }

    def "powinien zablokować próbę użycia System.exit()"() {
        given: "skrypt próbujący wyłączyć serwer"
        def hack = "System.exit(0)"

        when:
        AlertEngine.evaluateAlert(hack, [temp: 10])

        then:
        thrown(Exception)
    }
}