Skoro wszystko "działa", to przechodzimy do najważniejszego labu z punktu widzenia pracy w komercyjnych projektach. Groovy rzadko występuje sam – zazwyczaj żyje w symbiozie z Javą.

Lab 5: Integracja Java-Groovy (Joint Compilation)
-------------------------------------------------

Cel: Zrozumienie, jak Gradle kompiluje oba języki naraz oraz jak wywoływać skrypty Groovy dynamicznie z poziomu Javy (częsty przypadek w silnikach reguł biznesowych).

Krok 1: Stworzenie klasy w czystej Javie
----------------------------------------

Dzięki Gradle nie musisz nic konfigurować. Po prostu stwórz folder dla Javy.

W src/main stwórz folder java (jeśli go nie ma).

Stwórz tam pakiet (folder) `pl.edu.praktyki`.

Stwórz plik `LegacyService.java`:

```
package pl.edu.praktyki;

// Czysta Java - średniki, typy, brak magii
public class LegacyService {
public String processData(String input) {
return "Java otrzymała: " + input;
}

    public int calculateComplexMath(int a, int b) {
        return a + b;
    }
}
```

Krok 2: Wykorzystanie Javy w Groovym (Bez wysiłku)
--------------------------------------------------

Teraz pokażemy, że Groovy widzi klasę Javy tak, jakby była jego własna.

W `src/main/groovy` stwórz plik `GroovyBusinessLogic.groovy`:

```
import pl.edu.praktyki.LegacyService

class GroovyBusinessLogic {
def service = new LegacyService()

    def smartProcess(String text) {
        // Groovy używa metody z Javy, ale dodaje swój styl (GString)
        def result = service.processData(text)
        return "Groovy mówi: [${result.toUpperCase()}]"
    }
}
```

Krok 3: Wywołanie Groovy'ego z Javy (Dynamic Scripting)
-------------------------------------------------------

To jest "Power User Feature". 
Wyobraź sobie, że masz aplikację w Javie, ale chcesz dać klientowi możliwość 
pisania własnych formuł zniżkowych w Groovym bez restartu serwera.

W `src/main/java/pl/edu/praktyki` stwórz klasę `AppRunner.java`:


```
package pl.edu.praktyki;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class AppRunner {
public static void main(String[] args) {
// 1. Wywołanie zwykłej klasy Javy
LegacyService javaService = new LegacyService();
System.out.println(javaService.processData("Start"));

        // 2. Dynamiczne wykonanie kodu Groovy z poziomu Javy!
        Binding binding = new Binding();
        binding.setVariable("amount", 100);
        binding.setVariable("tax", 0.23);

        GroovyShell shell = new GroovyShell(binding);
        
        // Ten skrypt może być wczytany z bazy danych lub pliku
        Object result = shell.evaluate("return amount + (amount * tax)");

        System.out.println("Wynik dynamicznego skryptu Groovy: " + result);
    }
}
```


Zadanie dla Ciebie:
-------------------

Uruchom `AppRunner.java`. 
Powinieneś zobaczyć wynik zarówno z Javy, jak i z dynamicznego skryptu Groovy.

Zadanie logiczne: 
Zmodyfikuj skrypt wewnątrz `shell.evaluate(...)` w Javie tak, aby używał instrukcji if.

Jeśli amount > 50, niech nalicza podatek.

Jeśli amount <= 50, niech zwraca sam amount.

Test Spocka: 
Napisz test w Spocku dla klasy GroovyBusinessLogic, aby upewnić się, że poprawnie współpracuje z LegacyService.

Dlaczego to jest ważne?

W wielu firmach (np. bankowość, ubezpieczenia) rdzeń systemu jest w Javie, ale reguły naliczania prowizji są w Groovym. Dzięki temu analityk może zmienić skrypt Groovy "w locie", a Java go po prostu uruchomi przez GroovyShell.

Daj znać, jak poszło z uruchomieniem AppRunnera i czy GroovyShell Cię nie wystraszył! To ostatni krok przed finałowym LAB 6 (Automatyzacja i Gradle).

Rozwiazanie:
------------

To zadanie świetnie pokazuje, jak Java może "zlecić" wykonanie logiki biznesowej elastycznemu skryptowi Groovy.

Oto zmodyfikowana klasa AppRunner.java z zaimplementowaną logiką warunkową wewnątrz skryptu:

```
package pl.edu.praktyki;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class AppRunner {
public static void main(String[] args) {
// Przygotowanie środowiska dla skryptu
Binding binding = new Binding();
binding.setVariable("tax", 0.23);

        GroovyShell shell = new GroovyShell(binding);

        // Treść skryptu jako String (można to trzymać w bazie danych lub pliku .groovy)
        // Implementacja Zadania 2:
        String logicScript = 
            "if (amount > 50) { " +
            "    return amount + (amount * tax) " +
            "} else { " +
            "    return amount " +
            "}";

        // TEST 1: Kwota powyżej 50
        binding.setVariable("amount", 100);
        Object result1 = shell.evaluate(logicScript);
        System.out.println("Wynik dla 100 (z podatkiem): " + result1); // Powinno być 123.0

        // TEST 2: Kwota poniżej 50
        binding.setVariable("amount", 40);
        Object result2 = shell.evaluate(logicScript);
        System.out.println("Wynik dla 40 (bez podatku): " + result2); // Powinno być 40
    }
}
```

Co się tutaj wydarzyło? (Analiza)

Dynamiczność: Zauważ, że w kodzie Javy nie masz ani jednego if. 
Cała decyzja o tym, czy naliczyć podatek, zapada wewnątrz `logicScript`.

Przekazywanie kontekstu: 
Dzięki `binding.setVariable` "wstrzyknąłeś" zmienne z Javy do świata Groovy'ego.

Typ zwracany: 
Groovy automatycznie zwrócił wynik (wynik ostatniej instrukcji lub słowo return) jako obiekt typu Object, 
który w tym przypadku Java widzi jako Double (lub BigDecimal).

Dlaczego to jest "Practice" (Dobra praktyka)?

W ten sposób buduje się systemy, które muszą być bardzo elastyczne. 
Gdyby stawka podatkowa lub próg (50) uległy zmianie, nie musisz rekompilować całej aplikacji w Javie. 
Wystarczy podmienić treść Stringa (np. wczytując go z zewnątrz).

