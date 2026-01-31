import groovy.text.SimpleTemplateEngine

class TemplateGenerator {
    static String generateWelcomeEmail(String userName, List products) {
        // Szablon wygląda jak zwykły tekst z wstawkami ${...} lub kodem <% ... %>
        def templateText = '''
            Witaj ${user}!
            
            Dziękujemy za zakupy w naszym sklepie.
            Twoje produkty:
            <% products.each { prod -> %>
              - ${prod}
            <% } %>
            
            Suma pozycji: ${products.size()}
            Pozdrawiamy, Zespół.
        '''

        // Dane do wstrzyknięcia (Binding)
        def binding = [
                user: userName,
                products: products
        ]

        def engine = new SimpleTemplateEngine()
        return engine.createTemplate(templateText).make(binding).toString()
    }
}