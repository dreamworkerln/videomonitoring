package ru.kvanttelecom.tv.videomonitoring.utils.dto;

import lombok.Data;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.enums.CameraEventType;

import java.util.Set;

/**
 * Event that something changed with camera
 */
@Data
public class CameraEvent {

    /**
     * Camera name - id
     */
    private String name;

    /**
     * Event type
     */
    private Set<CameraEventType> eventSet;

    public CameraEvent() {}


    public CameraEvent(String name, Set<CameraEventType> eventSet) {
        this.name = name;
        this.eventSet = eventSet;
    }

}
