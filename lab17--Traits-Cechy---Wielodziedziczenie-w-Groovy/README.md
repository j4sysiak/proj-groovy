Lab 17: Traits (Cechy) – Wielodziedziczenie w Groovy
----------------------------------------------------

Cel: 
Java nie pozwala na dziedziczenie po wielu klasach. 
Groovy wprowadza Traits (podobne do interfejsów z default methods w Javie, ale mające stan/pola!). 
To potężne narzędzie architektoniczne.

Zadanie-1: System Logowania i Walidacji
---------------------------------------

Chcemy, aby nasze klasy mogły "nabyć" umiejętność logowania i walidacji, po prostu dodając słowo kluczowe.

Co masz zrobić:
Stwórz `src/main/groovy/TraitsDemo.groovy`

```groovy
// Trait - to coś więcej niż interfejs
trait Loggable {
  // Może mieć metodę z implementacją
  void log(String msg) {
    println "[LOG]: $msg"
  }
}

trait Validatable {
    // Może mieć stan! (pole)
    boolean isValid = true
    List<String> errors = []

    void addError(String err) {
        isValid = false
        errors << err
    }

    void validate() {
        if (!isValid) throw new RuntimeException("Validation failed: $errors")
    }
}

// Klasa użytkownika nabywa obie cechy naraz!
class UserHandler implements Loggable, Validatable {
void register(String username) {
log("Próba rejestracji: $username") // metoda z Loggable

        if (username.length() < 3) {
            addError("Nazwa za krótka") // metoda z Validatable
        }
        
        validate() // metoda z Validatable
        log("Sukces!")
    }
}
```

Test:  `TraitsSpec.groovy`

```groovy

import spock.lang.Specification

class TraitsSpec extends Specification {
def "UserHandler powinien mieć metody z obu Traitów"() {
given:
def handler = new UserHandler()

        when: "próbujemy zarejestrować błędnego użytkownika"
        handler.register("Jo")

        then: "rzuca wyjątek walidacji (z Validatable)"
        def ex = thrown(RuntimeException)
        ex.message.contains("Nazwa za krótka")

        and: "pole stanu z Traitu zostało zmienione"
        handler.isValid == false
    }
}
```