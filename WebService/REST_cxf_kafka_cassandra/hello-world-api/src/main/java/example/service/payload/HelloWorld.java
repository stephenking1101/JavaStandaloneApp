package example.service.payload;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HelloWorld {

	@JsonProperty("user_name")
	private String userName;
	
	private Long timestamp;
	
	private Map<String, Object> extension;
	
	public HelloWorld() {
    }

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

	@JsonAnyGetter
	public Map<String, Object> getExtension() {
		return extension;
	}

	public void setExtension(Map<String, Object> extension) {
		this.extension = extension;
	}
	
	@JsonAnySetter
    public void setExtension(String name, Object value) {
        if(this.extension == null) {
            this.extension = new HashMap<String, Object>();
        }

        this.extension.put(name, value);
    }

	@Override
	public String toString() {
		return "HelloWorld{userName=" + userName + ", extension=" + extension
				+ "}";
	}
}
