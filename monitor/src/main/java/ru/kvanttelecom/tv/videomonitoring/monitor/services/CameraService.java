package ru.kvanttelecom.tv.videomonitoring.monitor.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.videomonitoring.monitor.configurations.properties.ApplicationProperties;
import ru.kvanttelecom.tv.videomonitoring.utils.converter.CameraUpdateConverter;
import ru.kvanttelecom.tv.videomonitoring.utils.data.Camera;
import ru.kvanttelecom.tv.videomonitoring.utils.data.CameraState;
import ru.kvanttelecom.tv.videomonitoring.utils.data.CameraUpdate;
import ru.kvanttelecom.tv.videomonitoring.monitor.entities.CameraMap;
import ru.kvanttelecom.tv.videomonitoring.monitor.entities.ServerMap;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.CameraEvent;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.enums.CameraEventType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CameraService {

    private static final int CAMERA_MAX_LEVEL = 10;
    private static final int CAMERA_THRESHOLD_LEVEL = (int)(CAMERA_MAX_LEVEL * 0.7);

    private static final Double CAMERA_FLAPPING_MIN_RATE = 1./60;

    // минимально детектируемая частота флапа камеры (фильтр высоких частот флапа камеры)
    private double cameraFlappingMinRate;

    @Autowired
    private CameraMap cameras;

    @Autowired
    private ServerMap servers;

    @Autowired
    private CameraUpdateConverter updateConverter;

    @Autowired
    private ApplicationProperties props;

    private void postConstruct() {
        // 1/20 - flussonic media server не будет обновлять информацию о состоянии камеры чаще, чем 1/20 Гц
        // Math.min(1./(props.getRefreshIntervalSec()*2), 1./20);

        cameraFlappingMinRate = CAMERA_FLAPPING_MIN_RATE;
    }


    public List<Camera> findAll() {
        return new ArrayList<>(cameras.getMap().values());
    }


    /**
     * Get cameras that have specified names
     */
    public List<Camera> findById(List<String> names) {

        return names.stream().filter(name -> cameras.containsKey(name))
            .map(cameras::get)
            .collect(Collectors.toList());
    }


    /**
     * Apply cameras updates from selected server
     * Store camera updates from selected server to cameras and calculate CameraEvents
     * @param serverName server
     * @param updates updates from this server
     * @return calculated CameraEvents from this updates
     */
    public List<CameraEvent> applyUpdate(String serverName, List<CameraUpdate> updateList) {

        List<CameraEvent> result = new ArrayList<>();

        Map<String, CameraUpdate> updates =
            updateList.stream().collect(Collectors.toMap(CameraUpdate::getName, Function.identity()));


        // Обход по всем камерам на одном сервере
        try {
            // Карта камер на выбранном сервере
            CameraMap serverCameras = servers.get(serverName);

            // Детектирует состояние первоначальной загрузки камер со стримера -
            // когда для выбранного сервера еще ни одной камеры не было загружено
            // соответственно надо создать событие что камера была инициализирована, а не добавлена.
            // Иначе при 1 запуске по всем работающим камерам будет создано событие что они были добавлены
            //
            // (под добавлением имеется ввиду событие, когда пользователь добавил вручную
            // новую камеру на сервер черев вебинтерфейс flussonic watcher/ flussonic media server)

            boolean serverHasCameras = serverCameras.size() > 0;

            for (CameraUpdate update : updates.values()) {

                String cameraName = update.getName();
                Camera camera = serverCameras.get(cameraName);

                // NEW CAMERA - not found locally - add new camera to servers.cameras
                if (camera == null) {

                    // create new camera from CameraUpdate
                    camera = updateConverter.toCamera(update);

                    // add camera to server - добавляем камеру на сервер
                    serverCameras.put(camera.getName(), camera);

                    // add camera to CameraMap - добавляем камеру в общий список камер
                    cameras.put(camera.getName(), camera);

                    Set<CameraEventType> typeList = new HashSet<>();

                    // ADD CAMERA
                    if (serverHasCameras) {
                        typeList.add(CameraEventType.ADDED);
                        log.debug("{} CAMERA: {} {}",typeList, camera.getName(), camera.getTitle());
                    }
                    // INIT CAMERA
                    else {
                        typeList.add(CameraEventType.INIT);
                        //log.trace("{} CAMERA: {} {}",typeList, camera.getName(), camera.getTitle());
                    }


                    // create notification that camera has been added/inited
                    CameraEvent cameraEvent = new CameraEvent(cameraName, typeList);
                    result.add(cameraEvent);

                }
                // CAMERA STATUS CHANGED - existing camera found - check camera status modifications
                else {

                    Set<CameraEventType> typeList = calcCameraAlternation(camera, update);

                    // If camera status has been changed
                    if (typeList.size() > 0) {
                        CameraEvent event = new CameraEvent(cameraName, typeList);
                        result.add(event);

                        log.debug("{} CAMERA: {} {}",typeList, camera.getName(), camera.getTitle());
                    }
                }
            }

            // DELETED CAMERA
            for (Map.Entry<String, Camera> entry : serverCameras.entrySet()) {

                String cameraName = entry.getKey();
                Camera camera = entry.getValue();

                // Если у нас в serverCameras есть камера, а в обновлении ее нет,
                // Значит камера была удалена с flussonic watcher
                if (!updates.containsKey(cameraName)) {

                    // removing camera from serverCameras
                    serverCameras.remove(cameraName);

                    // removing camera from CameraMap - удаляем камеру из общего списка камер
                    cameras.remove(cameraName);

                    Set<CameraEventType> typeList = new HashSet<>();
                    typeList.add(CameraEventType.DELETED);

                    CameraEvent event = new CameraEvent(cameraName, typeList);
                    result.add(event);

                    log.debug("{} CAMERA: {} {}",typeList, camera.getName(), camera.getTitle());
                }
            }
        }
        // try-catch used only to write error message to log, rethrowing
        catch (Exception rethrow) {
            log.error("Applying update from MediaServer {} error:", serverName);
            throw rethrow;
        }
        return result;
    }




    // ===================================================================================================

    /**
     * Check if camera status has been changed (add/update/delete)
     */
    private Set<CameraEventType> calcCameraAlternation(Camera camera, CameraUpdate update) {

        Set<CameraEventType> result = new HashSet<>();

        CameraState state = camera.getState();

        // Calculating camera flapping -------------------------------

        Double flappingRate = state.calculateUpdateAliveFreq(update);

        //log.info("Частота изменения CameraUpdate.alive: {}", updateAliveFreq);

        if(flappingRate != null) {
            log.info("ИЗМЕНЕНИЕ: Частота изменения CameraUpdate.alive: {}", flappingRate);

            if(!camera.isFlapping() && flappingRate > cameraFlappingMinRate) {
                camera.setFlapping(true);
                result.add(CameraEventType.START_FLAPPING);
            }

            if(camera.isFlapping() && flappingRate < cameraFlappingMinRate) {
                camera.setFlapping(false);
                result.add(CameraEventType.STOP_FLAPPING);
            }
        }

        // Calculating camera online/offline changing -------------
        int step = update.isAlive() ? +1 : -1;


        // update camera.level
        if(Math.abs(state.getLevel() + step) < CAMERA_MAX_LEVEL) {
            state.setLevel(state.getLevel() + step);
        }

        if(camera.isAlive() && state.getLevel() < -CAMERA_THRESHOLD_LEVEL) {
            camera.setAlive(false);
            result.add(CameraEventType.OFFLINE);
        }

        if(!camera.isAlive() && state.getLevel() > +CAMERA_THRESHOLD_LEVEL) {
            camera.setAlive(true);
            result.add(CameraEventType.ONLINE);
        }

        return result;
    }

}
