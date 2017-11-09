package example.test.cf;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyFactory {
    public static Server createServer(int port, String contextPath, String webApp) {
        Server jettyServer = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setReuseAddress(true);
        jettyServer.setConnectors(new Connector[] { connector });
        jettyServer.setHandler(new WebAppContext(webApp, contextPath));
        jettyServer.setStopAtShutdown(true);
        return jettyServer;
    }

    public static void reloadContext(Server server) throws Exception {
        WebAppContext context = (WebAppContext) server.getHandler();

        System.out.println("Application reloading");
        context.stop();

        WebAppClassLoader classLoader = new WebAppClassLoader(context);
        classLoader.addClassPath("target/classes");
        classLoader.addClassPath("target/test-classes");
        context.setClassLoader(classLoader);

        context.start();

        System.out.println("Application reloaded");
    }
}