import pl.edu.praktyki.LegacyService

class GroovyBusinessLogic {
    def service = new LegacyService()

    def smartProcess(String text) {
        // Groovy używa metody z Javy, ale dodaje swój styl (GString)
        def result = service.processData(text)
        return "Groovy mówi: [${result.toUpperCase()}]"
    }
}