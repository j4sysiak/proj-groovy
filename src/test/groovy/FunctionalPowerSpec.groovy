import spock.lang.Specification

class FunctionalPowerSpec extends Specification {

    /*
    Wyjaśnienie mechaniki:
    curry(X): Bierze parametry od lewej strony.
    Oryginał: (rate, amount)
    curry(0.23) -> rate staje się 0.23. Zostaje (amount).
    rcurry(Y): Bierze parametry od prawej strony.
    Oryginał: (rate, amount)
    rcurry(1000) -> amount staje się 1000. Zostaje (rate).
    */

    def "powinien tworzyć dedykowane funkcje podatkowe używając curry (lewy parametr)"() {
        given: "ogólna funkcja obliczająca podatek"
        // Schemat: { stawka, kwota -> wynik }
        def calculateTax = { BigDecimal rate, BigDecimal amount -> amount * rate }

        when: "zamrażamy pierwszy parametr (stawkę - rate) używając curry"
        def vatPL = calculateTax.curry(0.23) // Stawka 23% na stałe
        def vatDE = calculateTax.curry(0.19) // Stawka 19% na stałe

        then: "nowe funkcje przyjmują już tylko jeden parametr (kwotę)"
        vatPL(100) == 23.00
        vatDE(100) == 19.00

        and: "działają dla innych kwot"
        vatPL(200) == 46.00
    }

    def "powinien użyć rcurry do zamrożenia kwoty (prawy parametr)"() {
        given: "ta sama funkcja podatkowa"
        def calculateTax = { BigDecimal rate, BigDecimal amount -> amount * rate }

        when: "zamrażamy drugi parametr (kwotę - amount) używając rcurry (right curry)"
        // Chcemy funkcję, która liczy różne stawki zawsze dla kwoty 1000 zł
        def taxFor1000 = calculateTax.rcurry(1000)

        then: "nowa funkcja przyjmuje tylko stawkę"
        taxFor1000(0.10) == 100.00 // 10% z 1000
        taxFor1000(0.50) == 500.00 // 50% z 1000
    }
}