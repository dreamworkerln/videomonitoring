package ru.kvanttelecom.tv.videomonitoring.monitor.entities;

import org.springframework.stereotype.Component;
import ru.dreamworkerln.spring.utils.common.mapwrapper.ConcurrentMapWrapper;
import ru.kvanttelecom.tv.videomonitoring.utils.data.Camera;

/**
 * Camera status map of all cameras(from all fussonic media servers)
 * <br>Общий список всех камер
 */
@Component
public class CameraMap extends ConcurrentMapWrapper<String, Camera> {}
