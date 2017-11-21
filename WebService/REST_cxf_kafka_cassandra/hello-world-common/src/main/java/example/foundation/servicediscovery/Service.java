package example.foundation.servicediscovery;

import java.util.HashMap;

public class Service {

    private String id;

    private String name;

    private String type;

    private String prefix_URI;

    private STATUS status;

    private HashMap<String,String> attributes = new HashMap<>();

    public Service(){

    }

    public Service(String name, String type, String URI, STATUS status){
        this.name = name;
         this.type= type;
        this.prefix_URI= URI;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrefix_URI() {
        return prefix_URI;
    }

    public void setPrefix_URI(String prefix_URI) {
        this.prefix_URI = prefix_URI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = STATUS.valueOf(status);
    }

    public enum STATUS {ACTIVE, DEACTIVE, BUSY};

    public String getAttribute(String key){
        return attributes.get(key);

    }

    public void setAttribute(String key, String val){
        attributes.put(key, val);
    }
}
