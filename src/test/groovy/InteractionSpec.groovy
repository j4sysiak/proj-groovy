// TEST
import spock.lang.Specification

class InteractionSpec extends Specification {

    def "powinien szpiegować prawdziwy LoggerService"() {
        given: "Prawdziwy serwis owinięty w Spy"
        def realLogger = new LoggerServiceImpl()
        def loggerSpy = Spy(realLogger)
        def processor = new BusinessProcessor(logger: loggerSpy)

        when:
        processor.process(-5)
        processor.process(10)

        then: "Sprawdzamy interakcje na żywym organizmie"
        1 * loggerSpy.log({ it.contains("Błąd") }) // Sprawdzamy czy raz wywołano z błędem
        1 * loggerSpy.log({ it.contains("Sukces") }) // Sprawdzamy czy raz wywołano z sukcesem

        // Zauważ: W konsoli i tak zobaczysz "REAL LOG", bo Spy wykonuje prawdziwy kod!
    }
}