package ru.kvanttelecom.tv.videomonitoring.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.kvanttelecom.tv.videomonitoring.utils.configurations.annotations.MultimoduleSpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.kvanttelecom.tv.videomonitoring")
public class MonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitorApplication.class, args);
	}

}
