package example.exception;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonGetter;

public class AACommonError implements Serializable {
    private static final long serialVersionUID = -351614323035642044L;
    @XmlElement(
        name = "error"
    )
    private String error;
    @XmlElement(
        name = "error_description"
    )
    private String errorDescription;
    private Map<String, Object> extensions;

    public AACommonError() {
    }

    public AACommonError(String error) {
        this.error = error;
    }

    public AACommonError(String error, String descr) {
        this.error = error;
        this.errorDescription = descr;
    }

    public AACommonError(String error, String descr, Map<String, Object> extensions) {
        this.error = error;
        this.errorDescription = descr;
        this.extensions = extensions;
    }

    @JsonGetter("error")
    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @JsonGetter("error_description")
    public String getErrorDescription() {
        return this.errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        if (this.extensions == null) {
            this.extensions = new HashMap();
        }

        return this.extensions;
    }

    @JsonAnySetter
    public void setExtensions(String name, Object value) {
        if (this.extensions == null) {
            this.extensions = new HashMap();
        }

        this.extensions.put(name, value);
    }

    public String toString() {
        return this.error + ": " + this.errorDescription + ":" + this.extensions;
    }
}
