package example.foundation.servicediscovery.support.test.ut.finderimpl;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import example.foundation.servicediscovery.support.test.InMemoryAddressFinder;
import example.foundation.servicediscovery.support.util.ConfigurationException;
import example.foundation.servicediscovery.support.util.Service.STATUS;

public class InMemoryAddressFinderTest {
    InMemoryAddressFinder finder = new InMemoryAddressFinder();
    
    @After
    public void clear(){
        finder.clear();
    }
    
    @Test
    public void testAddAddress(){
        String name = "TEST";
        String url = "localhost:123";
        finder.putLocalService(name, 123, null);
        Assert.assertEquals(url, finder.getURI(name));
        
        finder.remove(name);
        try{
            finder.getURI(name);
        }catch(ConfigurationException ex){
            Assert.assertEquals(name, ex.getServiceName());
        }
    }
    
    @Test
    public void testAddMultiAddress(){
        String name = "TEST";
        String[] urls = {"localhost:1001", "localhost:1002", "localhost:1003", 
                "localhost:1004", "localhost:1005", "localhost:1006"};
        Set<String> urlSet = new HashSet<String>();
        for (int i=0; i<urls.length; i++){
            if (i%2 == 0){
                finder.putService(name, name + "-" + i, urls[i], STATUS.DEACTIVE);
            }else{
                finder.putService(name, name + "-" + i, urls[i], STATUS.ACTIVE);
                urlSet.add(urls[i]);
            }
        }
        
        for (int i=0; i<10; i++){
            String url = finder.getURI(name);
            Assert.assertTrue(urlSet.contains(url));
        }
    }
}
