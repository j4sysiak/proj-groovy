// Tworzymy obiekt bez żadnej klasy!
def person = new Expando()

// Dodajemy pola "w locie"
person.firstName = "Jan"
person.lastName = "Kowalski"

// Dodajemy metodę jako domknięcie (Closure)
person.sayHello = {
    println "Cześć, jestem $firstName $lastName!"
}

// Dodajemy logikę obliczeniową
person.calculateBonus = { salary ->
    salary * 1.10
}

// Użycie
person.sayHello()
println "Bonus: " + person.calculateBonus(5000)