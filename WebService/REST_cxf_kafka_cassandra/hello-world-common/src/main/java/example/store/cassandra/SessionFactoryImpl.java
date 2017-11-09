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
        this.logger.info(" Session Factory begin to init with contact address [{}],local dc [{}],username [{}]", new Object[]{this.contactPointsWithPorts, this.localDc, this.username});
        Builder clusterBuilder = Cluster.builder().addContactPointsWithPorts(this.assembleContactPointsWithPorts()).withLoadBalancingPolicy(DCAwareRoundRobinPolicy.builder().withLocalDc(this.localDc).build()).withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE);
        if (StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.password)) {
            clusterBuilder.withCredentials(this.username, this.password);
        }

        if (this.isSSLConfigured()) {
            this.logger.info("ssl is configured");
            clusterBuilder.withSSL();
        }

        this.cluster = clusterBuilder.build();
        this.logger.info(" Session Factory init finish !");
    }

    boolean isSSLConfigured() {
        if (!this.sslEnabled) {
            return false;
        } else if (StringUtils.isBlank(this.trustStorePath)) {
            throw new StoreCassandraException("trust store path can not be null!");
        } else {
            this.logger.debug("trustStorePath is {}", this.trustStorePath);
            System.setProperty("javax.net.ssl.trustStore", this.trustStorePath);
            if (this.trustStorePassword == null) {
                this.logger.warn("trust store password is null");
            } else {
                System.setProperty("javax.net.ssl.trustStorePassword", this.trustStorePassword);
            }

            if (StringUtils.isNotBlank(this.keyStorePath)) {
                this.logger.debug("keyStorePath is {}", this.keyStorePath);
                System.setProperty("javax.net.ssl.keyStore", this.keyStorePath);
                if (this.keyStorePassword == null) {
                    this.logger.warn("key store password is null");
                } else {
                    System.setProperty("javax.net.ssl.keyStorePassword", this.keyStorePassword);
                }
            }

            return true;
        }
    }

    private Collection<InetSocketAddress> assembleContactPointsWithPorts() {
        Collection<InetSocketAddress> addresses = null;
        String[] contactPointsWithPortArray = StringUtils.split(this.contactPointsWithPorts, ",");
        if (contactPointsWithPortArray != null && contactPointsWithPortArray.length > 0) {
            addresses = new HashSet();
            String[] arr$ = contactPointsWithPortArray;
            int len$ = contactPointsWithPortArray.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                String contactPointsWithPort = arr$[i$];
                String[] ipAndPort = StringUtils.split(contactPointsWithPort, ":");
                String ip = ipAndPort[0];
                int port = this.defaultPort;
                if (ipAndPort.length > 1 && StringUtils.isNotBlank(ipAndPort[1])) {
                    if (StringUtils.isNumeric(ipAndPort[1])) {
                        port = Integer.parseInt(ipAndPort[1]);
                    } else {
                        this.logger.error("Cassandra ContactPointsWithPort :" + contactPointsWithPort + " invalid port!");
                    }
                }

                addresses.add(new InetSocketAddress(ip, port));
            }
        } else {
            this.logger.error("Cassandra ContactPointsWithPorts is empty!");
        }

        return addresses;
    }

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
                    this.session = this.cluster.connect(this.keySpace);
                }
            }
        }

        return this.session;
    }

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
