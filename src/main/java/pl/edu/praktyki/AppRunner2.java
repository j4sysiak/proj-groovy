package pl.edu.praktyki;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class AppRunner2 {
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