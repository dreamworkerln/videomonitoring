package ru.kvanttelecom.tv.videomonitoring.monitor.services.amqp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.CameraEvent;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class CameraEventSender {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private DirectExchange cameraUpdateExchange;

    @Autowired
    private Binding bindingCameraUpdate;


    public void send(List<CameraEvent> events) {

        String exchanger = cameraUpdateExchange.getName();
        String routing = bindingCameraUpdate.getRoutingKey();

        log.trace("SENDING CAMERA EVENTS: {}", events);
        template.convertAndSend(exchanger, routing, events);
        //log.trace("CAMERA EVENTS SEND");
    }

}
