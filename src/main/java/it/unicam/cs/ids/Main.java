package it.unicam.cs.ids;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("HackHub (Spring Boot) avviato con successo su http://localhost:8080 !");
        System.out.println("Console H2 disponibile su http://localhost:8080/h2-console");    }
}