package example.store.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;

public class CassandraTemplate {

	private SessionFactory sessionFactory;
    private volatile MappingManager mappingManager;

    public CassandraTemplate() {
    }

    public <A> A getAccessor(Class<A> accessorClazz) {
        return this.getMappingManager().createAccessor(accessorClazz);
    }

    public MappingManager getMappingManager() {
        if (this.mappingManager == null) {
            synchronized(this) {
                if (this.mappingManager == null) {
                    this.mappingManager = new MappingManager(this.sessionFactory.getSession());
                }
            }
        }

        return this.mappingManager;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session getSession() {
        return this.sessionFactory.getSession();
    }
}
