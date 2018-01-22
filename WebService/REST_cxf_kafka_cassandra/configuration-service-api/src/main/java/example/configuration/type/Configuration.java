package example.configuration.type;

import java.io.Serializable;

/**
 * Data Translate Object
 */
public class Configuration implements Serializable{

    public static final String TYPE_BOOLEAN = "java.lang.Boolean";
    public static final String TYPE_BYTE = "java.lang.Byte";
    public static final String TYPE_CHARACTER = "java.lang.Character";
    public static final String TYPE_STRING = "java.lang.String";
    public static final String TYPE_SHORT = "java.lang.Short";
    public static final String TYPE_INTEGER = "java.lang.Integer";
    public static final String TYPE_FLOAT = "java.lang.Float";
    public static final String TYPE_LONG = "java.lang.Long";
    public static final String TYPE_DOUBLE = "java.lang.Double";

    private static final long serialVersionUID = 6497104828893263352L;

    private String name;
    private String type;
    private String value;
    private String description;
    private Integer rw;
    private String usedby;

    /**
     * Get the name
     * 
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get type of the value
     * 
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Set type of the value
     * 
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the value as String
     * 
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value
     * 
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the description
     * 
     * @return String description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the rw tag
     * 
     * @return Integer
     */
    public Integer getRw() {
        return rw;
    }

    /**
     * Set the rw tag
     * 
     * @param rw
     */
    public void setRw(Integer rw) {
        this.rw = rw;
    }

    /**
     * Get the usedby
     * 
     * @return String
     */
    public String getUsedby() {
        return usedby;
    }

    /**
     * Set the usedby
     * 
     * @param usedby
     */
    public void setUsedby(String usedby) {
        this.usedby = usedby;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Configuration)) {
            return false;
        }

        Configuration o = (Configuration) obj;

        // if name equals
        if ((this.name == null) && (o.name != null)) {
            return false;
        }
        if ((this.name != null) && !this.name.equals(o.name)) {
            return false;
        }

        // if value equals
        if ((this.value == null) && (o.value != null)) {
            return false;
        }
        if ((this.value != null) && !this.value.equals(o.value)) {
            return false;
        }

        // if type equals
        if ((this.type == null) && (o.type != null)) {
            return false;
        }
        if ((this.type != null) && !this.type.equals(o.type)) {
            return false;
        }

        // if description equals
        if ((this.description == null) && (o.description != null)) {
            return false;
        }
        if ((this.description != null) && !this.description.equals(o.description)) {
            return false;
        }

        // if rw equals
        if ((this.rw == null) && (o.rw != null)) {
            return false;
        }
        if ((this.rw != null) && !this.rw.equals(o.rw)) {
            return false;
        }

        // if usedby equals
        if ((this.usedby == null) && (o.usedby != null)) {
            return false;
        }
        if ((this.usedby != null) && !this.usedby.equals(o.usedby)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Configuration");
        buf.append("{name=").append(name);
        buf.append(",value=").append(value);
        buf.append(",type=").append(type);
        buf.append(",description=").append(description);
        buf.append(",rw=").append(rw);
        buf.append(",usedby=").append(usedby);
        buf.append("}");
        return buf.toString();
    }
}
