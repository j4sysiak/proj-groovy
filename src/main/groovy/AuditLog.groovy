trait AuditLog {
    // Wewnętrzna lista logów (w prawdziwej aplikacji to byłaby baza danych)
    List<String> history = []

    void audit(String message) {
        String entry = "[AUDIT] ${java.time.LocalTime.now()} - $message"
        println entry // Wypisz na konsolę
        history << entry // Dodaj do listy (żebyśmy mogli to przetestować)
    }

    // Metoda pomocnicza dla testów - sprawdza czy dany wpis istnieje
    boolean hasAuditEntry(String fragment) {
        return history.any { it.contains(fragment) }
    }
}