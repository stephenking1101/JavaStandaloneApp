package example.test.cf;

import java.nio.charset.Charset;

import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

import example.constants.HelloWorldConstants;

public class EmbeddedCassandraHelper {
	private static final String hostIp = "127.0.0.1";
    private static final int port = 9142;
    protected static Cluster cluster;
    protected static Session session;
    protected static CQLDataLoader dataLoader;

    private static final String defaultCreateTablePath = "cql/create-table.cql";
    private static final String defaultDataPath = "cql/data.cql";

    private static boolean isInit = false;

    private static final int maxTryTimes = 3;

    public static synchronized void startEmbeddedCassandraAndLoadData() throws Exception {
        if (!isInit) {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(30000);
            initSession(1);
            loadData(1, false);
            isInit = true;
        }
    }

    private static void initSession(final int currentTryTimes) throws Exception {
        try {
            cluster = new Cluster.Builder().addContactPoints(hostIp).withPort(port).build();
            session = cluster.connect();
            dataLoader = new CQLDataLoader(session);
        } catch (NoHostAvailableException e) {

            if (currentTryTimes <= maxTryTimes) {
                System.out
                        .println(" already try connect Host Available Times is " + currentTryTimes + " , try again ! ");
                Thread.sleep(10 * 1000);
                int tryTimes = currentTryTimes;
                initSession(tryTimes++);
            } else {
                System.out
                        .println(" already try connect Host Available Times is " + currentTryTimes + " ,stop to try! ");
            }
        }

    }

    private static void loadData(final int currentTryTimes, boolean isKeyspaceDeletion) throws Exception {
        int tryTimes = currentTryTimes;
        try {
            dataLoader.load(new ClassPathCQLDataSetWithDiffCharset(defaultCreateTablePath, true, isKeyspaceDeletion,
            		HelloWorldConstants.EXAMPLE_KEYSPACE, Charset.forName("UTF-8")));
            dataLoader.load(new ClassPathCQLDataSetWithDiffCharset(defaultDataPath, false, false,
            		HelloWorldConstants.EXAMPLE_KEYSPACE, Charset.forName("UTF-8")));
        } catch (AlreadyExistsException e) {
            System.out.println(" AlreadyExistsException: " + e.getMessage() + " ;ignore this exception.");
            loadData(tryTimes++, true);
        } catch (NoHostAvailableException e) {
            if (currentTryTimes <= maxTryTimes) {
                Thread.sleep(10 * 1000);
                loadData(tryTimes++, isKeyspaceDeletion);
            } else {
                System.out.println(" already try connect Host Available Times is " + maxTryTimes + " ,stop to try! ");
            }
        }

    }

    public static long queryForCount(String countCql) {
        ResultSet rs = session.execute(countCql);
        long result = 0;
        for(Row r : rs) {
            result = r.get(0, Long.class);
        }
        return result;
    }

    public static void cleanAndCloseSession() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();


        if (session != null) {
            session.close();
        }
        if (cluster != null) {
            cluster.close();
        }
    }

}
