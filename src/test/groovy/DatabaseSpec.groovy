import groovy.sql.Sql
import spock.lang.Specification
import spock.lang.Shared

class DatabaseSpec extends Specification {

    //@Shared String databaseUrl = "jdbc:mysql://localhost:3306/mydb"
    @Shared // Współdzielimy bazę między testami w tej klasie
    Sql sql = Sql.newInstance("jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1", "sa", "", "org.h2.Driver")

    // Wykonuje się przed wszystkimi testami
    def setupSpec() {
        sql.execute "CREATE TABLE Tasks (id IDENTITY PRIMARY KEY, title VARCHAR(100), done BOOLEAN)"
    }

    // Wykonuje się po każdym teście (czyścimy dane)
    def cleanup() {
        sql.execute "DELETE FROM Tasks"
    }

    def "powinien zapisać zadanie w bazie"() {
        when: "dodajemy nowe zadanie"
        sql.execute "INSERT INTO Tasks (title, done) VALUES ('Nauczyć się SQL', false)"

        then: "w bazie powinien znajdować się 1 wpis"
        def row = sql.firstRow("SELECT * FROM Tasks")
        row.title == 'Nauczyć się SQL'
        row.done == false
    }

    def "powinien zliczyć zadania używając tabeli danych"() {
        when:
        sql.execute "INSERT INTO Tasks (title, done) VALUES ($taskTitle, $isDone)"

        then:
        sql.firstRow("SELECT COUNT(*) as cnt FROM Tasks").cnt == 1

        where:
        taskTitle   | isDone
        "Zakupy"    | true
        "Sprzątanie"| false
    }

    //def "test 1"() {
    //    expect: databaseUrl.startsWith("jdbc")
    //}
}