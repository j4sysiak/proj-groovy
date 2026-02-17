import groovy.cli.picocli.CliBuilder

def cli = new CliBuilder(usage: 'GreeterTool [options] name')

// Definiujemy dostępne opcje
cli.with {
    h longOpt: 'help', 'Wyświetla tę pomoc'
    v longOpt: 'verbose', 'Włącza tryb szczegółowy'
    l longOpt: 'lang', args: 1, argName: 'kod', 'Język (pl lub en), domyślnie en'
}

def options = cli.parse(args)

// 1. Obsługa pomocy
if (!options || options.h) {
    cli.usage()
    return
}

// 2. Pobieranie argumentów pozycyjnych (to co zostało po flagach)
def name = options.arguments()[0] ?: 'Nieznajomy'
def lang = options.l ?: 'en'

if (options.v) println "[DEBUG] Uruchamiam skrypt dla: $name (język: $lang)"

if (lang == 'pl') {
    println "Cześć $name!"
} else {
    println "Hello $name!"
}