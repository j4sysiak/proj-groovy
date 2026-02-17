import groovy.cli.picocli.CliBuilder

class LogMasker {
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
        String content = input.text

        if (opts.m) {
            // Regex dla emaila: /[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}/
            content = content.replaceAll(/[\w.-]+@[\w.-]+\.[a-zA-Z]{2,}/, "****@****")
        }

        if (opts.c) {
            // Regex dla karty (uproszczony): /\d{4}-\d{4}-\d{4}-(\d{4})/
            content = content.replaceAll(/\d{4}-\d{4}-\d{4}-(\d{4})/, "XXXX-XXXX-XXXX-\$1")
        }

        if (opts.o) {
            new File(opts.o).text = content
            println "Zapisano zamaskowany plik w: ${opts.o}"
        } else {
            println content
        }
    }
}