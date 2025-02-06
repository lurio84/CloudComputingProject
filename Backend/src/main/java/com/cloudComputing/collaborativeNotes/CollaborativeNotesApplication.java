package com.cloudComputing.collaborativeNotes;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Objects;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class CollaborativeNotesApplication {

	public static void main(String[] args) {
		// Load the .env file and set the environment variables
		Dotenv dotenv;
		try {
			dotenv = Dotenv.configure().directory("../").load(); // Para ejecución con mvn spring-boot:run
		} catch (Exception e) {
			dotenv = Dotenv.configure().directory("./").load(); // Para ejecución con el botón del IDE
		}
		System.setProperty("DATABASE_URL", Objects.requireNonNull(dotenv.get("DATABASE_URL")));
		System.setProperty("DATABASE_USERNAME", Objects.requireNonNull(dotenv.get("DATABASE_USERNAME")));
		System.setProperty("DATABASE_PASSWORD", Objects.requireNonNull(dotenv.get("DATABASE_PASSWORD")));

		SpringApplication.run(CollaborativeNotesApplication.class, args);
	}

}
