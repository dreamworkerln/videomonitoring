package ru.kvanttelecom.tv.videomonitoring.monitor.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.dreamworkerln.spring.utils.common.rest.RestClient;
import ru.kvanttelecom.tv.videomonitoring.monitor.configurations.properties.ApplicationProperties;
import ru.kvanttelecom.tv.videomonitoring.utils.data.CameraUpdate;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.CameraEvent;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.constants.ControllersPaths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static ru.dreamworkerln.spring.utils.common.StringUtils.formatMsg;
import static ru.dreamworkerln.spring.utils.common.StringUtils.isBlank;
import static ru.kvanttelecom.tv.videomonitoring.utils.dto.constants.Constants.PROTOCOL;


/**
 * Flussonic MediaServer HTTP REST API client
 */
@Service
@Slf4j
public class MediaServerApiClient {

    //BlockingJobPool<String, Object> jobPool = new BlockingJobPool<>(10, Duration.ofMillis(5000), null);


    @Autowired
    RestClient restClient;

//    @Autowired
//    Converter converter;

    @Autowired
    ApplicationProperties props;

    /**
     * Get json info about all cameras from flussonic media server
     * @param url flussonic media server url
     * @return body
     */

    /**
     * Get info about all cameras from selected flussonic media server
     * @param server server
     */
    public List<CameraUpdate> getCamerasUpdate(String server) {


        List<CameraUpdate> result = new ArrayList<>();

        String url = PROTOCOL + server + "/flussonic/api/media";

        String cameraName = "<null>";
        String body = null;
        try {

            //log.trace("DOWNLOADING CAMERA LIST: {}", url);
            ResponseEntity<String> response = restClient.get(url);
            body = response.getBody();

            if(isBlank(body)) {
                return result;
            }

            //noinspection ConstantConditions
            JSONArray list = new JSONArray(body);
            //log.trace("list: {}", list);

            for (int i = 0; i < list.length(); i++) {

                JSONObject camJson = list.getJSONObject(i).getJSONObject("value");
                cameraName = camJson.getString("name");
                //log.trace("NAME: {}", name);

                JSONObject stats = camJson.getJSONObject("stats");
                //log.trace("STATS: {}", stats.toString());

                boolean alive = stats.optBoolean("alive", false);

                int retryCount = stats.optInt("retry_count", 0);

                JSONObject options = camJson.getJSONObject("options");
                //log.trace("OPTIONS: {}", options.toString());

                String title = options.optString("title", null);

                //boolean enabled = !options.optBoolean("disabled", false);

                CameraUpdate cameraUpdate = new CameraUpdate(cameraName,server, title, alive);

                result.add(cameraUpdate);
            }
        }
        // try-catch used only to write error message to log, rethrowing
        catch(JSONException rethrow) {
            String message =
                formatMsg("PARSING CAMERA PROBLEM: {} ", cameraName) +
                formatMsg("JSON PARSE ERROR: {} ", rethrow.getMessage()) +
                formatMsg("PROBLEM JSON:\n{}", body);
            log.error(message);
            throw rethrow;
        }
        return result;
    }




//    /**
//     * Send data to master service
//     */
//    @SneakyThrows
//    public void sendEventsToBot(ConcurrentMap<String, CameraEvent> events) {
//        String url = PROTOCOL + props.getBotUrl() + ControllersPaths.tbot.camera.update;
//        //String json = converter.camEventToJson(events);
//        //restClient.post(url, json).getBody();
//    }
}
