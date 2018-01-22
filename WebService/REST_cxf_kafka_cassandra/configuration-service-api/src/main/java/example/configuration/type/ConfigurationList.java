package example.configuration.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Translate Object
 */
public class ConfigurationList implements Serializable{
    /** <code>serialVersionUID</code> */
    private static final long serialVersionUID = 5548005865139078800L;
    private List<Configuration> configurations;

    /**
     * Get the configuration list
     * 
     * @return List&lt;{@link Configuration}&gt;
     */
    public List<Configuration> getConfigurations(){
        if(configurations==null){
            configurations = new ArrayList<Configuration>();
        }
        return this.configurations;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("ConfigurationList{");
        for (Configuration config : configurations) {
            buf.append(config.toString());
        }
        buf.append("}");
        return buf.toString();
    }
}
