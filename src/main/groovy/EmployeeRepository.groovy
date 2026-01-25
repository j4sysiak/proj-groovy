import groovy.sql.Sql

class EmployeeRepository {
    private Sql sql

    // Konstruktor przyjmuje obiekt Sql - to pozwala nam podpiąć inną bazę w teście!
    EmployeeRepository(Sql sql) {
        this.sql = sql
    }

    void initDatabase() {
        sql.execute "CREATE TABLE Employee (id INT PRIMARY KEY, name VARCHAR(100), salary INT)"
    }

    void addEmployee(int id, String name, int salary) {
        sql.execute "INSERT INTO Employee (id, name, salary) VALUES ($id, $name, $salary)"
    }

    List<String> getRichEmployeesNames(int minSalary) {
        return sql.rows("SELECT name FROM Employee WHERE salary > $minSalary").collect { it.name }
    }

    int getTotalSalary() {
        return sql.firstRow("SELECT SUM(salary) as total FROM Employee").total ?: 0
    }
}