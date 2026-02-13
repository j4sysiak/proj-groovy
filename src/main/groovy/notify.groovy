/**
 * Symulacja globalnego kroku Jenkinsa (vars/notify.groovy)
 */
def call(Map config = [:]) {
    // Pobieramy parametry z mapy lub ustawiamy domyślne
    def status = config.status ?: 'SUCCESS'
    def channel = config.channel ?: '#general'
    def message = ""

    if (status == 'FAILURE') {
        message = "[SLACK] Alert w kanale ${channel}: Build FAILED!"
    } else {
        message = "[SLACK] Build OK"
    }

    // W Jenkinsie to wypisze się w Console Output buildu
    println message

    // Zwracamy wiadomość, aby ułatwić testowanie w Spocku
    return message
}