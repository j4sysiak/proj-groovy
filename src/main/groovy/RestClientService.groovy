import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class RestClientService {

    private static final HttpClient client = HttpClient.newHttpClient()
    private static final JsonSlurper slurper = new JsonSlurper()

    static def getPost(int id) {
        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts/$id"))
                .GET()
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 404) throw new RuntimeException("NOT_FOUND")

        return slurper.parseText(response.body())
    }

    static def createPost(String title, String body, int userId) {
        def jsonBody = JsonOutput.toJson([title: title, body: body, userId: userId])

        def request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return slurper.parseText(response.body())
    }
}