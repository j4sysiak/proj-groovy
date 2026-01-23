// TEST
import spock.lang.Specification

class InteractionSpec extends Specification {

 //   Tworzysz Spy w tym teście, żeby jednocześnie:
//    1. obserwować interakcje między BusinessProcessor a rzeczywistym LoggerServiceImpl (liczba wywołań, argumenty),
//    2. pozwolić wykonywać część prawdziwego zachowania (np. logowanie, walidacje) zamiast całkowicie je podmieniać mockiem,
//    3. testować efekty uboczne i kolejność wywołań bez rezygnacji z realnej implementacji.
//    W praktyce w Twoim InteractionSpec sprawdzasz, że processor.process(...) wywołuje log(...) z oczekiwanymi komunikatami i odpowiednią liczbą razy, przy zachowaniu realnego działania loggera. Minusem jest większe sprzężenie testu z implementacją i możliwa niestabilność — jeśli chcesz tylko sprawdzić wywołania bez uruchamiania realnego kodu, użyj Mock zamiast Spy.

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