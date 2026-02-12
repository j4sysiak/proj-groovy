import groovy.transform.ToString

@ToString
class OrderItem {
    String name
    double price
}

@ToString
class FinalOrder {
    String customerName
    List<OrderItem> items = []

    double getTotal() { items.price.sum() ?: 0 }
}