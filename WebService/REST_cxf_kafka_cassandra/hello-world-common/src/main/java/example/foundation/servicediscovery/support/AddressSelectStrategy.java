package example.foundation.servicediscovery.support;

import java.util.List;

public interface AddressSelectStrategy{
    <T> T getNextAlternate(List<T> alternates);
}