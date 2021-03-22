package ru.kvanttelecom.tv.videomonitoring.monitor.services.amqp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.videomonitoring.monitor.services.CameraService;
import ru.kvanttelecom.tv.videomonitoring.utils.data.Camera;

import java.util.List;

@Service
@Slf4j
public class CameraRpcServer {

    @Autowired
    CameraService cameraService;

    @RabbitListener(queues = "#{cameraRpcQueue.getName()}")
    private List<Camera> response(List<String> names) {

        List<Camera> result;

        log.trace("RPC REQUEST <FIND CAMERAS BY NAME> RECEIVED: {}", names);
        try {
            // костыли
            if(names.size() > 0) {
                result = cameraService.findById(names);
            }
            else {
                result = cameraService.findAll();
            }
            log.trace("RPC RESPONSE: {}", result);
        }
        catch(Exception rethrow) {
            log.error("CameraRpcServer.response error:", rethrow);
            throw rethrow;
        }
        return result;
    }


}
