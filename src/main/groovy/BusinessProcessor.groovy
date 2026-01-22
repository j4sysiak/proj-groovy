

class BusinessProcessor {

    LoggerService logger

    void process(int value) {
        if (value < 0) {
            logger.log("Błąd: wartość ujemna")
        } else {
            logger.log("Sukces: $value")
        }
    }
}