package ru.kvanttelecom.tv.videomonitoring.tbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.kvanttelecom.tv.videomonitoring.utils.configurations.annotations.MultimoduleSpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.kvanttelecom.tv.videomonitoring")
//@MultimoduleSpringBootApplication
public class BotApplication {
	public static void main(String[] args) {
		SpringApplication.run(BotApplication.class, args);
	}

}
