package ru.kvanttelecom.tv.videomonitoring.tbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ru.kvanttelecom.tv.videomonitoring.tbot.services.BotScheduler;

import static ru.dreamworkerln.spring.utils.common.StringUtils.isBlank;


@Component
@Slf4j
public class BotStartupRunner implements ApplicationRunner {

    @Autowired
    private Environment environment;

    @Autowired
    private BotScheduler botScheduler;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String port = environment.getProperty("local.server.port");
        if (!isBlank(port)) {
            log.info("Embedded Tomcat run at port: {}", port);
        }
    }
}
