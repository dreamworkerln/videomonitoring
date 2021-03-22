package ru.kvanttelecom.tv.videomonitoring.tbot.services.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.videomonitoring.utils.data.Camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CameraRpcClient {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private DirectExchange cameraRpcExchange;

    @Autowired
    private Binding bindingCameraRpc;

    public List<Camera> findAll() {

        List<Camera> result;
        log.trace("RPC REQUEST <GET ALL CAMERAS>");
        List<String> empty = new ArrayList<>();

        String exchanger = cameraRpcExchange.getName();
        String routing = bindingCameraRpc.getRoutingKey();

        ParameterizedTypeReference<ArrayList<Camera>> typeRef = new ParameterizedTypeReference<>() {};
        result =  template.convertSendAndReceiveAsType(exchanger, routing, empty, typeRef);
        log.trace("RPC RESPONSE: {}", result);

        if(result == null) {
            throw new RuntimeException("RPC <GET ALL CAMERAS>: NO RESPONSE");
        }
        return result;
    }


    public List<Camera> findByName(List<String> names) {

        List<Camera> result;

        // костыли
        if(names.size() == 0) {
            return new ArrayList<>();
        }

        log.trace("RPC REQUEST <FIND CAMERAS BY NAME>: {}", names);

        String exchanger = cameraRpcExchange.getName();
        String routing = bindingCameraRpc.getRoutingKey();

        ParameterizedTypeReference<ArrayList<Camera>> typeRef = new ParameterizedTypeReference<>() {};
        result = template.convertSendAndReceiveAsType(exchanger, routing, names, typeRef);
        log.trace("RPC RESPONSE: {}", result);

        if(result == null) {
            throw new RuntimeException("RPC <FIND CAMERAS BY NAME>: NO RESPONSE");
        }

        return result;
    }

}
