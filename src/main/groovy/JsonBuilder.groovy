import groovy.json.JsonBuilder

def builder = new JsonBuilder()

builder.response {
    status "SUCCESS"
    timestamp new Date().format("yyyy-MM-dd")
    results (1..3).collect { "Item-$it" }
}

println "Wygenerowany JSON:"
println builder.toPrettyString()