package ru.kvanttelecom.tv.videomonitoring.utils.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.dreamworkerln.spring.utils.common.annotations.Default;
import ru.kvanttelecom.tv.videomonitoring.utils.dto.CameraDto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Camera status
 */
public class Camera {

    @Getter
    protected String name;

    @Getter
    protected String title;

    // Is camera online/offline
    @Getter
    @Setter
    protected boolean alive;

    // Is camera flapping
    @Getter
    @Setter
    protected boolean flapping;

    // Internal camera state
    @Getter
    @Setter
    @JsonIgnore
    protected CameraState state = new CameraState();

    public Camera() {}

    @Default
    public Camera(String name, String title, boolean alive) {
        this.name = name;
        this.title = title;
        this.alive = alive;
        state.setLastUpdateAlive(alive);
    }

    @Override
    public String toString() {
        return "Camera{" +
            "name='" + name + '\'' +
            ", title='" + title + '\'' +
            ", alive=" + alive +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Camera)) return false;
        Camera camera = (Camera) o;
        return name.equals(camera.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
