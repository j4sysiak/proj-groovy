class OrderBuilder {
    private FinalOrder order = new FinalOrder()

    // Metoda ustawiająca nazwę klienta
    void client(String name) {
        order.customerName = name
    }

    // Metoda obsługująca zagnieżdżony blok 'item'
    void item(String name, double price) {
        order.items << new OrderItem(name: name, price: price)
    }

    // Statyczna metoda wejściowa do DSL
    // Zwróć uwagę na adnotację @DelegatesTo – to ona podpowiada IDE (IntelliJ),
    // jakie metody są dostępne wewnątrz klamerek (..o tuu ..)
    static FinalOrder make(@DelegatesTo(value = OrderBuilder, strategy = Closure.DELEGATE_FIRST) Closure code) {
        def builder = new OrderBuilder()

        // Magia: Przypisujemy builder jako delegata dla bloku kodu
        code.delegate = builder
        code.resolveStrategy = Closure.DELEGATE_FIRST

        code() // Wykonujemy blok kodu użytkownika

        return builder.order
    }
}