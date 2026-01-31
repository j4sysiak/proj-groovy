import java.util.regex.Matcher

class RegexDemo {

    static List extractOrders(String logText) {
    // Wzorzec: Słowo "Order", myślnik, cyfry.
    // W Groovy używamy /.../ zamiast "...", żeby nie musieć pisać podwójnych slashy (\\d)
        def pattern = /Order-(\d+)/

        // Operator =~ tworzy Matcher
        def matcher = (logText =~ pattern)

        // matcher[0] -> całe dopasowanie i grupy pierwszego znalezienia
        // matcher[0][1] -> zawartość pierwszej grupy nawiasowej (\d+)

        // Pobieramy wszystkie numery (grupa 1) z każdego dopasowania
        def orders = []
        matcher.each { match, number ->
            orders << number
        }

        return orders
    }

    static boolean isValidPostalCode(String code) {
        // Format XX-XXX
        return code ==~ /\d{2}-\d{3}/
    }

    static boolean isSecurePassword(String pass) {
        // Min 8 znaków, duża litera, cyfra (uproszczone)
        return pass ==~ /(?=.*\d)(?=.*[A-Z]).{8,}/
    }
}