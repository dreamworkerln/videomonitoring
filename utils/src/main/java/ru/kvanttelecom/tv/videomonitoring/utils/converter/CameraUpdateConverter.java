package ru.kvanttelecom.tv.videomonitoring.utils.converter;

import org.mapstruct.Mapper;
import ru.kvanttelecom.tv.videomonitoring.utils.data.Camera;
import ru.kvanttelecom.tv.videomonitoring.utils.data.CameraUpdate;

import javax.annotation.PostConstruct;

@Mapper
public abstract class CameraUpdateConverter {

    @PostConstruct
    private void postConstruct() {}

    //public abstract CameraUpdate toUpdate(Camera camera);
    public abstract Camera toCamera(CameraUpdate update);
}
