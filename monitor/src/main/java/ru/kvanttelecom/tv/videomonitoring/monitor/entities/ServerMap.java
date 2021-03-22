package ru.kvanttelecom.tv.videomonitoring.monitor.entities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.dreamworkerln.spring.utils.common.mapwrapper.ConcurrentMapWrapper;
import ru.dreamworkerln.spring.utils.common.mapwrapper.ConcurrentNavigableMapWrapper;
import ru.kvanttelecom.tv.videomonitoring.monitor.configurations.properties.ApplicationProperties;


import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Список камер по серверам
 * ServerName -> CameraMap
 */
@Component
public class ServerMap extends ConcurrentNavigableMapWrapper<String, CameraMap> {

    @Autowired
    ApplicationProperties props;

    @PostConstruct
    private void postConstruct() {

        // init MediaServerMap
        List<String> servers = props.getServers().getServerList();
        for (String server : servers) {
            this.put(server, new CameraMap());
        }
    }



}


