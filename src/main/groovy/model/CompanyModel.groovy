package model

import groovy.transform.ToString

@ToString
class Company {
    String name
    List<Department> departments = []
}

@ToString(includeNames = true, excludes = 'company')
class Department {
    String title
    Company company // Relacja zwrotna
    List<Employee> employees = []
}

@ToString(includeNames = true, excludes = 'department')
class Employee {
    String name
    double salary
    Department department // Relacja zwrotna
}