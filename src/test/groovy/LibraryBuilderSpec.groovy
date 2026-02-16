import model.Library
import spock.lang.Specification

class LibraryBuilderSpec extends Specification {

    def "powinien stworzyć bibliotekę z wieloma książkami tego samego autora używając refId"() {
        given: "inicjalizacja buildera"
        def builder = new ObjectGraphBuilder()
        builder.classNameResolver = "model"

        when: "budujemy graf obiektów z użyciem identyfikatorów"
        Library mojaBiblioteka = builder.library(name: 'Książnica') {
            // Pierwsza książka tworzy autora i nadaje mu ID 'lem'
            book(title: 'Solaris') {
                author(id: 'lem', lastName: 'Lem')
            }
            // Druga książka tylko referuje do istniejącego autora 'lem'
            book(title: 'Cyberiada') {
                author(refId: 'lem')
            }
        }

        then: "biblioteka ma dwa elementy"
        mojaBiblioteka.books.size() == 2

        and: "obie książki mają autora o nazwisku Lem"
        mojaBiblioteka.books[0].author.lastName == 'Lem'
        mojaBiblioteka.books[1].author.lastName == 'Lem'

        and: "KLUCZOWY TEST: To jest dokładnie ten sam obiekt w pamięci!"
        // W Groovy 'is()' sprawdza referencję (czy to ten sam adres w RAM)
        mojaBiblioteka.books[0].author.is(mojaBiblioteka.books[1].author)

        // Alternatywnie w Spocku:
        System.identityHashCode(mojaBiblioteka.books[0].author) == System.identityHashCode(mojaBiblioteka.books[1].author)
    }
}