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