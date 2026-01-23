// Własny wyjątek
class InvalidOrderException extends RuntimeException {
    InvalidOrderException(String message) {
        super(message)
    }
}