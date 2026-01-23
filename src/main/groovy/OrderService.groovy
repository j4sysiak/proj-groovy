// Serwis obsługi zamówień

class OrderService {
    PaymentProvider paymentProvider

    void processOrder(int amount) {
        if (amount <= 0) {
            throw new InvalidOrderException("Kwota musi być dodatnia!")
        }
        paymentProvider.pay(amount)
    }
}