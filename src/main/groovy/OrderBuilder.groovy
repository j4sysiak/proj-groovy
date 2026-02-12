class OrderBuilder {
    private FinalOrder order = new FinalOrder()
    private String activeCategory = "Ogólna" // Domyślna kategoria

    // Metoda ustawiająca nazwę klienta
    void client(String name) {
        order.customerName = name
    }

    // Metoda obsługująca zagnieżdżony blok 'item'
    //void item(String name, double price) {
    //    order.items << new OrderItem(name: name, price: price)
    //}
    // Zmieniona metoda item - teraz korzysta z activeCategory
    void item(String name, double price) {
        order.items << new OrderItem(name: name, price: price, category: activeCategory)
    }

    // NOWA METODA: Obsługa bloku category
    void category(String name, @DelegatesTo(value = OrderBuilder, strategy = Closure.DELEGATE_FIRST) Closure code) {
        // 1. Zapamiętujemy poprzednią kategorię (ważne przy głębokim zagnieżdżaniu!)
        def previousCategory = activeCategory

        // 2. Ustawiamy nową kategorię dla elementów wewnątrz tego bloku
        activeCategory = name

        // 3. Wykonujemy blok kodu (ponownie delegujemy do tego samego buildera)
        code.delegate = this
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code()

        // 4. PRZYWRACAMY poprzednią kategorię po wyjściu z klamerek
        activeCategory = previousCategory
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