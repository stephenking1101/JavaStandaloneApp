package example.foundation.servicediscovery.support;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.foundation.servicediscovery.ServiceDiscoveryFactory;

public abstract class LocalAddressHelper {
    private static final Logger logger = LoggerFactory.getLogger(LocalAddressHelper.class);
    private static final Set<String> localIPs = getLocalIPs();

    private static Set<String> getLocalIPs(){
        Set<String> localIPs = new HashSet<String>();
        localIPs.add("localhost");
        localIPs.add("127.0.0.1");

        ClusterDeployment clusterDeployment = ServiceDiscoveryFactory.getServiceDiscovery().discover(
                ClusterDeployment.class, null, null);
        if (clusterDeployment == null){
            logger.error("Could not find ClusterDeployment implementation through ServiceDiscovery! " +
                    "The localonly/localfirst sd strategy may be impacted.");
            return localIPs;
        }
        Node currentNode = clusterDeployment.currentNode();
        if (currentNode != null) {
            localIPs.add(currentNode.getExternalIp());
            localIPs.add(currentNode.getInternalIp());
            localIPs.add(currentNode.getHostname());
            localIPs.add(currentNode.getNodename());
        }
        return localIPs;
    }

    public static boolean isLocalIP(String strURI){
        if (strURI == null)
            return false;

        String address = strURI;
        if (address.contains(":")){
            address = address.substring(0, address.indexOf(":"));
        }
        return localIPs.contains(address);
    }

    public static String covertToLocalIpUri(String strURI){
        if (strURI == null) {
            return null;
        }

        String address = strURI;
        if (address.contains(":")){
            address = address.substring(0, address.indexOf(":"));
        }
        return strURI.replaceFirst(address, "localhost");
    }
}
