class ConfigParser {

    static Map<String, String> parseConfig(String configText) {
        def config = [:]

        // Przetwarzamy tekst linia po linii
        configText.eachLine { line ->
            line = line.trim() // Usuwamy białe znaki z początku/końca

            // 1. Ignorujemy puste linie i komentarze (zaczynające się od #)
            if (line.isEmpty() || line.startsWith("#")) {
                return // idź do następnej linii
            }

            // 2. Definiujemy Regex z dwiema grupami: (klucz)=(wartość)
            def pattern = /(.+)=(.+)/
            def matcher = (line =~ pattern)

            // 3. Jeśli linia pasuje do wzorca, wyciągamy dane
            if (matcher) {
                // matcher[0] to całe dopasowanie
                // matcher[0][1] to pierwsza grupa (klucz)
                // matcher[0][2] to druga grupa (wartość)
                def key = matcher[0][1].trim()
                def value = matcher[0][2].trim()

                config[key] = value
            }
        }

        return config
    }
}