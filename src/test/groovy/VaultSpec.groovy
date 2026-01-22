import spock.lang.Specification

class VaultSpec extends Specification {
    def service = new VaultService()

    def "powinien rzucić wyjątek przy błędnym kodzie"() {
        when:
        service.open("9999")

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "BŁĘDNY KOD!"
    }
}