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

