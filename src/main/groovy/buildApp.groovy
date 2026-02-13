// Symulujemy krok rurociągu
def call(Map config = [:]) {
    def appName = config.name ?: "Unnamed-App"
    def env = config.env ?: "dev"

    println "[JENKINS] Rozpoczynam budowanie aplikacji: ${appName.toUpperCase()}"
    println "[JENKINS] Środowisko docelowe: ${env}"

    if (env == "prod") {
        println "[WARNING] Budowanie na PRODUKCJĘ wymaga dodatkowych uprawnień!"
    }

    // Wykorzystujemy klasę z src/
    // W Jenkinsie trzeba by zaimportować: import pl.devops.VersionTool
    return "BUILD_SUCCESS_${appName}_${env}"
}