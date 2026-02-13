// 1. Definiujemy "Pipeline" (DSL style)
def runPipeline() {
    println "--- START CI/CD PIPELINE ---"

    // Wywołujemy nasz "globalny krok" (tak jakbyśmy byli w Jenkinsie)
    def status = new buildApp().call(name: "OrderService", env: "prod")

    println "Status końcowy: $status"
    println "--- KONIEC CI/CD PIPELINE ---"
}

runPipeline()