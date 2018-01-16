package example.servicediscovery.backend;

public class LocalFileConstants {
    public static String SERVICES_FILE_PATH = "/var/lib/iam-sdsysnc/sd-services.yml";
    public static final long POLLING_INTERVAL = 1000L;

    public static String LINE = "_";
    public static String TYPE = "CONSUL-SERVICE";

    public static String ADDRESS = "Address";
    public static String PORT = "Port";
    public static String STATUS = "Status";
    public static String TAGS = "Tags";
    public static String STATUS_PASSING = "passing";
    public static String STATUS_WARNING = "warning";
    public static String STATUS_CRITICAL = "critical";
    public static String STATUS_UNKNOWN = "unknown";

}
