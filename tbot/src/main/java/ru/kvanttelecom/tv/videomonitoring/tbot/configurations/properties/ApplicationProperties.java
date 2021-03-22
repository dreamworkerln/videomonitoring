package ru.kvanttelecom.tv.videomonitoring.tbot.configurations.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ApplicationProperties {

    @Autowired
    Environment env;

    @Value("${telegram.bot.token}")
    @Getter
    private String botToken;

    @Value("${telegram.bot.group}")
    @Getter
    private Long botGroup;


    @Getter
    private String address;

    @Value("${refresh.interval.sec}")
    @Getter
    private int refreshIntervalSec;


    @PostConstruct
    private void postConstruct() {
        address = env.getProperty("server.host") + ":" + env.getProperty("server.port");
    }
}
