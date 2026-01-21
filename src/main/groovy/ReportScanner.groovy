import groovy.json.JsonSlurper

class ReportScanner {
    static void main(String[] args) {
        // Symulacja wyniku z zewnętrznego systemu (np. SonarQube lub Testy)
        def jsonResponse = '''
        {
            "status": "SUCCESS",
            "metrics": {
                "coverage": 75.5,
                "bugs": 2,
                "vulnerabilities": 0
            },
            "environment": "production"
        }
        '''

        def slurper = new JsonSlurper()
        def result = slurper.parseText(jsonResponse)

        // Logika automatyzacji
        println "Analiza raportu dla środowiska: ${result.environment}"

        if (result.metrics.bugs > 0) {
            println "[WARNING] Znaleziono błędy: ${result.metrics.bugs}"
        }

        if (result.metrics.coverage < 80) {
            println "[FAILURE] Coverage ${result.metrics.coverage}% jest poniżej progu 80%!"
        } else {
            println "[SUCCESS] Jakość kodu zaakceptowana."
        }
    }
}