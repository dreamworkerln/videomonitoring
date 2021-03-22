package ru.kvanttelecom.tv.videomonitoring.tbot.services.amqp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.videomonitoring.tbot.services.CameraSynchronizer;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.CameraEvent;

import java.util.List;

@Service
@Slf4j
public class CameraEventReceiver {

    @Autowired
    CameraSynchronizer synchronizer;

    @RabbitListener(queues = "#{cameraUpdateQueue.getName()}")
    private void receive(List<CameraEvent> events) {

        try {
            log.trace("CAMERA EVENT: {}", events);
            synchronizer.syncFromEvent(events);

        }
        catch(Exception rethrow) {
            log.error("Synchronization fromEvent error: ", rethrow);
            throw rethrow;
        }
    }

}
