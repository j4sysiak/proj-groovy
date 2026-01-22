import groovy.sql.Sql

// Tworzymy połączenie z bazą H2 w pamięci
def sql = Sql.newInstance("jdbc:h2:mem:testdb", "sa", "", "org.h2.Driver")

// 1. Tworzenie tabeli
sql.execute "CREATE TABLE Users (id INT PRIMARY KEY, name VARCHAR(100))"

// 2. Wstawianie danych (z użyciem list)
def users = ["Adam", "Ewa", "Marek"]
users.eachWithIndex { name, i ->
    sql.execute "INSERT INTO Users (id, name) VALUES ($i, $name)"
}

// 3. Pobieranie danych (Magia Groovy: każda linia to Mapa)
println "Użytkownicy w bazie:"
sql.eachRow("SELECT * FROM Users") { row ->
    println "ID: ${row.id}, IMIĘ: ${row.name}"
}

// 4. Pobieranie pojedynczej wartości
def count = sql.firstRow("SELECT COUNT(*) as total FROM Users").total
println "Suma użytkowników: $count"