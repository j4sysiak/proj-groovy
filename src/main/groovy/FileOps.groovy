import groovy.io.FileType

class FileOps {
    static List<File> findErrorLogs(String path) {
        def foundFiles = []
        def dir = new File(path)

        if (!dir.exists()) return []

        // Magia Groovy: traverse
        dir.traverse(type: FileType.FILES, nameFilter: ~/.*error.*\.txt/) { file ->
            println "Znalazłem plik: ${file.name}"
            foundFiles << file
        }
        return foundFiles
    }

    static void generateReportFromCsv(File csvFile, File reportFile) {
        // Czyścimy plik wynikowy
        reportFile.write("RAPORT CENOWY\n================\n")

        // Czytamy CSV linia po linii (pomijając nagłówek)
        def lines = csvFile.readLines()
        if (lines.size() < 2) return

        def header = lines[0] // np. Product,Price,Qty

        lines.drop(1).each { line ->
            def parts = line.split(',')
            def product = parts[0]
            def price = parts[1].toBigDecimal()
            def qty = parts[2].toInteger()

            def total = price * qty

            // Dopisujemy do pliku (append)
            reportFile.append("Produkt: $product | Wartość magazynowa: $total PLN\n")
        }
    }
}