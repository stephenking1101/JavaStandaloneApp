package example.exception;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * The error includes error code, error description and extension
 * information.
 * 
 * @see AACommonErrorEnum
 * 
 */
public class AACommonError implements Serializable {

    private static final long serialVersionUID = -351614323035642044L;

    @XmlElement(name = AACommonConstants.ERROR_FIELD_ERROR)
    private String error;
    @XmlElement(name = AACommonConstants.ERROR_FIELD_ERROR_DESC)
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

    @JsonGetter(value = AACommonConstants.ERROR_FIELD_ERROR)
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @JsonGetter(value = AACommonConstants.ERROR_FIELD_ERROR_DESC)
    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        if (extensions == null) {
            extensions = new HashMap<String, Object>();
        }
        return extensions;
    }

    @JsonAnySetter
    public void setExtensions(String name, Object value) {
        if (extensions == null) {
            extensions = new HashMap<String, Object>();
        }
        this.extensions.put(name, value);
    }

    @Override
    public String toString() {
        return error + ": " + errorDescription + ":" + extensions;
    }
}
