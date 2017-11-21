package example.foundation.servicediscovery.support;

import java.util.List;
import java.util.Random;

public class RandomStrategy implements AddressSelectStrategy{
    private Random random = new Random();
    
    /**
     * Get next alternate address (String or Service).
     * 
     * @param alternates non-empty List of alternate address 
     * @return
     */
    public <T> T getNextAlternate(List<T> alternates) {
        if (alternates == null || alternates.size() == 0) {
            return null;
        }
        return alternates.remove(random.nextInt(alternates.size()));
    }
}