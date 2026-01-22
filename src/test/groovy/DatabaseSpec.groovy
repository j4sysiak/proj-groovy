import spock.lang.Shared
import spock.lang.Specification

class DatabaseSpec extends Specification {
    @Shared String databaseUrl = "jdbc:mysql://localhost:3306/mydb"

    def "test 1"() {
        expect: databaseUrl.startsWith("jdbc")
    }
}