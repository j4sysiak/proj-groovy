static void main(String[] args) {

    // --- Testowanie klasy (możesz dopisać pod klasą w tym samym pliku) ---

// 1. Nazwany konstruktor (Named Arguments)
    def user = new User(firstName: "Jan", lastName: "Kowalski", age: 30)

// 2. Dostęp do pól jak do właściwości (Property access)
    println "Użytkownik: ${user.firstName} ${user.lastName}"

// 3. Sprawdzenie @Canonical
    println user.toString()

}