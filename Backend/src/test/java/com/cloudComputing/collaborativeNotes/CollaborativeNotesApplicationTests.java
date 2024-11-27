package com.cloudComputing.collaborativeNotes;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

@SpringBootTest
class CollaborativeNotesApplicationTests {

	@BeforeAll
	static void setup() {
		Dotenv dotenv = Dotenv.configure()
				.directory("../")
				.load();
		System.setProperty("DATABASE_URL", Objects.requireNonNull(dotenv.get("DATABASE_URL")));
		System.setProperty("DATABASE_USERNAME", Objects.requireNonNull(dotenv.get("DATABASE_USERNAME")));
		System.setProperty("DATABASE_PASSWORD", Objects.requireNonNull(dotenv.get("DATABASE_PASSWORD")));
	}

	@Test
	void contextLoads() {
	}
}
