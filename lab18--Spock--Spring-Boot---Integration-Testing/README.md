Lab 18: Spock + Spring Boot (Integration Testing)
-------------------------------------------------

Cel: 
To jest "Święty Graal". 
Większość pracy w Javie to Spring Boot. 
Pokażę Ci, jak wstrzyknąć Beany Springa do Spocka.

Krok 1: Dodaj zależność do build.gradle (konieczne dla integracji):
-------------------------------------------------------------------

```groovy
dependencies {
    testImplementation 'org.spockframework:spock-spring:2.3-groovy-4.0'
    // Jeśli nie masz Springa w projekcie, normalnie byś go tu miał.
    // Zakładamy symulację lub dodaj: implementation 'org.springframework.boot:spring-boot-starter:3.1.0'
}
```


Krok 2: Symulacja komponentu Springa
Stwórz src/main/groovy/MySpringBean.groovy:

code
Groovy
download
content_copy
expand_less
import org.springframework.stereotype.Service

@Service
class CurrencyService {
String getCurrency() { return "PLN" }
}

Krok 3: Test ze Spring Context
Stwórz src/test/groovy/SpringIntegrationSpec.groovy:

code
Groovy
download
content_copy
expand_less
import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

// W prawdziwym projekcie Spring Boot wystarczy @SpringBootTest
// Tu robimy symulację kontekstu
@ContextConfiguration(classes = [CurrencyService])
class SpringIntegrationSpec extends Specification {

    @Autowired
    CurrencyService currencyService

    def "powinien wstrzyknąć beana Springa do testu Spocka"() {
        expect: "serwis został wstrzyknięty (nie jest null)"
        currencyService != null

        and: "metoda działa"
        currencyService.getCurrency() == "PLN"
    }
}

(Uwaga: Ten Lab wymaga, aby Twój projekt miał zależności Springa. Jeśli masz czysty projekt Groovy, Krok 3 może wymagać dodania bibliotek Spring Core/Context do build.gradle).

Wyzwanie Finałowe (Lab 16-18):

Stwórz klasę DataImporter, która:

Implementuje trait Loggable.

Przyjmuje plik CSV.

Przetwarza go (IO) i jeśli znajdzie błędną linię (np. ujemna cena), używa log() z traitu do zgłoszenia błędu.

Napisz test, który przygotuje plik CSV z błędem i sprawdzi, czy metoda log została wywołana (możesz użyć Spy na klasie DataImporter!).

