import spock.lang.Specification
import spock.lang.Unroll

class LogMaskerSpec extends Specification {

    @Unroll
    def "powinien poprawnie maskować dane: email=#maskEmail, card=#maskCard"() {
        given: "tekst wejściowy z wrażliwymi danymi"
        def input = "User jan@wp.pl kupił bilet kartą 1111-2222-3333-4444."

        when: "uruchamiamy logikę maskowania"
        def result = LogMaskerWithTestingPossibility.maskContent(input, maskEmail, maskCard)

        then: "wynik zawiera oczekiwane maski"
        result == expectedOutput

        where: "różne kombinacje flag"
        maskEmail | maskCard || expectedOutput
        true      | false    || "User ****@**** kupił bilet kartą 1111-2222-3333-4444."
        false     | true     || "User jan@wp.pl kupił bilet kartą XXXX-XXXX-XXXX-4444."
        true      | true     || "User ****@**** kupił bilet kartą XXXX-XXXX-XXXX-4444."
        false     | false    || "User jan@wp.pl kupił bilet kartą 1111-2222-3333-4444."
    }

    def "nie powinien nic zmieniać jeśli tekst nie zawiera maili ani kart"() {
        given:
        def cleanText = "To jest bezpieczny log systemowy."

        expect:
        LogMaskerWithTestingPossibility.maskContent(cleanText, true, true) == cleanText
    }
}