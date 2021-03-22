package ru.kvanttelecom.tv.videomonitoring.tbot.services;

import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.kvanttelecom.tv.videomonitoring.tbot.configurations.properties.ApplicationProperties;
import ru.kvanttelecom.tv.videomonitoring.tbot.entities.CameraStatusMap;
import ru.kvanttelecom.tv.videomonitoring.tbot.services.amqp.CameraRpcClient;
import ru.kvanttelecom.tv.videomonitoring.tbot.telebot.Telebot;
import ru.kvanttelecom.tv.videomonitoring.utils.data.Camera;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.CameraEvent;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.enums.CameraEventType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.dreamworkerln.spring.utils.common.StringUtils.notBlank;

/**
 * Synchronize cameras with monitor
 */
@Service
@Slf4j
public class CameraSynchronizer {

    @Autowired
    private CameraStatusMap cameras;

    @Autowired
    private CameraRpcClient cameraRpcClient;

    @Autowired
    private Telebot telebot;

    @Autowired
    ApplicationProperties props;

    /**
     * Reload all cameras from monitor
     */
    public void syncAll() {
        log.trace("CameraSynchronizer: Reloading all cameras");

        Map<String, Camera> updates = cameraRpcClient.findAll().stream()
            .collect(Collectors.toMap(Camera::getName, Function.identity()));

        // prevent client to see empty cameras Map
        // ---------------------------------------------------------------
        // 1. Remove from cameras that not exists in update
        cameras.removeIf(entry -> !updates.containsKey(entry.getKey()));
        // 2. Put all from update to cameras - replace existing
        cameras.putAll(updates);
        // ---------------------------------------------------------------

        log.trace("Updated: {} cameras", updates.size());
    }


    /**
     * Parse update event from monitor
     */
    public void syncFromEvent(List<CameraEvent> events) {

        log.trace("CameraSynchronizer: syncFromEvent");

        // get cameras names from events list
        List<String> names = events.stream().map(CameraEvent::getName).collect(Collectors.toList());

        // go to monitor and get full info about updated cameras
        Map<String, Camera> alteredCameras = cameraRpcClient.findByName(names).stream()
            .collect(Collectors.toMap(Camera::getName, Function.identity()));

        // updating all local cameras (if alteredCameras have any)
        cameras.putAll(alteredCameras);

        // Sending message to telegram
        // Doing it before deleting camera or will have no info about deleted camera
        sendToTelegram(events, alteredCameras);

        // DELETING CAMERAS -----------------------------------------------------

        // Теперь сравниваем events и alteredCameras
        // Если в events было сообщение об удаленных камерах, то
        // в alteredCameras этих камер уже не будет(перед отправкой event monitor их уже удалил).
        // вычисляем разницу - получаем камеры для удаления из cameras

        Set<String> fromEvents = events.stream().map(CameraEvent::getName).collect(Collectors.toSet());
        Set<String> fromAltered = new HashSet<>(alteredCameras.keySet());

        // теперь в fromEvents содержатся камеры, которые были удалены на monitor
        fromEvents.removeAll(fromAltered);

        // удаляем локально камеры из fromEvents
        if(fromEvents.size() > 0) {
            log.debug("DELETED CAMERAS: {}", fromEvents);
            cameras.removeAll(fromEvents);
        }
    }


    // =============================================================================================


    private void sendToTelegram(List<CameraEvent> events, Map<String, Camera> alteredCameras) {

        List<String> lines = new ArrayList<>();
        for (CameraEvent event : events) {

            String name = event.getName();

            Camera camera = alteredCameras.get(name);

            // If camera has been deleted in monitor, borrow camera from local cameras
            if(camera == null) {
                camera = cameras.get(name);
            }

            Assert.notNull(camera, "Camera == null");

            // Filter events -----------------------------------------------------------------
            // 1. remove INIT cameras
            // 2. remove flapping cameras
            Predicate<CameraEvent> noInit = e -> !e.getEventSet().contains(CameraEventType.INIT);
            Predicate<Camera> noFlap =  c -> !c.isFlapping();

            if(noInit.test(event) &&
               noFlap.test(camera)) {

                lines.add(camera.getTitle() + " " + event.getEventSet().toString() + "\n");
            }
        }

        lines.sort(String::compareTo);
        StringBuilder sb = new StringBuilder();
        lines.forEach(sb::append);
        String message = sb.toString();

        // sending to telegram bot group - skip on error
        if (notBlank(message)) {
            try {
                telebot.sendMessage(props.getBotGroup(), message);
            } catch (Exception skip) {
                log.error("Sending telebot error ", skip);
            }
        }
    }


//    private void appendToMessage(StringBuilder sb, CameraEventType eventType, Camera camera) {
//        //sb.append(camera.getName());
//        //sb.append(" <");
//        sb.append(camera.getTitle());
//        //sb.append(">: ");
//        sb.append(eventType);
//        sb.append("\n");
//    }


}


//        if(list == null) {
//            log.warn("CameraSynchronizer.applyUpdate, no reply from monitor, camera update cancelled");
//            return;
//            }
