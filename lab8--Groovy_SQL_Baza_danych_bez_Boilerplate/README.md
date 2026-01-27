Cel: 
Zobaczysz, jak Groovy zastępuje 50 linii kodu JDBC (Java Database Connectivity) pięcioma liniami kodu.
Zadanie: Użyjemy bazy danych w pamięci (H2), aby szybko coś zapisać i odczytać.

W kolejnym kroku:
Skonfigurujemy bazę danych H2 (w pamięci RAM).
Zobaczysz, jak Groovy potrafi wykonać zapytanie SQL i zamienić wynik w listę Map w jednej linii.
Połączymy to ze Spockiem, aby zrobić test integracyjny z bazą danych.

Krok 1: Dodaj zależność H2 do projektu
--------------------------------------
Dodaj zależność do build.gradle: `  implementation 'com.h2database:h2:2.1.214'  `

Krok 2: Podstawy operacji SQL (Skrypt)
--------------------------------------
Stwórz plik `src/main/groovy/SqlQuickstart.groovy`. 
Zobacz, jak mało kodu potrzeba, by zarządzać danymi:

Krok 3: Integracja SQL ze Spockiem (Test Integracyjny)
------------------------------------------------------
To jest najczęstszy scenariusz: chcesz przetestować, czy Twój kod poprawnie zapisuje dane w prawdziwej bazie.
Stwórz `src/test/groovy/DatabaseSpec.groovy`:



Dlaczego to jest ważne (Kluczowe pojęcia):
Bezpieczeństwo (GString magic): Zauważ, że piszemy VALUES ($taskTitle). W czystej Javie to byłoby niebezpieczne (podatne na SQL Injection). W Groovy SQL, jeśli używasz zmiennej wewnątrz cudzysłowu w zapytaniu, Groovy automatycznie tworzy bezpieczny PreparedStatement.
eachRow: Nie musisz zamykać ResultSet ani Statement. Groovy robi to za Ciebie.
H2 In-Memory: Idealne do testów – baza powstaje w locie i znika po teście, nie musisz instalować Postgresa czy MySQL na komputerze.