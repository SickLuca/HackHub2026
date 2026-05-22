package it.unicam.cs.ids;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("HackHub (Spring Boot) successfully started at http://localhost:8080 !");
        System.out.println("H2 Console available at http://localhost:8080/h2-console");
        System.out.println("Swagger available at http://localhost:8080/swagger-ui/index.html");
    }
}