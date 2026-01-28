import spock.lang.Specification

class RestApiSpec extends Specification {

    def "powinien pobrać konkretny post i sprawdzić dane"() {
        when:
        def post = RestClientService.getPost(1)

        then:
        post.id == 1
        post.title != null
    }

    def "powinien rzucić wyjątek dla nieistniejącego posta"() {
        when:
        RestClientService.getPost(9999)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "NOT_FOUND"
    }
}