package ru.kvanttelecom.tv.videomonitoring.monitor.configurations.properties;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ApplicationProperties {

//    @Value("${tbot.url}")
//    @Getter
//    private String botUrl;

//    @Value("${delay.threshold.sec}")
//    @Getter
//    private int delayThresholdSec;

    @Value("${refresh.interval.sec}")
    @Getter
    private int refreshIntervalSec;

    @Getter
    @Autowired
    VideoServers servers;

    @PostConstruct
    private void postConstruct() {

        // validating
        if(refreshIntervalSec <=0) {
            throw new IllegalArgumentException("refresh.interval.sec <=0");
        }
    }

    @Component
    public static class VideoServers {

        /**
         * Address of origin streamer or balancer (host:port)
         */
        @Getter(AccessLevel.PUBLIC)
        @Value("#{'${media.server.list}'.split(',')}")
        private List<String> serverList;


        @Value("${media.server.username}")
        @Getter
        private String username;


        @Value("${media.server.password}")
        @Getter
        private String password;

    }
}
