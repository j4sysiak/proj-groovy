// 1. Definiujemy ogólną funkcję obliczającą podatek
def calculateTax = { rate, amount -> amount * rate }

// 2. Tworzymy "specjalistyczne" funkcje przy użyciu curry()
def plTax = calculateTax.curry(0.23) // rate jest zamrożone na 0.23
def deTax = calculateTax.curry(0.19) // rate jest zamrożone na 0.19

println "Podatek w PL od 100: " + plTax(100)
println "Podatek w DE od 100: " + deTax(100)

// 3. rcurry (right curry) - mrozi parametry od prawej strony
def taxOf100 = calculateTax.rcurry(100) // amount jest zamrożone na 100
println "Podatek 8% od 100: " + taxOf100(0.08)