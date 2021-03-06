package example.store.cassandra;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;

import example.store.cassandra.exception.StoreCassandraException;

public class SessionFactoryImpl implements SessionFactory {

	private Logger logger = LoggerFactory.getLogger(SessionFactoryImpl.class);
    private String contactPointsWithPorts;
    private String localDc;
    private Cluster cluster;
    private volatile Session session;
    private int defaultPort = 9042;
    private String keySpace;
    private String username;
    private String password;
    private String trustStorePath;
    private String trustStorePassword;
    private String keyStorePath;
    private String keyStorePassword;
    private boolean sslEnabled;
    private static final String TRUST_STORE_SYS_PROP = "javax.net.ssl.trustStore";
    private static final String TRUST_STORE_PW_SYS_PROP = "javax.net.ssl.trustStorePassword";
    private static final String KEY_STORE_SYS_PROP = "javax.net.ssl.keyStore";
    private static final String KEY_STORE_PW_SYS_PROP = "javax.net.ssl.keyStorePassword";
    
    public SessionFactoryImpl() {
    }

    public void init() {
    	logger.info(" Session Factory begin to init with contact address [{}],local dc [{}],username [{}]",
                contactPointsWithPorts, localDc, username);
        Builder clusterBuilder = Cluster.builder().addContactPointsWithPorts(assembleContactPointsWithPorts())
                .withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder().withLocalDc(localDc).build())
                .withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE);
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            clusterBuilder.withCredentials(username, password);
        }
        if (isSSLConfigured()) {
            logger.info("ssl is configured");
            clusterBuilder.withSSL();
        }

        //cluster holds the known state of the actual Cassandra cluster (notably the Metadata). This class is thread-safe and should be reused
        this.cluster = clusterBuilder.build();
        logger.info(" Session Factory init finish !");
    }

    boolean isSSLConfigured() {
        if (!sslEnabled) {
            return false;
        }
        if (StringUtils.isBlank(trustStorePath)){
            throw new StoreCassandraException("trust store path can not be null!");
        }
        logger.debug("trustStorePath is {}",trustStorePath);
        System.setProperty(TRUST_STORE_SYS_PROP, trustStorePath);
        if (trustStorePassword == null) {
            logger.warn("trust store password is null");
        } else {
            System.setProperty(TRUST_STORE_PW_SYS_PROP, trustStorePassword);
        }

        if (StringUtils.isNotBlank(keyStorePath)) {
            logger.debug("keyStorePath is {}", keyStorePath);
            System.setProperty(KEY_STORE_SYS_PROP, keyStorePath);
            if (keyStorePassword == null) {
                logger.warn("key store password is null");
            } else {
                System.setProperty(KEY_STORE_PW_SYS_PROP, keyStorePassword);
            }
        }
        return true;
    }

    private Collection<InetSocketAddress> assembleContactPointsWithPorts() {
		Collection<InetSocketAddress> addresses = null;
		String[] contactPointsWithPortArray = StringUtils.split(contactPointsWithPorts, ",");
		if ((contactPointsWithPortArray != null) && (contactPointsWithPortArray.length > 0)) {
            addresses = new HashSet<InetSocketAddress>();
			for (String contactPointsWithPort : contactPointsWithPortArray) {
				String[] ipAndPort = StringUtils.split(contactPointsWithPort, ":");
                String ip = ipAndPort[0];
				int port = defaultPort;
                if ((ipAndPort.length > 1) && StringUtils.isNotBlank(ipAndPort[1])) {
					if (StringUtils.isNumeric(ipAndPort[1])) {
						port = Integer.parseInt(ipAndPort[1]);
					} else {
						logger.error("Cassandra ContactPointsWithPort :" + contactPointsWithPort + " invalid port!");
                	}
                }
				addresses.add(new InetSocketAddress(ip, port));

            }
		} else {
			logger.error("Cassandra ContactPointsWithPorts is empty!");
        }
		return addresses;
	}

    @Override
    public Session getSession() {
        if (this.cluster.isClosed()) {
            this.logger.warn("the cluster has been close for some reason, init again");
            synchronized(this) {
                if (this.cluster.isClosed()) {
                    this.init();
                }
            }
        }

        if (this.session == null) {
            synchronized(this) {
                if (this.session == null) {
                	//the Session is what you use to execute queries. Likewise, it is thread-safe and should be reused
                    this.session = this.cluster.connect(this.keySpace);
                }
            }
        }

        return this.session;
    }

    @Override
    public void destroy() {
        if (this.session != null) {
            this.session.close();
        }

        if (this.cluster != null) {
            this.cluster.close();
        }

    }

    public void setKeySpace(String keySpace) {
        if (StringUtils.isBlank(keySpace)) {
            throw new StoreCassandraException("keyspace can not be empty!");
        } else {
            this.keySpace = keySpace;
        }
    }

    public void setLocalDc(String localDc) {
        this.localDc = localDc;
    }

    public void setUsername(String username) {
        this.username = StringUtils.trim(username);
    }

    public void setPassword(String password) {
        this.password = StringUtils.trim(password);
    }

    public void setContactPointsWithPorts(String contactPointsWithPorts) {
        this.contactPointsWithPorts = contactPointsWithPorts;
    }

    public String getTrustStorePath() {
        return this.trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return this.trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStorePath() {
        return this.keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return this.keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public boolean getSslEnabled() {
        return this.sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

}
