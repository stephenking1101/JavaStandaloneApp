package example.servicediscovery.backend.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import example.foundation.servicediscovery.support.util.Backend;
import example.foundation.servicediscovery.support.util.Service;
import example.foundation.servicediscovery.support.util.Service.STATUS;
import example.servicediscovery.backend.ConsulService;
import example.servicediscovery.backend.ConsulSrvHealth;
import example.servicediscovery.backend.Util;

public class ConsulImpl implements Backend {

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private HashMap<String, ArrayList<Service>> usedCache = new HashMap<>();

    private HashMap<String, Service> nearlyCache = new HashMap<>();

    private HashMap<String, Service> usedHashMap = new HashMap<>();

    private static String srvUri ="http://127.0.0.1:8500/v1/catalog/service/";

    private static  String healthUri = "http://127.0.0.1:8500/v1/health/checks/";



    public ConsulImpl() {

        new SyncThread().start();

    }


    @Override
    public Service getOneActiveInstance(String serviceName) {

        Service res = null;

        lock.readLock().lock();
        try {
            res = nearlyCache.get(serviceName);

            if (res == null){
                ArrayList<Service> services = updateCache(serviceName);
                if(services != null && !services.isEmpty()){
                    res = services.get(0);
                }
            }


            if (res != null && !STATUS.ACTIVE.equals(res.getStatus())) {
                ArrayList<Service> srvList = usedCache.get(serviceName);

                if (srvList != null && srvList.size() > 0) {
                    int size = srvList.size();
                    int offset = ThreadLocalRandom.current().nextInt(0, size + 1) - 1;
                    res = srvList.get(++offset);
                    for (int i = 0; i < size; i++) {
                        if (STATUS.ACTIVE.equals(res.getStatus()))
                            break;
                        else
                            res = null;

                        if (offset >= size) offset = 0;
                        res = srvList.get(++offset);
                    }
                }
            }
        }catch (MalformedURLException e){
            System.out.println(e.getMessage());
        }
        finally {
            lock.readLock().unlock();
        }

        return res;
    }


    @Override
    public boolean isActive(String serviceId) {
        Service srv = usedHashMap.get(serviceId);
        return srv != null && STATUS.ACTIVE.equals(srv.getStatus());
    }

    @Override
    public List<Service> getAllByName(String serviceName) {

        ArrayList<Service> res =null;

        lock.readLock().lock();
        try {
            if (usedCache.get(serviceName) != null) {
                res = (ArrayList<Service>) usedCache.get(serviceName).clone();
            } else {
                res = updateCache(serviceName);
            }
        }catch (MalformedURLException e ){
            System.out.println(e.getMessage());

        } finally {
            lock.readLock().unlock();
        }
        return res;
    }


    private ArrayList<Service> consulService2Service(ArrayList<ConsulService> consulService,
                                                     ArrayList<ConsulSrvHealth> ConsulStatus) {

        ArrayList<Service> res = new ArrayList<>();
        for (ConsulService csv : consulService){
            Service tmp = new Service();
            tmp.setId(csv.getServiceID().concat("_").concat(csv.getNode()));
            tmp.setName(csv.getServiceName());
            tmp.setType("CONSUL-SERVICE");
            tmp.setPrefix_URI(csv.getServiceAddress().concat(":").concat(Integer.toString(csv.getServicePort())));
            for(String str : csv.getServiceTags()){
                int idx = str.indexOf("=");
                if (idx > 0)
                    tmp.setAttribute(str.substring(0,idx),str.substring(idx+1));
                else
                    tmp.setAttribute(str,"");
            }
            //unknown, passing, warning, or critical
            STATUS status = STATUS.ACTIVE;
            for(ConsulSrvHealth health: ConsulStatus){
                if (health.getNode()==csv.getNode() && health.getServiceID() == csv.getServiceID()) {
                    if (health.getStatus().equalsIgnoreCase("critical")){
                        status = STATUS.DEACTIVE;
                        break;
                    }else if (health.getStatus().equalsIgnoreCase("warning"))
                        status = STATUS.BUSY;
                    else if (health.getStatus().equalsIgnoreCase("unknown"))
                        continue;
                }
            }
            tmp.setStatus(status);
            res.add(tmp);
        }
        return res;
    }

