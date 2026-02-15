import groovy.transform.CompileStatic

class PerformanceService {

    // Metoda DYNAMICZNA (domyślna w Groovy)
    // Każde wywołanie "+" przechodzi przez MetaClass (wolniejsze)
    def calculateDynamic(int iterations) {
        def sum = 0
        for (int i = 0; i < iterations; i++) {
            sum += i
        }
        return sum
    }

    // Metoda STATYCZNA
    // Kompilator generuje bajtkod identyczny z Javą (szybkie)
    @CompileStatic
    long calculateStatic(int iterations) {
        long sum = 0
        for (int i = 0; i < iterations; i++) {
            sum += i
        }
        return sum
    }

    /**
     * Zadanie Finałowe: Wydajne łączenie stringów.
     * Dzięki @CompileStatic, Groovy wygeneruje bajtkod identyczny z Javą.
     */
    @CompileStatic
    String processStringsStatic(List<String> list) {
        if (list == null) return ""

        // Używamy standardowego javowego StringBuilder dla max wydajności
        StringBuilder sb = new StringBuilder()

        // Tradycyjna pętla for-in w trybie statycznym jest bardzo szybka
        for (String s : list) {
            sb.append(s)
        }

        return sb.toString()
    }

    String processStringsDynamic(List<String> list) {
        if (list == null) return ""

        // Używamy standardowego javowego StringBuilder dla max wydajności
        StringBuilder sb = new StringBuilder()

        // Tradycyjna pętla for-in w trybie statycznym jest bardzo szybka
        for (String s : list) {
            sb.append(s)
        }

        return sb.toString()
    }
}