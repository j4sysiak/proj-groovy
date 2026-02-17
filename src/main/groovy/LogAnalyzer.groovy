import groovy.cli.picocli.CliBuilder

class LogAnalyzer {
    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'LogAnalyzer -f <file> -k <keyword> [-o <output>]')

        cli.with {
            f longOpt: 'file', args: 1, required: true, 'Ścieżka do pliku wejściowego'
            k longOpt: 'keyword', args: 1, required: true, 'Słowo kluczowe do wyszukania'
            o longOpt: 'output', args: 1, 'Plik wynikowy (opcjonalnie)'
        }

        def opts = cli.parse(args)
        if (!opts) return // Błędy parsowania (np. brak wymaganych pól) są wypisywane automatycznie

        File inputFile = new File(opts.f)
        if (!inputFile.exists()) {
            System.err.println "BŁĄD: Plik ${opts.f} nie istnieje!"
            return
        }

        def foundLines = inputFile.readLines().findAll { it.contains(opts.k) }

        if (opts.o) {
            new File(opts.o).text = foundLines.join('\n')
            println "Wyniki zapisano do ${opts.o}"
        } else {
            foundLines.each { println it }
        }
    }
}