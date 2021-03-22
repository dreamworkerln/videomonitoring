package ru.kvanttelecom.tv.videomonitoring.tbot.entities;

import org.springframework.stereotype.Component;
import ru.dreamworkerln.spring.utils.common.mapwrapper.ConcurrentMapWrapper;
import ru.kvanttelecom.tv.videomonitoring.utils.data.Camera;


/**
 * Hold all cameras, received from monitor
 */
@Component
public class CameraStatusMap extends ConcurrentMapWrapper<String, Camera> {}
