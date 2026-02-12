import groovy.transform.ToString

@ToString
class OrderItem {
    String name
    double price
    String category // Dodane pole
}

@ToString
class FinalOrder {
    String customerName
    List<OrderItem> items = []

    double getTotal() { items.price.sum() ?: 0 }
}