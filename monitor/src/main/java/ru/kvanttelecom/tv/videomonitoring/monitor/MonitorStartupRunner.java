package ru.kvanttelecom.tv.videomonitoring.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ru.kvanttelecom.tv.videomonitoring.monitor.configurations.properties.ApplicationProperties;

import static ru.dreamworkerln.spring.utils.common.StringUtils.isBlank;

@Component
@Slf4j
public class MonitorStartupRunner implements ApplicationRunner {

    @Autowired
    private Environment environment;

    @Autowired
    ApplicationProperties props;

    @Override
    public void run(ApplicationArguments args) {

        String port = environment.getProperty("local.server.port");
        if (!isBlank(port)) {
            log.info("Embedded Tomcat run at port: {}", port);
        }
    }
}
