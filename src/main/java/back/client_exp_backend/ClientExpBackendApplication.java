package back.client_exp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClientExpBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientExpBackendApplication.class, args);
	}

}
