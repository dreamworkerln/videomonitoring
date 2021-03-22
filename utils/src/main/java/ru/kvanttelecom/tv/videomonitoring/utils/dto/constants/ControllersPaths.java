package ru.kvanttelecom.tv.videomonitoring.utils.dto.constants;

public class ControllersPaths {

    public static class monitor {
        public static class camera {
            public static final String all = "/camera/all";
            public static final String idList = "/camera/findById";
        }
    }

    public static class tbot {
        public static class camera {
            public static final String update = "/camera/update";
        }
    }
}
