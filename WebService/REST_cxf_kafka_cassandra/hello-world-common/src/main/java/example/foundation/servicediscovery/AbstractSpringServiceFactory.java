package example.foundation.servicediscovery;

import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class AbstractSpringServiceFactory extends AbstractServiceFactory {

    private ApplicationContext applicationContext;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T instance(Class<T> serviceClass, @SuppressWarnings("unused") Map<String, Object> config) {
        if (this.applicationContext == null) {
            this.applicationContext = createApplicationContext();
        }
        final String serviceName = serviceClass.getSimpleName();
        final String beanName = serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1) + "Impl";
        return (T) this.applicationContext.getBean(beanName);
    }

    private ApplicationContext createApplicationContext() {
        final Thread thread = Thread.currentThread();
        final ClassLoader cl = thread.getContextClassLoader();
        thread.setContextClassLoader(getClass().getClassLoader());
        try {
            return new ClassPathXmlApplicationContext(getContextFile());
        } finally {
            thread.setContextClassLoader(cl);
        }
    }

    /**
     * This method must be override to provide the spring application
     * context XML file path.
     * 
     * @return application context file path
     */
    abstract protected String getContextFile();
}
