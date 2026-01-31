import spock.lang.Specification

class ConfigParserSpec extends Specification {

    def "powinien sparsować plik konfiguracyjny ignorując komentarze"() {
        given: "tekst konfiguracyjny (np. wczytany z pliku)"
        def rawConfig = """
            db.host=localhost
            db.port=5432
            
            # To jest komentarz - to powinno zostać pominięte
            # Kolejny komentarz
            
            app.name=GroovyApp
            timeout = 3000 
        """

        when: "parsujemy konfigurację"
        def result = ConfigParser.parseConfig(rawConfig)

        then: "mapa zawiera poprawne pary klucz-wartość"
        result['db.host'] == 'localhost'
        result['db.port'] == '5432'
        result['app.name'] == 'GroovyApp'

        and: "parser poradził sobie ze spacjami (dzięki .trim())"
        result['timeout'] == '3000'

        and: "mapa ma rozmiar 4 (host, port, name, timeout)"
        result.size() == 4

        and: "mapa NIE zawiera treści komentarzy"
        !result.keySet().any { it.contains("komentarz") }
    }

}
