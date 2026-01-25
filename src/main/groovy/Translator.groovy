class Translator {

    // Ta metoda uruchomi się, gdy ktoś wywoła metodę, której nie ma w klasie
    def methodMissing(String name, args) {
        if (name.startsWith("sayIn")) {
            def language = name.substring(5) // np. "Spanish"
            println "Tłumaczę '${args[0]}' na język $language..."
            return "ZAKODOWANE_W_$language"
        }
        throw new MissingMethodException(name, this.class, args)
    }
}