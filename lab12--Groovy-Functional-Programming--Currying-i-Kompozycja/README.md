Lab 12: Groovy Functional Programming – Currying i Kompozycja
-------------------------------------------------------------

Cel: 
Nauczysz się technik funkcyjnych, które pozwalają tworzyć bardzo elastyczny kod poprzez "zamrażanie" parametrów funkcji (Currying) 
oraz łączenie małych funkcji w większe potoki (Composition).

Zadanie 1: Currying – Tworzenie funkcji z funkcji
-------------------------------------------------

Currying pozwala nam wziąć funkcję z wieloma parametrami i stworzyć na jej bazie nową funkcję, która ma już niektóre parametry ustawione na stałe.

Co masz zrobić:
Stwórz plik `src/main/groovy/FunctionalPower.groovy`:

``````
// 1. Definiujemy ogólną funkcję obliczającą podatek
def calculateTax = { rate, amount -> amount * rate }

// 2. Tworzymy "specjalistyczne" funkcje przy użyciu curry()
def plTax = calculateTax.curry(0.23) // rate jest zamrożone na 0.23
def deTax = calculateTax.curry(0.19) // rate jest zamrożone na 0.19

println "Podatek w PL od 100: " + plTax(100)
println "Podatek w DE od 100: " + deTax(100)

// 3. rcurry (right curry) - mrozi parametry od prawej strony
def taxOf100 = calculateTax.rcurry(100) // amount jest zamrożone na 100
println "Podatek 8% od 100: " + taxOf100(0.08)
``````

Oto profesjonalny test w Spocku, który pokrywa logikę curry (mrożenie od lewej) oraz rcurry (mrożenie od prawej).
Możesz dodać go do pliku `src/test/groovy/FunctionalPowerSpec.groovy`:

Wyjaśnienie mechaniki:
curry(X): Bierze parametry od lewej strony.
Oryginał: (rate, amount)
curry(0.23) -> rate staje się 0.23. Zostaje (amount).
rcurry(Y): Bierze parametry od prawej strony.
Oryginał: (rate, amount)
rcurry(1000) -> amount staje się 1000. Zostaje (rate).

Zadanie 2: Kompozycja Funkcji – Budowanie rurociągów
----------------------------------------------------

W Groovy możesz łączyć funkcje za pomocą operatorów << (kompozycja wsteczna) lub >> (kompozycja w przód). 
To pozwala na budowanie czytelnych potoków przetwarzania danych.

 
def trim = { it.trim() }
def upper = { it.toUpperCase() }
def addBrackets = { "[ $it ]" }

// Łączymy funkcje w jeden "rurociąg" (Pipeline)
// Najpierw trim, potem upper, na końcu addBrackets
def cleanAndFormat = trim >> upper >> addBrackets

println cleanAndFormat("    java-groovy-practices    ")
// Wynik: [ JAVA-GROOVY-PRACTICES ]



Jasne, lecimy z testem do Kompozycji Funkcji.
To jest jeden z najprzyjemniejszych momentów w Groovym – testowanie, jak małe, proste klocki składają się w jedną, 
potężną całość.
Oto test w Spocku, który pokrywa operator >> (wykonywanie po kolei) oraz dla kontrastu operator << (matematyczne składanie funkcji od końca).
Dodaj to do pliku` src/test/groovy/CompositionSpec.groovy`:

Dlaczego warto znać oba operatory?
>> (Forward): Używany najczęściej do przetwarzania danych (ETL). 
> Czyta się go naturalnie: "Weź dane, wyczyść je, sformatuj, zapisz".
<< (Backward): Używany częściej w matematyce lub algorytmach, gdzie myślimy w kategoriach funkcji zewnętrznych i wewnętrznych f(g(x)).



Zadanie 3: Test Spocka (Weryfikacja funkcyjna)
----------------------------------------------

Stwórz `src/test/groovy/FunctionalSpec.groovy`:

````
import spock.lang.Specification

class FunctionalSpec extends Specification {

    def "powinien poprawnie zastosować curry do filtracji"() {
        given: "ogólna funkcja filtrująca"
        def filterBy = { minLength, text -> text.length() >= minLength }
        
        and: "funkcja wyspecjalizowana dla długich słów"
        def isLongWord = filterBy.curry(10)

        expect:
        isLongWord("Groovy") == false
        isLongWord("Java-Groovy-Practices") == true
    }

