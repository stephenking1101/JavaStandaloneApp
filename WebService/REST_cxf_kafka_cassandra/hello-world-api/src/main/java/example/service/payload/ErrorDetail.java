package example.service.payload;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ErrorDetail implements Serializable {

    private static final long serialVersionUID = -2952002760723099799L;

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    protected Map<String, Object> extension;

    public ErrorDetail(String errorCode, String errorDescription) {
        this.error = errorCode;
        this.errorDescription = errorDescription;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtension() {
        return extension;
    }

    public void setExtension(Map<String, Object> extension) {
        this.extension = extension;
    }

    @JsonAnySetter
    public void setExtension(String name, Object value) {
        if (extension == null) {
            extension = new HashMap<String, Object>();
        }
        this.extension.put(name, value);
    }
}
