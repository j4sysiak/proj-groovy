import spock.lang.Specification
import pl.devops.VersionTool

class JenkinsLibrarySpec extends Specification {

    def "VersionTool powinien generować poprawny format snapshotu"() {
        expect:
        VersionTool.getSnapshotVersion("1.0") ==~ /1.0-SNAPSHOT-\d{8}/
    }

    def "buildApp powinien poprawnie przetworzyć konfigurację"() {
        given: "nasz skrypt z vars/"
        def buildStep = new buildApp()

        when: "wywołujemy go z parametrami"
        def result = buildStep.call(name: "PaymentApi", env: "staging")

        then: "zwraca oczekiwany status"
        result == "BUILD_SUCCESS_PaymentApi_staging"
    }

    def "buildApp powinien użyć domyślnych wartości gdy brakuje mapy"() {
        given:
        def buildStep = new buildApp()

        when:
        def result = buildStep.call()

        then:
        result.contains("Unnamed-App")
        result.contains("dev")
    }

    def "notify powinien poprawnie generować komunikaty dla Slacka"() {
        given: "nasz globalny krok notify"
        def notify = new notify()

        expect: "komunikat błędu zawiera kanał i status"
        notify.call(status: 'FAILURE', channel: '#dev-alerts') == "[SLACK] Alert w kanale #dev-alerts: Build FAILED!"

        and: "komunikat sukcesu jest prosty"
        notify.call(status: 'SUCCESS') == "[SLACK] Build OK"

        and: "domyślny kanał jest używany, gdy go nie podamy"
        notify.call(status: 'FAILURE') == "[SLACK] Alert w kanale #general: Build FAILED!"
    }
}