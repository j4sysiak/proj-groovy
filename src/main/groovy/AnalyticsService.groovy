import groovy.transform.Memoized

class AnalyticsService {

    @Memoized
    String getComplexReport(int id) {
        println "Rozpoczynam ciężkie generowanie raportu dla ID: $id..."
        sleep(3000) // Symulacja ciężkiej pracy (3 sekundy)
        return "Raport #$id - Dane: ${Math.random()}"
    }
}