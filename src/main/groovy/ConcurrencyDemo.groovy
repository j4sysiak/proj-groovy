import groovyx.gpars.GParsPool

class ConcurrencyDemo {

    static void main(String[] args) {
        // Lista liczb do przetworzenia
        def numbers = (1..10).toList()

        // Definicja "ciężkiej" operacji (trwa 1 sekundę)
        def heavyCalculation = { num ->
            sleep(1000) // Symulacja opóźnienia
            return num * 10
        }

        println "--- START: Przetwarzanie sekwencyjne (zwykłe collect) ---"
        long start = System.currentTimeMillis()

        // Zwykłe Groovy: leci jeden po drugim (10 * 1s = ok. 10s)
        def resultSeq = numbers.collect(heavyCalculation)

        long end = System.currentTimeMillis()
        println "Wynik: $resultSeq"
        println "Czas sekwencyjny: ${(end - start)} ms"


        println "\n--- START: Przetwarzanie równoległe (GPars) ---"
        start = System.currentTimeMillis()

        // GPars Magic: Tworzymy pulę wątków i używamy metody collectParallel
        GParsPool.withPool {
            // collectParallel automatycznie dzieli pracę na rdzenie procesora
            def resultPar = numbers.collectParallel(heavyCalculation)
            println "Wynik wewnątrz puli: $resultPar"
        }

        end = System.currentTimeMillis()
        println "Czas równoległy: ${(end - start)} ms"
    }
}