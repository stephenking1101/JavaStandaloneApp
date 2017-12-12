package example.foundation.servicediscovery.support;

public abstract class RealmAwareAddressProvider extends AddressProvider {

    private static ThreadLocal<String> realmThreadLocal = new ThreadLocal<String>();

    @Override
    public String getServiceName() {
        return getServiceNameByRealm(getRealm());
    }

    protected abstract String getServiceNameByRealm(String realm);

    public String getRealm() {
        return realmThreadLocal.get();
    }

    public void setRealm(String realm) {
        realmThreadLocal.set(realm);
    }
}
