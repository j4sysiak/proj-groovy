import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical // Automatycznie generuje: toString, equals, hashCode i konstruktor argumentowy
@CompileStatic // Włącza statyczne sprawdzanie typów (jak w Javie - wydajność!)
class User {
    String firstName
    String lastName
    int age

    // Zauważ: brak public, brak średników, brak getterów/setterów
}

