import groovy.json.JsonSlurper

def jsonText = '''
{
    "company": "DevStudio",
    "employees": [
        {"name": "Adam", "role": "Dev", "skills": ["Java", "Groovy"]},
        {"name": "Ewa", "role": "Tester", "skills": ["Selenium"]}
    ],
    "active": true
}
'''

def slurper = new JsonSlurper()
def data = slurper.parseText(jsonText)

// Dostęp do danych przez kropkę!
println "Firma: ${data.company}"
println "Pierwszy pracownik: ${data.employees[0].name}"

// Magia Groovy: znajdź skille wszystkich programistów
def allSkills = data.employees.findAll { it.role == "Dev" }.skills.flatten()
println "Umiejętności devów: $allSkills"