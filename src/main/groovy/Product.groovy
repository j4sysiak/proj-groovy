import groovy.transform.Canonical

@Canonical
class Product {
    String name
    double price
    String category
}

def products = [
        new Product("Chleb", 5.50, "Food"),
        new Product("Mleko", 4.00, "Food"),
        new Product("Monitor", 1200.00, "Tech"),
        new Product("Myszka", 150.00, "Tech")
]

// 1. findAll (odpowiednik filter)
def cheapFood = products.findAll { it.price < 10 && it.category == "Food" }

// 2. collect (odpowiednik map)
def names = products.collect { it.name.toUpperCase() }

// 3. groupBy (grupuje w Mapę!)
def byCategory = products.groupBy { it.category }

// 4. Any / Every
boolean hasExpensiveTech = products.any { it.price > 1000 }