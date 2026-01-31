import spock.lang.Specification
import spock.lang.Unroll

class RegexDemoSpec extends Specification {

    @Unroll // Rozwija każdy wiersz tabeli jako osobny test w raporcie
    def "Zadanie 1: powinien wyciągnąć numery zamówień z tekstu '#logInput'"() {
        when: "uruchamiamy ekstraktor na podanym tekście"
        def ids = RegexDemo.extractOrders(logInput)

        then: "wynikowa lista zawiera tylko oczekiwane numery ID"
        ids == expectedIds

        where: "zdefiniowane scenariusze testowe"
        logInput                                            || expectedIds
        "System created Order-1234 successfully."           || ["1234"]
        "Error in Order-55. Retrying Order-99... Success."  || ["55", "99"]
        "Order-1, Order-2, Order-3"                         || ["1", "2", "3"]
        "Brak zamówień w tym logu."                         || []
        "Order-ABC (błędny format), Order- (pusty)"         || []
        "Order-100 mixed with Order-200"                    || ["100", "200"]
    }
}