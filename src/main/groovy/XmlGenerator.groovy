import groovy.xml.MarkupBuilder

def writer = new StringWriter()
def xml = new MarkupBuilder(writer)

xml.records {
    user(id: 1) {
        username "jkowalski"
        email "j@k.pl"
    }
    user(id: 2) {
        username "anowak"
        email "a@n.pl"
    }
}

println "Wygenerowany XML:"
println writer.toString()