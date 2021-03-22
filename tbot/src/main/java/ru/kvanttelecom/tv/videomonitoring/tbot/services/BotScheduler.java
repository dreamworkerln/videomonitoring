package ru.kvanttelecom.tv.videomonitoring.tbot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BotScheduler {

    @Autowired
    CameraSynchronizer synchronizer;

    /**
     * Синхронизирует локальный список камер с monitor (целиком)
     */
    //@Scheduled(fixedDelayString = "#{applicationProperties.getRefreshIntervalSec() * 1000}", initialDelay = 10 * 1000)
    public void syncAllCameras() {

        try {
            synchronizer.syncAll();
        }
        catch(Exception skip) {
            log.error("Synchronization all error: ", skip);
        }
    }
}
