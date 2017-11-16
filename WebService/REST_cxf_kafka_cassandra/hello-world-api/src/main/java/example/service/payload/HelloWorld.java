package example.service.payload;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HelloWorld {

	//@JsonProperty关联json字段到java属性。可以标记属性，也可以用来标记属性的getter/setter方法。当标记属性时，可以对属性字段重命名。当标记方法时，可以把json字段关联到java属性的getter或setter方法。
	@JsonProperty("user_name")
	private String userName;
	
	private Long timestamp;
	
	private Map<String, Object> extension;
	
	//json反序列化为java对象时，该注解用于定义构造函数。当从json创建java时，@JsonCreator注解的构造函数被会调用，如果没有@JsonCreator注解，则默认调用java类的无参构造函数，此时，如果java类中只有有参构造函数，而无默认的无参构造函数，在反序列化时会抛出这样的异常：com.fasterxml.jackson.databind.JsonMappingException，所以，当我们不使用@JsonCreator指定反序列化的构造函数，而又在java类中重载了构造函数时，一定要记得编写类的无参构造函数。
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

	//@JsonAnyGetter用于标记类方法，设置和读取json字段作为键值对存储到map中，这两个注解标记的方法不会处理任何java类中已经定义过的属性变量，只对java中未定义的json字段作处理。
	@JsonAnyGetter
	public Map<String, Object> getExtension() {
		return extension;
	}

	public void setExtension(Map<String, Object> extension) {
		this.extension = extension;
	}
	
	// unmapped key-value pairs from Json Object structs are added to the property
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
