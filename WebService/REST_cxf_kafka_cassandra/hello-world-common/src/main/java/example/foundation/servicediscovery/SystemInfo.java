package example.foundation.servicediscovery;

/**
 * Store all System information here, all -Dxxx properties can be read
 * from here
 */
public class SystemInfo {
    
    private static String SYSTEM_RUNNING_MODE = "SIG_Running_Mode"; 
    
    private static String NODE_NAME = "node.name"; 

    private static String SERVER_IP = "server.ip"; 
    
    /**
     * Indicate the SIG is running on normal environment or testing environment.
     */
    public enum SystemMode{
        /**
         * normal running mode
         */
        Normal,
        /**
         * testing running mode, for internal BIT and FT
         */
        Testing;
        
        /**
         * 
         * 
         * @param value
         * @return SystemMode
         */
        public static SystemMode getSystemMode(String value){
            if(SystemMode.Testing.name().equalsIgnoreCase(value)){
                return SystemMode.Testing;
            }
            return SystemMode.Normal;
        }
    }

    /**
     * get the system mode from system property, for testing mode, the
     * property is -DSIG_Running_Mode=Testing
     * 
     * @return SystemMode
     */
    public static SystemMode getSystemMode(){
        return SystemMode.getSystemMode(System.getProperty(SYSTEM_RUNNING_MODE,"Normal"));
    }

    /**
     * get the node name from system property
     * 
     * @return String
     */
    public static String getNodeName(){
        return System.getProperty(NODE_NAME,"default-node");
    }
    
    /**
     * get the server ip from system property
     * 
     * @return String
     */
    public static String getServerIp(){
        return System.getProperty(SERVER_IP,"127.0.0.1");
    }
}
