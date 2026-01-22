
class VaultService {
    void open(String code) {
        if (code != "1234") {
            throw new IllegalArgumentException("BŁĘDNY KOD!")
        }
    }
}