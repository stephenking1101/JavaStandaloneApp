package example.foundation.servicediscovery.support.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ServiceFactory {

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private HashMap<String, Service> stickyServiceCache = new HashMap<>();

    private Backend backend;

    private static String DEFAULT_TYPE = "LBS_DEFAULT";

    private static final char UNDERSCORE = '_';

    private static final char DASH = '-';


    private ServiceFactory() {
        Map<String, String> env = System.getenv();
        String SUFFIX_LBS = "_SERVICE";
        for (Map.Entry entry : env.entrySet()) {
            String key =entry.getKey().toString();

            if (key.endsWith(SUFFIX_LBS)) {
                String name = key.substring(0,key.indexOf(SUFFIX_LBS));
                Service lbs = new Service(name, DEFAULT_TYPE,
                        entry.getValue().toString(), Service.STATUS.ACTIVE);
                stickyServiceCache.put(name, lbs);
            }
        }

        ServiceLoader<Backend> loadedService =
                ServiceLoader.load(Backend.class);
        if (loadedService.iterator().hasNext()) {
            backend = loadedService.iterator().next();
        }

    }

    private boolean isDefaultType(String type) {
        return DEFAULT_TYPE.equals(type);

    }

    public static ServiceFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }


    private void newStickyInstance(final String serviceName) {
        Service newOne = backend.getOneActiveInstance(serviceName);

        lock.readLock().unlock();
        lock.writeLock().lock();
        try {
            if (newOne != null) {
                stickyServiceCache.put(getUnderscoredServiceName(serviceName).toUpperCase(), newOne);
            }
        } finally {
            lock.writeLock().unlock();
        }

        lock.readLock().lock();
    }

    public String getStickyInstanceURI(final String serviceName) {
        String upperCase = serviceName.toUpperCase();

        lock.readLock().lock();
        Service res = stickyServiceCache.get(getUnderscoredServiceName(serviceName).toUpperCase());

        try {
            if ((backend != null && res != null && !isDefaultType(res.getType())
                    && !backend.isActive(res.getId()))) {
                newStickyInstance(upperCase);
                res = stickyServiceCache.get(getUnderscoredServiceName(serviceName).toUpperCase());
            } else if (res == null && backend != null) {
                newStickyInstance(upperCase);
                res = stickyServiceCache.get(getUnderscoredServiceName(serviceName).toUpperCase());
            } else if (res != null && isDefaultType(res.getType())) {

            } else if (res == null && backend == null) {
                throw new ConfigurationException("missing LBS env configuration and discovery adapter", serviceName);
            }else if ((backend != null && res != null && !isDefaultType(res.getType())
                    && backend.isActive(res.getId()))){

            }

        } finally {
            lock.readLock().unlock();
        }

        if (res == null) {
            throw new NoServiceException("Cannot get any instance", serviceName);
        }

        return res.getPrefix_URI();

    }


    private Service getRandomInstance(final String serviceName) {
        String underscoredSvcName = getUnderscoredServiceName(serviceName);
        String toUpperCase = serviceName.toUpperCase();

        lock.readLock().lock();
        Service res = stickyServiceCache.get(underscoredSvcName.toUpperCase());
        try {
            if (res == null && backend != null) {
                res = backend.getOneActiveInstance(toUpperCase);
            } else if (res != null && !isDefaultType(res.getType()) && backend != null) {
                res = backend.getOneActiveInstance(toUpperCase);
            } else if (res != null && isDefaultType(res.getType())) {

            } else if (res == null && backend == null) {
                throw new ConfigurationException("missing LBS env configuration and discovery adapter", serviceName);
            }

        } finally {
            lock.readLock().unlock();
        }

        if (res == null) {
            throw new NoServiceException("Cannot get any instance", serviceName);
        }

        return res;


    }

    public String getRandomInstanceURI(final String serviceName) {

        return getRandomInstance(serviceName).getPrefix_URI();
    }

    public String getRandomInstanceAsSticky(final String serviceName) {

        Service res = getRandomInstance(serviceName);

        if (!isDefaultType(res.getType())) {
            lock.writeLock().lock();
            try {
                stickyServiceCache.put(getUnderscoredServiceName(serviceName).toUpperCase(), res);
            } finally {
                lock.writeLock().unlock();
            }
        }

        return res.getPrefix_URI();
    }

    public List<Service> getAllInstances(final String serviceName) {
        Service stickyService = stickyServiceCache.get(getUnderscoredServiceName(serviceName).toUpperCase());
        if(stickyService != null){
            return Arrays.asList(stickyService);
        } else if(backend != null){
            return backend.getAllByName(serviceName.toUpperCase());
        } else {
            return new ArrayList<>();
        }
    }


    private String getUnderscoredServiceName(String serviceName){
        return serviceName.replace(DASH,UNDERSCORE);
    }


    private static class SingletonHolder {
        private static final ServiceFactory INSTANCE = new ServiceFactory();
    }
}
