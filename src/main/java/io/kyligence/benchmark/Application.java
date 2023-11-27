package io.kyligence.benchmark;

import lombok.extern.java.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		// ! Benchmark Started
		log.info("Start to run benchmark");
	}

}
