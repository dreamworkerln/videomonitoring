package ru.kvanttelecom.tv.videomonitoring.monitor.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.videomonitoring.monitor.configurations.properties.ApplicationProperties;
import ru.kvanttelecom.tv.videomonitoring.monitor.services.amqp.CameraEventSender;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.CameraEvent;
import ru.kvanttelecom.tv.videomonitoring.utils.data.CameraUpdate;
import ru.kvanttelecom.tv.videomonitoring.monitor.entities.CameraMap;
import ru.kvanttelecom.tv.videomonitoring.monitor.entities.ServerMap;

import java.util.*;

import static ru.dreamworkerln.spring.utils.common.StringUtils.formatMsg;


/**
 * Main monitoring process, has been scheduled in interval
 */
@Service
@Slf4j
public class MonitoringScheduler {

    @Autowired
    private MediaServerApiClient apiClient;

    @Autowired
    private ServerMap servers;

    @Autowired
    CameraService cameraService;

    @Autowired
    CameraEventSender cameraEventSender;



    @Autowired
    ApplicationProperties props;

    /**
     * Monitoring camera updates on flussonic media servers
     */
    //
    @Scheduled(fixedDelayString = "#{applicationProperties.getRefreshIntervalSec() * 1000}",
        initialDelayString = "#{applicationProperties.getRefreshIntervalSec()/2 * 1000}")
    private void monitor() {

        try {

            // Events from all servers
            List<CameraEvent> events = new ArrayList<>();

            // CameraUpdate name index - checking camera uniqueness (on all servers)
            Map<String,CameraUpdate> nameIndex = new HashMap<>();

            // обходим по всем серверам
            for (Map.Entry<String, CameraMap> server : servers.entrySet()) {

                String serverName = null;
                try {
                    serverName = server.getKey();

                    //log.trace("Scanning server: {}", serverName);

                    // get updates from specified flussonic media server
                    List<CameraUpdate> update = apiClient.getCamerasUpdate(serverName);


                    checkDuplicate(update, nameIndex);

/*                    // FixMe - удалить try catch
                    try {
                        checkDuplicate(update, nameIndex);
                    }
                    catch (Exception skip) {
                        log.error("Found duplicate cameras on server: {}", serverName, skip);
                    }
*/


                    // calculate update events for selected server
                    List<CameraEvent> serverEvents = cameraService.applyUpdate(serverName, update);

                    events.addAll(serverEvents);
                }
                // have error - continue to next server
                catch (Exception skip) {
                    log.error("Getting data from server {} error:", serverName, skip);
                }
            }

            // sending to bot ---------------------------------------
            if (events.size() > 0) {
                // sending events to message aggregator(bot)
                try {
                    cameraEventSender.send(events);
                }
                // skip on error
                catch (Exception skip) {
                    log.error("Sending to aggregator(bot) error:", skip);
                }
            } else {
                //log.trace("Nothing to send");
            }

        }
        // Unexpected error - shutdown program
        catch (Exception fatal) {
            log.error("Monitor strange error, shutdown: ", fatal);
            System.exit(-1);
        }
    }


    /**
     * Check duplicate cameras (by name) on different flussonic media servers
     * <b>If duplicate camera found on server then all cameras update from this server will be rejected
     * @param update update from specific server
     * @param nameIndex cameras name index of all cameras on all servers
     */
    private void checkDuplicate(List<CameraUpdate> update, Map<String, CameraUpdate> nameIndex) {
        // check for duplicates

        List<CameraUpdateDuplicate> duplicates = new ArrayList<>();

        update.forEach(u -> {
            CameraUpdate exists = nameIndex.get(u.getName());
            if(nameIndex.containsKey(u.getName())) {
                duplicates.add(new CameraUpdateDuplicate(exists, u));
            }
            else {
                nameIndex.put(u.getName(), u);
            }
        });

        if(duplicates.size() > 0) {
            StringBuilder sb = new StringBuilder("Found duplicate cameras:\n");

            String prefix = "";
            for (CameraUpdateDuplicate d : duplicates) {
                sb.append(prefix);
                prefix = "\n";
                sb.append(d);

/*                // FixMe  - удалить строку, поиск сканированием всех строк
                // Добавил, чтобы удалялись дубликаты камер, иначе весь update будет отброшен
                update.removeIf(u->u.getName().equals(d.exists.getName()));
*/
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }



    private static class CameraUpdateDuplicate {

        @Getter
        private final CameraUpdate exists;
        private final CameraUpdate update;


        private CameraUpdateDuplicate(CameraUpdate exists, CameraUpdate update) {
            this.exists = exists;
            this.update = update;
        }

        @Override
        public String toString() {
            return "[" +
                "exists={name='" + exists.getName() + "', label='" + exists.getTitle() + "', server=" + exists.getServerName() + "}, " +
                "update={name='" + update.getName() + "', label='" + update.getTitle() + "', server=" + update.getServerName() + "}]";
        }
    }


}
