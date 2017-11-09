package example.store.cassandra;

import com.datastax.driver.core.Session;

public interface SessionFactory {
	Session getSession();

    void destroy();
}
