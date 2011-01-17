package an.example.dems;

import an.control.AbstractStatus;

public class DEMSStatus extends AbstractStatus {
    public static final String KEY_RUN_STATUS = "Status";
    // TODO other keys

    public static final String STATUS_RUN_NOTRUN = "NotRun";
    public static final String STATUS_RUN_RUNING = "Running";
    public static final String STATUS_RUN_INITIALIZED = "Initialized";
    // TODO other status

    public DEMSStatus() {
        status.put(KEY_RUN_STATUS, STATUS_RUN_NOTRUN);
    }
}