package example.foundation.servicediscovery;

import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedClassLoader extends URLClassLoader {

    private static Logger logger = LoggerFactory.getLogger(ExtendedClassLoader.class);

    public ExtendedClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    logger.debug("{} is not found in extended class path, find it in parent class loader.", name);
                }

                if (c == null) {
                    return super.loadClass(name, resolve);
                }

                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
            return c;
        }
    }
}
