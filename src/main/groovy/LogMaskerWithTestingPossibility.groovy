import groovy.cli.picocli.CliBuilder

class LogMaskerWithTestingPossibility {

    // Wydzielona logika - to będziemy testować!
    static String maskContent(String content, boolean maskEmail, boolean maskCard) {
        if (maskEmail) {
            // Regex dla emaila
            content = content.replaceAll(/[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}/, "****@****")
        }

        if (maskCard) {
            // Regex dla karty - $1 odnosi się do grupy (\d{4})
            content = content.replaceAll(/\d{4}-\d{4}-\d{4}-(\d{4})/, "XXXX-XXXX-XXXX-\$1")
        }
        return content
    }

    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'LogMasker -f <file> [-m] [-c] [-o <output>]')
        cli.with {
            f(longOpt: 'file', args: 1, required: true, 'Plik wejściowy')
            m(longOpt: 'mask-email', 'Maskowanie adresów e-mail')
            c(longOpt: 'mask-card', 'Maskowanie kart kredytowych')
            o(longOpt: 'output', args: 1, 'Plik wyjściowy')
        }

        def opts = cli.parse(args)
        if (!opts) return

        File input = new File(opts.f)
        if (!input.exists()) {
            println "BŁĄD: Plik nie istnieje"
            return
        }

        // Wywołujemy naszą testowalną metodę
        String result = maskContent(input.text, (boolean)opts.m, (boolean)opts.c)

        if (opts.o) {
            new File(opts.o).text = result
            println "Zapisano w: ${opts.o}"
        } else {
            println result
        }
    }
}
