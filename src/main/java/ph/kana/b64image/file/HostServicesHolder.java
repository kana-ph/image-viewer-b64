package ph.kana.b64image.file;

import javafx.application.HostServices;

public class HostServicesHolder {
    private static HostServices hostServices;

    public static void setHostServices(HostServices hostServices) {
        HostServicesHolder.hostServices = hostServices;
    }

    public static HostServices getHostServices() {
        return hostServices;
    }
}
