package rmi;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class Server implements Hello {

    public Server() {}

    public String sayHello() {
        return "Hello, world!";
    }

    public static void main(String args[]) {

    	RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    	List<String> arguments = runtimeMxBean.getInputArguments();
    	final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace(); 
    	final String mainClassName = stackTrace[stackTrace.length - 1].getClassName();
    	System.out.println(mainClassName);
    	for (String vmArg : arguments) System.out.println(vmArg); 
    	
        try {
            Server obj = new Server();
            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Hello", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
