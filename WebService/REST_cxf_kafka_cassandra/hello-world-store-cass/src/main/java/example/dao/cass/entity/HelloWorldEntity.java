
package example.dao.cass.entity;

import java.util.Map;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import example.store.cassandra.entity.AbstractExtendableEntity;

@Table(name = "hello_world", readConsistency = "LOCAL_QUORUM", writeConsistency = "LOCAL_QUORUM")
public class HelloWorldEntity extends AbstractExtendableEntity {
    @PartitionKey
    @Column(name = "user_name")
    String userName;
    @ClusteringColumn
    Long timestamp;
    @Column(name = "extension")
    private Map<String, String> rawExtension;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Map<String, String> getRawExtension() {
        return this.rawExtension;
    }

    @Override
    public void setRawExtension(Map<String, String> map) {
        this.rawExtension = map;
    }

}
