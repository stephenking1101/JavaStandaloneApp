package example.foundation.servicediscovery.support.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class AutoPorts {
    private static Logger logger = LoggerFactory.getLogger(AutoPorts.class);
    public static final int DEFAULT_MIN_PORT = 3000;
    public static final int DEFAULT_MAX_PORT = 6000;

    private int minPort;
    private int maxPort;
    private AtomicInteger portCursor = new AtomicInteger();

    private static AutoPorts instance = null;

    private AutoPorts(int minPort, int maxPort) {
        this.minPort = minPort;
        this.maxPort = maxPort;
        portCursor.set(minPort - 1);
    }

    public static void setPortRange(int minPort, int maxPort) {
        if ((instance == null) || (instance.minPort != minPort) || (instance.maxPort != maxPort)) {
            instance = new AutoPorts(minPort, maxPort);
        }
    }

    public static AutoPorts getInstance() {
        if (instance == null) {
            instance = new AutoPorts(DEFAULT_MIN_PORT, DEFAULT_MAX_PORT);
        }
        return instance;
    }

    public static int getNextPort() {
        return getInstance().nextPort();
    }

    public synchronized int nextPort() {
        while (true) {
            int port = portCursor.incrementAndGet();
            if (port <= maxPort) {
                if (isPortNotUsed(port)) {
                    logger.info("Port available: " + port);
                    return port;
                }
            } else {
                logger.info("No unused port found in range [" + minPort + " - " + maxPort + "]");
                return 0;
            }
        }
    }

    private static boolean isPortNotUsed(int port) {
        try {
            InetAddress theAddress = InetAddress.getByName("localhost");
            Socket socket = new Socket(theAddress, port);
            socket.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
