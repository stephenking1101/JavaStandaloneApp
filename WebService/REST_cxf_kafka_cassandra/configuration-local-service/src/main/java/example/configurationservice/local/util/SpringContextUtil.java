package example.configurationservice.local.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * This class saves a map of spring-bean ids to their corresponding
 * interfaces. Any bean-lookup can use this class getBeanId method to
 * obtain a spring bean only specifying the interface class. <br/>
 * The bean-id-map of this class must be consistent <br/>
 * to the applicationContext.xml file.
 * 
 * @author Toy
 * @version $Revision: 896210 $
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * implement ApplicationContextAware interface callback method to
     * setup the context environment
     * 
     * @param context
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * get bean object
     * 
     * @param name
     * @return Object registered bean instance
     * @throws BeansException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException {
        if (applicationContext != null) {
            return (T) applicationContext.getBean(name);
        }
        return null;
    }

    /**
     * requiredType if bean can not be converted, relative exception
     * will throw out
     * 
     * @param name bean registered name
     * @param requiredType returned object type
     * @return Object return requiredType class object
     * @throws BeansException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Object getBean(String name, Class requiredType) throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }

    /**
     * if BeanFactory included a bean which name is matching with
     * it,then return true
     * 
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * Judge whether the registered bean is singleton or prototype
     * 
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }

    /**
     * @param name
     * @return Class register object type
     * @throws NoSuchBeanDefinitionException
     */
    @SuppressWarnings({ "rawtypes" })
    public static Class getType(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }

    /**
     * if given bean name have another name, then return it
     * 
     * @param name
     * @return String[]
     * @throws NoSuchBeanDefinitionException
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getAliases(name);
    }
}