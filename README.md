Podsumowanie kursu Java-Groovy-Practices
----------------------------------------

## Budowanie i uruchamianie

Projekt wymaga JDK 21 i korzysta z Groovy 4.0.33 oraz Spock 2.4
(wariant `groovy-4.0`). Wersje modułów Groovy i Spock są zarządzane
przez ich BOM-y w `build.gradle`.

Ustaw `JAVA_HOME` na katalog JDK 21. Gradle Wrapper 8.14 obsługuje Javę 21;
toolchain projektu wybiera JDK 21 do kompilacji, testów i zadań uruchomieniowych.
Jeśli toolchain nie jest dostępny lokalnie, skonfigurowany resolver Foojay
może go pobrać (wymaga dostępu do sieci).

W PowerShell:

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
```

W IntelliJ IDEA ustaw Project SDK, poziom języka i Gradle JVM na Javę 21,
a następnie przeładuj projekt Gradle.

## Podsumowanie kursu

Przeszedłeś przez pełną ścieżkę:
Lab 1: Pozbyłeś się boilerplate'u (POGO).
Lab 2: Opanowałeś kolekcje i Closures (moc Groovy'ego).
Lab 3: Napisałeś profesjonalne testy w Spocku (tabele prawdy i mocki).
Lab 4: "Zhacksowałeś" Javę przez Metaprogramowanie i DSL.
Lab 5: Połączyłeś Javę z Groovym w jednym procesie.
Lab 6: Zastosowałeś Groovy'ego do automatyzacji budowania projektu.
Co teraz?
Groovy to brama do Gradle i Jenkinsa. 
Jeśli planujesz iść w stronę Seniora lub DevOpsa, znajomość tej "magii" pod maską sprawi, 
że będziesz rozumiał, jak działają nowoczesne systemy budowania, zamiast tylko kopiować 
skrypty z internetu.