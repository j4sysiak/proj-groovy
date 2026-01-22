// język: groovy
class LoggerServiceImpl implements LoggerService {
    void log(String message) {
        println "REAL LOG: $message"
    }
}
