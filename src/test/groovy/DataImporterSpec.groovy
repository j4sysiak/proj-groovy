import spock.lang.Specification

class DataImporterSpec extends Specification {

    def "powinien zalogować błąd gdy cena w CSV jest ujemna"() {
        given: "plik CSV z prawidłowymi i błędnymi danymi"
        def tempDir = File.createTempDir()
        def csvFile = new File(tempDir, "data.csv")

        csvFile.text = """Product,Price
                          Mleko,3.50
                          Chleb,-5.00
                          Masło,8.00"""

        and: "SZPIEG (Spy) na klasie DataImporter"
        // Tworzymy prawdziwy obiekt, ale owinięty w Spy
        def importer = Spy(new DataImporter())

        when: "uruchamiamy import"
        importer.importCsv(csvFile)

        then: "metoda log() z Traita została wywołana dla błędnej linii"
        // Sprawdzamy interakcję: 1 raz, z argumentem zawierającym słowo 'Chleb'
        1 * importer.log({ msg ->
            msg.contains("Chleb") && msg.contains("ujemną cenę")
        })

        and: "dla poprawnych linii log NIE został wywołany (opcjonalne sprawdzenie)"
        0 * importer.log({ msg -> msg.contains("Mleko") })

        cleanup: "sprzątamy pliki"
        tempDir.deleteDir()
    }
}