    def "powinien składać funkcje matematyczne"() {
        given:
        def multiplyBy2 = { it * 2 }
        def add10 = { it + 10 }

        when: "składamy: (x + 10) * 2"
        def pipe = add10 >> multiplyBy2

        then:
        pipe(5) == 30 // (5 + 10) * 2
    }
}
````

PODSUMOWANIE:
-------------

Dlaczego to są "Practices"?

DRY (Don't Repeat Yourself): 
Zamiast pisać dziesięć metod calculateTaxForPL, calculateTaxForDE itd., 
masz jedną ogólną funkcję i generujesz resztę przez curry.

Testowalność: 
Małe funkcje (jak trim czy upper) są banalne do przetestowania. Składając je, masz pewność, że cały proces działa, jeśli klocki są sprawne.

Czytelność: 
Potoki typu data >> clean >> save czytają się jak opis procesu biznesowego, a nie jak skomplikowany kod.



Wyzwanie Finałowe Lab 12:
-------------------------

Stwórz funkcję log(level, date, message) która przyjmuje 3 parametry.
Użyj currying, aby stworzyć funkcję errorLog(message), która zawsze ma level = "ERROR" oraz date = "DZISIEJSZA_DATA"
(możesz użyć new Date().format('HH:mm')).

Zbuduj potok (kompozycję), który:
Przyjmuje surowy tekst.
Dodaje do niego prefiks "LOG: ".
Przekazuje to do Twojej funkcji errorLog.
Napisz test Spocka, który sprawdzi, czy wywołanie tego potoku zwraca poprawny format (np. [ERROR] [12:00] LOG: Treść błędu).


Rozwiązanie wyzwania finałowego Lab 12:
To zadanie jest kwintesencją podejścia funkcyjnego w Groovy: 
bierzemy ogólną logikę, "zamrażamy" konfigurację (poziom błędu i czas), a następnie łączymy to w potok przetwarzania tekstu.

Stwórz plik `src/test/groovy/LoggerChallengeSpec.groovy`:

code
Groovy
download
content_copy
expand_less
import spock.lang.Specification

class LoggerChallengeSpec extends Specification {

    def "powinien zbudować potok logowania używając currying i kompozycji"() {
        given: "1. Bazowa funkcja logująca (3 parametry)"
        def log = { String level, String date, String msg ->
            "[$level] [$date] $msg"
        }

        and: "aktualna godzina (zamrożona na potrzeby testu)"
        String now = new Date().format('HH:mm')

        and: "2. Tworzymy wyspecjalizowaną funkcję errorLog (Currying)"
        // Używamy curry podając dwa argumenty naraz.
        // Groovy przypisze je od lewej: level="ERROR", date=now.
        // Zostaje nam funkcja przyjmująca tylko jeden argument: message.
        def errorLog = log.curry("ERROR", now)

        and: "3. Funkcja dodająca prefiks"
        def addPrefix = { String msg -> "LOG: $msg" }

        when: "4. Budujemy rurociąg (Pipeline): Prefiks -> ErrorLog"
        // Najpierw dodaj prefiks, potem przekaż wynik do errorLog
        def processingPipeline = addPrefix >> errorLog

        and: "uruchamiamy potok z surowym tekstem"
        def result = processingPipeline("Nieudane połączenie z bazą")

        then: "wynik ma poprawny format, zawiera ERROR, godzinę i prefiks"
        result == "[ERROR] [$now] LOG: Nieudane połączenie z bazą"
    }
}
Analiza rozwiązania (Dlaczego to działa?):

log.curry("ERROR", now):

Oryginał: (level, date, msg)

Po curry: level to "ERROR", date to zmienna now.

Wynik: Nowa funkcja, która czeka już tylko na (msg).

addPrefix >> errorLog:

Tekst wchodzi do addPrefix, zmienia się w "LOG: tekst".

Ten zmieniony tekst wpada do errorLog.

errorLog dokłada [ERROR] [HH:mm] na początek.

Gratulacje!

Ukończyłeś Lab 12. Masz teraz solidne podstawy programowania funkcyjnego w Groovy.

Tym samym przeszliśmy przez bardzo szeroki zakres materiału:

Od prostych POGO i kolekcji,

Przez testy w Spocku i bazy danych SQL,

Aż po zaawansowane metaprogramowanie, XML/JSON i podejście funkcyjne.

To był ostatni zaplanowany moduł.
Czy chcesz:

Zakończyć kurs i otrzymać krótkie podsumowanie wszystkiego, co zrobiliśmy?

Czy masz ochotę na jakiś konkretny, "bonusowy" temat (np. wielowątkowość w Groovy, REST Client)?