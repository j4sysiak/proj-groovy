import groovy.sql.Sql

// 1. Połączenie (H2 w pamięci)
def db = Sql.newInstance("jdbc:h2:mem:testdb", "sa", "", "org.h2.Driver")

// 2. Tworzenie tabeli
db.execute "CREATE TABLE Employee (id INT PRIMARY KEY, name VARCHAR(100), salary INT)"

// 3. Wstawianie danych (Groovy automatycznie zamienia $zmienna na PreparedStatement!)
def workers = [
        [id: 1, name: 'Jan', salary: 5000],
        [id: 2, name: 'Anna', salary: 7000],
        [id: 3, name: 'Piotr', salary: 4500]
]

workers.each { w ->
    db.execute "INSERT INTO Employee (id, name, salary) VALUES (${w.id}, ${w.name}, ${w.salary})"
}

// 4. Pobieranie danych (każdy wiersz to mapa)
println "Pracownicy z pensją > 4800:"
db.eachRow("SELECT * FROM Employee WHERE salary > 4800") { row ->
    println "- ${row.name} (ID: ${row.id}) zarabia ${row.salary}"
}

// 5. Pobranie pojedynczej wartości (Suma pensji)
def total = db.firstRow("SELECT SUM(salary) as total_sum FROM Employee").total_sum
println "Suma pensji: $total"