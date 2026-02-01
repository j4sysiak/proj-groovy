class TraitsDemo {

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
}
