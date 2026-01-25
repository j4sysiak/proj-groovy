import spock.lang.Specification

class AstSpec extends Specification {

    def "powinien używać metod delegowanych (@Delegate)"() {
        given: "mamy drukarkę SmartPrinter"
        def smartPrinter = new SmartPrinter()

        expect: "możemy na niej wywołać metodę, której NIE MA w jej kodzie, bo została delegowana"
        // Metoda connect() pochodzi z klasy SimplePrinter!
        smartPrinter.connect()
        smartPrinter.printLowRes("test")
        smartPrinter.respondsTo("connect")
        smartPrinter.printHighRes("wysoka jakość")
    }

    def "powinien wygenerować toString zawierający tylko model drukarki"() {
        given: "nowa inteligentna drukarka"
        def smartPrinter = new SmartPrinter()

        when: "wywołujemy metodę toString"
        def result = smartPrinter.toString()

        then: "wynik zawiera nazwę modelu"
        result.contains("HP Laser 5000")

        and: "wynik NIE zawiera informacji o delegacie basicPrinter"
        // Domyślnie toString bez parametrów wypisałby wszystkie pola
        !result.contains("basicPrinter")
        !result.contains("SimplePrinter")

        and: "wynik ma format oczekiwany przez Groovy"
        // Groovy generuje format: NazwaKlasy(pole1, pole2)
        result == "SmartPrinter(HP Laser 5000)"
    }

    def "powinien błyskawicznie zwracać zapamiętany wynik (@Memoized)"() {
        given: "serwis z pamięcią podręczną"
        def service = new AnalyticsService()

        when: "pierwsze wywołanie trwające 3 sekundy"
        long start = System.currentTimeMillis()
        service.getComplexReport(101)
        long duration1 = System.currentTimeMillis() - start

        and: "drugie wywołanie z tym samym ID"
        start = System.currentTimeMillis()
        service.getComplexReport(101)
        long duration2 = System.currentTimeMillis() - start

        then: "drugie wywołanie było natychmiastowe (< 100ms)"
        duration2 < 100
        duration1 > 2900
    }

    def "powinien policzyć sumę bez błędu StackOverflow (@TailRecursive)"() {
        expect: "rekurencja dla ogromnej liczby nie wywala programu"
        MathWizard.sumLongRange(100_000) == 5000050000L
    }
}