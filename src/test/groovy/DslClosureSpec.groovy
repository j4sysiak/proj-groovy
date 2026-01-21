import spock.lang.Specification

/*
 To jest technika używana w Jenkinsfile lub build.gradle.
 Chcemy stworzyć strukturę, która pozwoli nam skonfigurować np. wysyłkę raportu w taki sposób:
 Tak ma docelowo wyglądać nasz kod:

    raport {
            tytul "Miesięczny"
            odbiorca "szef@firma.pl"
            priority 1
    }
 */

class EmailConfig {
    String title
    String to
    int prio

    void tytul(String text) { this.title = text }
    void odbiorca(String text) { this.to = text }
    void priority(int p) { this.prio = p }
}

class DslClosureSpec extends Specification {

        // Metoda pomocnicza udająca silnik DSL
        def emailBuilder(Closure block) {
            def config = new EmailConfig()
            block.delegate = config         // Wszystko co w bloku, "leci" do obiektu config
            block.resolveStrategy = Closure.DELEGATE_FIRST
            block()                         // Uruchomienie bloku
            return config
        }

        def "powinien zbudować konfigurację przez DSL"() {
            when:
            def result = emailBuilder {
                tytul "Alert"
                odbiorca "admin@dev.pl"
                priority 10
            }

            then:
            result.title == "Alert"
            result.to == "admin@dev.pl"
            result.prio == 10
        }
}