    private ArrayList<Service> updateCache(String serviceName) throws MalformedURLException {
        URL srvURL = new URL(srvUri.concat(serviceName).concat("?near=_agent"));
        URL healthURL = new URL(healthUri.concat(serviceName).concat("?near=_agent"));
        ArrayList<ConsulService> tmp = (ArrayList<ConsulService>) Util.json2Object(ConsulClient.getServiceList(srvURL),
                                                                      "example.serivceDiscovery.backend.ConsulService");

        ArrayList<ConsulSrvHealth> tmpStatus = (ArrayList<ConsulSrvHealth>) Util.json2Object(ConsulClient.getServiceList(healthURL),
                "example.serivceDiscovery.backend.ConsulSrvHealth");

        ArrayList<Service> result = consulService2Service(tmp, tmpStatus);

        lock.readLock().unlock();
        lock.writeLock().lock();
        try {
            if (result != null) {
                usedCache.put(serviceName, result);

                if (result.size() > 0) {
                    nearlyCache.put(serviceName, result.get(0));
                } else {
                    System.out.println("Service " + serviceName + " is empty");
                }
                for (Service srv : result) {
                    usedHashMap.put(srv.getId(), srv);
                }
            }
        } finally {
            lock.writeLock().unlock();
            lock.readLock().lock();
        }
        return result;

    }

    /**
     * (non-Javadoc)
     * <p>
     * asynchronous consul's service status
     */
    @SuppressWarnings("unchecked")
    private class SyncThread extends Thread {

        private HashMap<String, ArrayList<Service>> serviceCache = new HashMap<>();

        private HashMap<String, Service> serviceHashMap = new HashMap<>();

        SyncThread() {
            super("async Consul's service status");
        }


        @Override
        public void run() {
            do {
                Iterator it = ((HashMap) usedCache.clone()).keySet().iterator();
                serviceCache.clear();
                serviceHashMap.clear();
                try {

                    while (it.hasNext()) {
                        String name = (String) it.next();
                        URL srvURL = new URL(srvUri.concat(name).concat("?near=_agent"));
                        URL healthURL = new URL(healthUri.concat(name).concat("?near=_agent"));
                        ArrayList<ConsulService> tmp = (ArrayList<ConsulService>) Util.json2Object(
                                ConsulClient.getServiceList(srvURL),
                                "example.serivceDiscovery.backend.ConsulService");

                        ArrayList<ConsulSrvHealth> tmpStatus = (ArrayList<ConsulSrvHealth>) Util.json2Object(
                                ConsulClient.getServiceList(healthURL),
                                "example.serivceDiscovery.backend.ConsulSrvHealth");

                        ArrayList<Service> tmpRes = consulService2Service(tmp, tmpStatus);

                        if (tmp != null) {
                            usedCache.put(name, tmpRes);

                            if (tmp.size() > 0) {
                                nearlyCache.put(name, tmpRes.get(0));
                            }
                            for (Service srv : tmpRes) {
                                serviceHashMap.put(srv.getId(), srv);
                            }
                        }
                    }
                }catch (MalformedURLException e){

                    System.out.println(e.getMessage());

                }
                lock.writeLock().lock();

                try {
                    usedCache.clear();
                    usedHashMap.clear();
                    usedCache = (HashMap<String, ArrayList<Service>>) serviceCache.clone();
                    usedHashMap = (HashMap<String, Service>) serviceHashMap.clone();

                } finally {
                    lock.writeLock().unlock();
                }

                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {

                }
            } while (true);
        }
    }

}
