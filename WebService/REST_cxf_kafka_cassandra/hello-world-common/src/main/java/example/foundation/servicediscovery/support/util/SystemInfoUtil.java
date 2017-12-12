package example.foundation.servicediscovery.support.util;

import example.foundation.servicediscovery.SystemInfo;

public class SystemInfoUtil {
    public static boolean isTestMode(){
        SystemInfo.SystemMode mode = SystemInfo.getSystemMode();
        return SystemInfo.SystemMode.Testing.equals(mode);
    }
}