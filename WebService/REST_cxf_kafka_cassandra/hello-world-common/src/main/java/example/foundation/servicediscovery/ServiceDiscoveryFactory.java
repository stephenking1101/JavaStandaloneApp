package example.foundation.servicediscovery;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The factory of {@link ServiceDiscovery}.
 */
public class ServiceDiscoveryFactory {

    private static Logger logger = LoggerFactory.getLogger(ServiceDiscoveryFactory.class);

    private static Map<ClassLoader, ServiceDiscovery> sdMap = new HashMap<ClassLoader, ServiceDiscovery>();

    private static Map<String, ClassLoader> classLoaderMap = new HashMap<String, ClassLoader>();

    private static ClassLoader defaultClassLoader = ServiceDiscoveryDelegate.class.getClassLoader();

    public static String EXTENDED_CLASSLOADING_DIR = "extended.classloading.dir";

    /**
     * Create a new {@link ServiceLoadingServiceDiscovery} with the
     * current {@link ClassLoader}.
     * 
     * @return {@link ServiceDiscovery}.
     */
    public static ServiceDiscovery getServiceDiscovery() {
        final String classLoadingDir = System.getProperty(EXTENDED_CLASSLOADING_DIR);
        if (classLoadingDir != null) {
            final ClassLoader extendedClassLoader = getClassLoader(new File(classLoadingDir));
            return getServiceDiscovery(extendedClassLoader);
        }
        return getServiceDiscovery(defaultClassLoader);
    }

    public static ServiceDiscovery getServiceDiscovery(String classLoadingDir) {
        if (classLoadingDir != null) {
            return getServiceDiscovery(new File(classLoadingDir));
        }
        return getServiceDiscovery();
    }

    public static ServiceDiscovery getServiceDiscovery(File classLoadingDir) {
        if (classLoadingDir != null) {
            final ClassLoader classLoader = getClassLoader(classLoadingDir);
            return getServiceDiscovery(classLoader);
        }
        return getServiceDiscovery();
    }

    public synchronized static ServiceDiscovery getServiceDiscovery(ClassLoader classloader) {
        ServiceDiscovery sd = sdMap.get(classloader);
        if (sd == null) {
            sd = new ServiceDiscoveryDelegate(classloader);
            sdMap.put(classloader, sd);
        }
        return sd;
    }

    private static ClassLoader getClassLoader(File dir) {
        final String path = dir.getAbsolutePath();
        ClassLoader classLoader = classLoaderMap.get(path);

        if (classLoader == null) {
            synchronized (classLoaderMap) {
                classLoader = classLoaderMap.get(path);
                if (classLoader == null) {
                    if (dir.exists() && dir.isDirectory()) {
                        logger.info("Load the service classes from extended library dir [{}]", path);
                        final FileFilter fileFilter = new FileFilter() {

                            @Override
                            public boolean accept(File file) {
                                return file.isFile() && file.getName().endsWith(".jar");
                            }
                        };

                        final List<URL> urlList = new ArrayList<URL>();
                        for (File jarFile : dir.listFiles(fileFilter)) {
                            try {
                                urlList.add(jarFile.toURI().toURL());
                                logger.info("Extended jar [{}] will be loaded.", jarFile);
                            } catch (MalformedURLException e) {
                                logger.warn("jar file url [{}] is invalid", jarFile);
                            }
                        }
                        classLoader = new ExtendedClassLoader(urlList.toArray(new URL[] {}), defaultClassLoader);
                        classLoaderMap.put(path, classLoader);
                    } else {
                        logger.warn("class loading dir [{}] is not a valid directory, return default class loader",
                                path);
                        return defaultClassLoader;
                    }
                }
            }
        }
        return classLoader;
    }

    /**
     * Returns the mocks map in the ServiceDiscovery, which can be
     * used to add the mock services. The mock services are usually
     * used for testing. Use can inject services associate to specific
     * service types, when this factory is called to get the discovery
     * and discover services, it will first check if there's already
     * mock service in the map with the given service type, if yes,
     * return it directly, otherwise call the service discovery.
     * 
     * @return Map
     */
    public static Map<Class<?>, Object> getMocks() {
        return ServiceDiscoveryDelegate.mocks;
    }

    /**
     * Reload method might cause memory leak. make sure all old class
     * loaded by the old class loader no reference in memory,
     * otherwise old class loader could not be recycled.
     */
    public synchronized static void reload() {
        sdMap.clear();
        classLoaderMap.clear();
    }
}
