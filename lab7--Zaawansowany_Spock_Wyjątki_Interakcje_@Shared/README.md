Zadanie 1: Testowanie wyjątków (thrown i notThrown)
---------------------------------------------------

Często musisz sprawdzić, czy Twój kod poprawnie rzuca błędy (np. przy błędnej walidacji).

Co masz zrobić:
Stwórz klasę `VaultService.groovy` (kod produkcyjny) i test `VaultSpec.groovy`


Zadanie 2: Stan współdzielony (@Shared)
---------------------------------------

Normalnie Spock tworzy nową instancję klasy testowej dla każdej metody (czyste środowisko).
Czasem jednak chcesz, aby coś było stworzone tylko raz (np. połączenie z bazą danych).


Lab 7 (część 2): Zaawansowane interakcje i cykl życia
Cel: 
Dowiesz się, jak precyzyjnie sprawdzać nie tylko co metoda zwróciła, ale ile razy i z czym została wywołana. 
Poznasz też różnicę między Mock a Spy.

Zadanie 3: Mock vs Spy (Szpiegowanie)
-------------------------------------

Mock: Całkowicie sztuczny obiekt. Jeśli nie powiesz mu, co ma zwrócić, zwróci null lub 0.
Spy: "Owijka" na prawdziwy obiekt. Wykonuje prawdziwy kod, ale pozwala Ci podsłuchiwać, co się dzieje.

Co masz zrobić:
Stwórz klasę `LoggerService.groovy` oraz `BusinessProcessor.groovy`, a następnie test w `InteractionSpec.groovy`




Zadanie 4: Ograniczenia czasowe (@Timeout) i ignorowanie testów
---------------------------------------------------------------

W profesjonalnych projektach testy nie mogą wisieć w nieskończoność (np. przy testowaniu integracji).
Co masz zrobić:
Dopisz testy sprawdzające limity czasowe: `ControlSpec.groovy`


Zadanie 5: połączenie wszystkiego, czego się dziś nauczyłeś
-----------------------------------------------------------

Stwórz klasę `PaymentProvider.groovy`, która posiada metodę `pay(int amount)`.
Stwórz klasę `OrderService.groovy`, która przyjmuje `PaymentProvider`.
W teście Spocka:
Stwórz Mocka dla `PaymentProvider`.
Napisz test, który sprawdzi, czy jeśli wywołasz OrderService.processOrder(50), 
to metoda pay na mocku zostanie wywołana dokładnie raz z wartością 50.

Dodaj drugi test: 
jeśli amount jest ujemny, `OrderService` powinien rzucić Twój własny wyjątek InvalidOrderException, 
a metoda pay na mocku nigdy (0 *) nie powinna zostać wywołana.

To zadanie to esencja testowania jednostkowego: weryfikujemy zachowanie (interakcje) i reakcję na błędy (wyjątki).

1. Kod produkcyjny (Logic)
   Możesz to umieścić w jednym pliku .groovy w src/main/groovy lub rozdzielić.
Utworz klasy:
`InvalidOrderException.groovy`
`OrderService.groovy`
`PaymentProvider.groovy`

2. Test Spock (Specification)
   W folderze src/test/groovy stwórz plik `OrderServiceSpec.groovy`:

Co tutaj najważniejszego się wydarzyło? (Analiza)
Cardinality (Krotność): 
Zapis 1 * oraz 0 * to unikalna cecha Spocka. 
Pozwala Ci ona mieć pewność, że kod nie wykonuje niepotrzebnych operacji (np. nie płaci dwa razy za to samo zamówienie).

Wildcards (_): 
W drugim teście użyliśmy _. 
Mówimy Spockowi: "Nie obchodzi mnie, z jaką kwotą mogłaby być wywołana płatność – ona po prostu ma się nie wydarzyć".

Kolejność w bloku then:: 
Zauważ, że thrown() i weryfikacja mocka 0 * ... są w jednym bloku. 
Spock jest na tyle inteligentny, że potrafi jednocześnie przechwycić wyjątek i sprawdzić, czy interakcja z mockiem nie nastąpiła "po drodze".



Moje pytanie do Ciebie:

User
ale w teście mamy coś takiego:
then: "metoda pay na mocku musi być wywołana dokładnie 1 raz z parametrem 50
przeciez nie mamy implementcji metody pay(...) z interface: PaymentProvider
to dlaczego to ma zwrócić 1* paymentProviderMock.pay(50)


Odpowiedź:
To jest kluczowe pytanie, które dotyka samej istoty Mockowania. To, co wydaje się "dziwne", jest największą siłą frameworków testowych takich jak Spock.

Oto wyjaśnienie, dlaczego to działa i dlaczego jest to poprawne:

1. Spock tworzy implementację "w locie" (Dynamic Proxy)

Kiedy piszesz def paymentProviderMock = Mock(PaymentProvider), Spock nie bierze Twojego kodu, ale generuje w pamięci RAM nową klasę, która:

Implementuje interfejs PaymentProvider.

Posiada metodę pay(int amount).

Ta metoda jest jednak "pusta" (nie robi nic poza zapisaniem informacji o tym, że została wywołana).

To tak, jakbyś zatrudnił aktora, który udaje bankiera. Aktor nie ma prawdziwych pieniędzy i nie robi przelewów, ale potrafi zapisać w notatniku: "Dzisiaj o 12:00 ktoś kazał mi przelać 50 zł".

2. Sprawdzasz interakcję, a nie logikę

W teście jednostkowym OrderServiceSpec Twoim celem nie jest sprawdzenie, czy pieniądze faktycznie wyszły z konta (to byłby test integracyjny z prawdziwym bankiem).

Twoim celem jest sprawdzenie, czy klasa OrderService wydała polecenie zapłaty.

Sprawdzasz "intencję" serwisu.

Sprawdzasz, czy programista nie zapomniał dopisać linijki paymentProvider.pay(amount).

Sprawdzasz, czy nie pomylił się i nie wysłał np. kwoty 500 zamiast 50.

3. Co się dzieje "pod maską" podczas 1 * ...?

Zapis 1 * paymentProviderMock.pay(50) to nie jest wywołanie metody. To jest oczekiwanie (Assertion).

Uruchamiasz orderService.processOrder(50).

Wewnątrz tej metody wywoływane jest paymentProvider.pay(50).

Ponieważ paymentProvider to Mock, on "przechwytuje" to wywołanie i odkłada do swojej wewnętrznej listy: "Zostałem wywołany raz z liczbą 50".

Po zakończeniu metody when:, Spock zagląda do "notatnika" Mocka i sprawdza: "Czy liczba wpisów zgadza się z tym, co napisałeś w then:?".

Jeśli w notatniku jest jeden wpis z liczbą 50, test przechodzi na zielono.

Co by się stało, gdybyś nie użył Mocka?

Gdybyś chciał użyć prawdziwej klasy, musiałbyś:

Skonfigurować bazę danych.

Połączyć się z API banku.

Mieć internet.

Ryzykować, że test "wyczyści" Ci konto.

Mock pozwala Ci przetestować logikę OrderService w całkowitej izolacji od reszty świata.

Podsumowując:

W teście jednostkowym nie potrzebujesz implementacji PaymentProvider, bo testujesz tylko czy OrderService poprawnie z nim rozmawia. To, czy PaymentProvider faktycznie umie płacić, przetestujesz w innym teście (np. testując konkretną klasę implementującą ten interfejs).

Czy to wyjaśnienie rozjaśnia magię Mocków? Jeśli tak, możemy lecieć do Lab 8, gdzie użyjemy już czegoś "prawdziwego" – bazy danych!