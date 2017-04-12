package com.superware.restclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class RESTClient {

	// 创建CookieStore实例  
    private static CookieStore cookieStore = null;  
    private static HttpClientContext context = null; 
    
    public static boolean logout(){
    	if(context == null || cookieStore == null) return true;
    	context.setCookieSpecRegistry(null);  
        context.setCookieStore(null);
        cookieStore.clear();
        context.setAttribute("login", false);
    	return true;
    }
    
    public static boolean login(String loginUrl, 
    						 String idField, 
    						 String userName, 
    						 String passField, 
    						 String password, 
    						 String loginErrorUrl,
    						 String sessionID) throws Exception {  
        System.out.println("----Logging in");  
 
        CloseableHttpClient client = HttpClients.createDefault();  
  
        HttpPost httpPost = new HttpPost(loginUrl);  
        Map<String, String> parameterMap = new HashMap<String, String>();  
        parameterMap.put(idField, userName);  
        parameterMap.put(passField, password);  
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(getParam(parameterMap), "UTF-8");  
        httpPost.setEntity(postEntity);  
        //System.out.println("request line:" + httpPost.getRequestLine());  
        try {  
            // 执行post请求  
            HttpResponse httpResponse = client.execute(httpPost);  
            String location = httpResponse.getFirstHeader("Location")  
                    .getValue();  
            if (location != null && location.startsWith(loginErrorUrl)) {  
                System.out.println("----Login Error: " + location);
                throw new Exception("Failed to login!");
            }  
            //printResponse(httpResponse);  
  
            // cookie store  
            setCookieStore(httpResponse, sessionID, getHost(loginUrl));  
            // context  
            setContext();  
        } catch (IOException e) {  
            e.printStackTrace(); 
            throw new Exception(e.getMessage());
        } finally {  
            try {  
                // 关闭流并释放资源  
                client.close();  
            } catch (IOException e) {  
                e.printStackTrace();
            }  
        }
        System.out.println("----Login successfully");  
        return true;
    }
    
    public static List<NameValuePair> getParam(Map<String, String> parameterMap) {  
        List<NameValuePair> param = new ArrayList<NameValuePair>();  
        Iterator<?> it = parameterMap.entrySet().iterator();  
        while (it.hasNext()) {  
            Entry<?, ?> parmEntry = (Entry<?, ?>) it.next();  
            param.add(new BasicNameValuePair((String) parmEntry.getKey(),  
                    (String) parmEntry.getValue()));  
        }  
        return param;  
    } 
    
    public static void printResponse(HttpResponse httpResponse)  
            throws ParseException, IOException {  
        // 获取响应消息实体  
        HttpEntity entity = httpResponse.getEntity();  
        // 响应状态  
        System.out.println("status:" + httpResponse.getStatusLine());  
        System.out.println("headers:");  
        HeaderIterator iterator = httpResponse.headerIterator();  
        while (iterator.hasNext()) {  
            System.out.println("\t" + iterator.next());  
        }  
        // 判断响应实体是否为空  
        if (entity != null) {  
            String responseString = EntityUtils.toString(entity);  
            System.out.println("response length:" + responseString.length());  
            System.out.println("response content:"  
                    + responseString.replace("\r\n", ""));  
        }  
    }
    
    public static void setContext() {  
        //System.out.println("----setContext");  
        context = HttpClientContext.create();  
        Registry<CookieSpecProvider> registry = RegistryBuilder  
                .<CookieSpecProvider> create()  
                .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())  
                .register(CookieSpecs.BROWSER_COMPATIBILITY,  
                        new BrowserCompatSpecFactory()).build();  
        context.setCookieSpecRegistry(registry);  
        context.setCookieStore(cookieStore);  
        context.setAttribute("login", true);
    }  
  
    public static void setCookieStore(HttpResponse httpResponse, String sessionID, String domain) {  
        //System.out.println("----setCookieStore");  
        cookieStore = new BasicCookieStore();  
        // JSESSIONID  
        String setCookie = httpResponse.getFirstHeader("Set-Cookie")  
                .getValue();  
        String JSESSIONID = setCookie.substring((sessionID + "=").length(),  
                setCookie.indexOf(";"));  
        //System.out.println(sessionID + ":" + JSESSIONID);  
        // 新建一个Cookie  
        BasicClientCookie cookie = new BasicClientCookie(sessionID,  
                JSESSIONID);  
        cookie.setVersion(0);  
        cookie.setDomain(domain);  
        cookie.setPath("/");  
        // cookie.setAttribute(ClientCookie.VERSION_ATTR, "0");  
        // cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "127.0.0.1");  
        // cookie.setAttribute(ClientCookie.PORT_ATTR, "8080");  
        // cookie.setAttribute(ClientCookie.PATH_ATTR, "/CwlProWeb");  
        cookieStore.addCookie(cookie);  
    }
    
    public static void sendRequestWithCookieStore(String url, String method, String data) throws Exception {  
        //System.out.println("----sendRequestWithCookieStore");  
        // 使用cookieStore方式  
        CloseableHttpClient client = HttpClients.custom()  
                .setDefaultCookieStore(cookieStore).build();  
        if(method.equals("POST") && data != null && !data.trim().equals("")){
        	HttpPost postRequest = new HttpPost(url);
        	System.out.println(method + " request line:" + postRequest.getRequestLine());
			StringEntity input = new StringEntity(data);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			try {  
                // 执行get请求  
                HttpResponse httpResponse = client.execute(postRequest);  
                //System.out.println("cookie store:" + cookieStore.getCookies());  
                printResponse(httpResponse);  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                try {  
                    // 关闭流并释放资源  
                    client.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        } else {
        	HttpGet httpGet = new HttpGet(url);  
            System.out.println(method + " request line:" + httpGet.getRequestLine());  
            try {  
                // 执行get请求  
                HttpResponse httpResponse = client.execute(httpGet);  
                //System.out.println("cookie store:" + cookieStore.getCookies());  
                printResponse(httpResponse);  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                try {  
                    // 关闭流并释放资源  
                    client.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }
    }
    
    public static void sendRequestWithContext(String url, String method, String data) throws Exception {  
        //System.out.println("----sendRequestWithContext");  
        // 使用context方式  
        CloseableHttpClient client = HttpClients.createDefault();
        if(context != null && (boolean) context.getAttribute("login")){
        	if(method.equals("POST") && data != null && !data.trim().equals("")){
            	HttpPost postRequest = new HttpPost(url);
            	System.out.println(method + " request line:" + postRequest.getRequestLine());  
    			StringEntity input = new StringEntity(data);
    			input.setContentType("application/json");
    			postRequest.setEntity(input);
    			try {  
                    // 执行get请求  
                    HttpResponse httpResponse = client.execute(postRequest, context);  
                    //System.out.println("cookie store:" + cookieStore.getCookies());  
                    printResponse(httpResponse);  
                } catch (IOException e) {  
                    e.printStackTrace();  
                } finally {  
                    try {  
                        // 关闭流并释放资源  
                        client.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
    			return;
            }
        	
        	if(method.equals("PUT") && data != null){
            	HttpPut putRequest = new HttpPut(url);
            	System.out.println(method + " request line:" + putRequest.getRequestLine());  
    			StringEntity input = new StringEntity(data);
    			input.setContentType("application/json");
    			putRequest.setEntity(input);
    			try {  
                    // 执行put请求  
                    HttpResponse httpResponse = client.execute(putRequest, context);  
                    //System.out.println("cookie store:" + cookieStore.getCookies());  
                    printResponse(httpResponse);  
                } catch (IOException e) {  
                    e.printStackTrace();  
                } finally {  
                    try {  
                        // 关闭流并释放资源  
                        client.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
    			return;
            } 

            if(method.equals("DELETE")){
            	HttpDelete httpDelete = new HttpDelete(url);  
    	        System.out.println(method + " request line:" + httpDelete.getRequestLine());  
    	        try {  
    	            // 执行delete请求  
    	            HttpResponse httpResponse = client.execute(httpDelete, context);  
    	            //System.out.println("context cookies:"  
    	            //        + context.getCookieStore().getCookies());  
    	            printResponse(httpResponse);  
    	        } catch (IOException e) {  
    	            e.printStackTrace();  
    	        } finally {  
    	            try {  
    	                // 关闭流并释放资源  
    	                client.close();  
    	            } catch (IOException e) {  
    	                e.printStackTrace();  
    	            }  
    	        }
    	        return;
            }

        	if(method.equals("GET")){
    	        HttpGet httpGet = new HttpGet(url);  
    	        System.out.println(method + " request line:" + httpGet.getRequestLine());  
    	        try {  
    	            // 执行get请求  
    	            HttpResponse httpResponse = client.execute(httpGet, context);  
    	            //System.out.println("context cookies:"  
    	            //        + context.getCookieStore().getCookies());  
    	            printResponse(httpResponse);  
    	        } catch (IOException e) {  
    	            e.printStackTrace();  
    	        } finally {  
    	            try {  
    	                // 关闭流并释放资源  
    	                client.close();  
    	            } catch (IOException e) {  
    	                e.printStackTrace();  
    	            }  
    	        }
            }
        	return;
        }
        
        if(method.equals("POST") && data != null && !data.trim().equals("")){
        	HttpPost postRequest = new HttpPost(url);
        	System.out.println(method + " request line:" + postRequest.getRequestLine());  
			StringEntity input = new StringEntity(data);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			try {  
                // 执行post请求  
                HttpResponse httpResponse = client.execute(postRequest);  
                //System.out.println("cookie store:" + cookieStore.getCookies());  
                printResponse(httpResponse);  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                try {  
                    // 关闭流并释放资源  
                    client.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
			return;
        } 
        
        if(method.equals("PUT") && data != null){
        	HttpPut putRequest = new HttpPut(url);
        	System.out.println(method + " request line:" + putRequest.getRequestLine());  
			StringEntity input = new StringEntity(data);
			input.setContentType("application/json");
			putRequest.setEntity(input);
			try {  
                // 执行put请求  
                HttpResponse httpResponse = client.execute(putRequest);  
                //System.out.println("cookie store:" + cookieStore.getCookies());  
                printResponse(httpResponse);  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                try {  
                    // 关闭流并释放资源  
                    client.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
			return;
        } 

        if(method.equals("DELETE")){
        	HttpDelete httpDelete = new HttpDelete(url);  
	        System.out.println(method + " request line:" + httpDelete.getRequestLine());  
	        try {  
	            // 执行delete请求  
	            HttpResponse httpResponse = client.execute(httpDelete);  
	            //System.out.println("context cookies:"  
	            //        + context.getCookieStore().getCookies());  
	            printResponse(httpResponse);  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        } finally {  
	            try {  
	                // 关闭流并释放资源  
	                client.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }
	        return;
        }
        
        if(method.equals("GET")){
	        HttpGet httpGet = new HttpGet(url);  
	        System.out.println(method + " request line:" + httpGet.getRequestLine());  
	        try {  
	            // 执行get请求  
	            HttpResponse httpResponse = client.execute(httpGet);  
	            //System.out.println("context cookies:"  
	            //        + context.getCookieStore().getCookies());  
	            printResponse(httpResponse);  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        } finally {  
	            try {  
	                // 关闭流并释放资源  
	                client.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }
	        return;
        }
        
        System.out.println(method + " Not Found!");  
    }
    
    /**
     * Will take a url such as http://www.stackoverflow.com and return www.stackoverflow.com
     * 
     * @param url
     * @return
     */
    public static String getHost(String url){
        if(url == null || url.length() == 0)
            return "";

        int doubleslash = url.indexOf("//");
        if(doubleslash == -1)
            doubleslash = 0;
        else
            doubleslash += 2;

        int end = url.indexOf('/', doubleslash);
        end = end >= 0 ? end : url.length();

        int port = url.indexOf(':', doubleslash);
        end = (port > 0 && port < end) ? port : end;

        return url.substring(doubleslash, end);
    }
    
    /**  
     * Get the base domain for a given host or url. E.g. mail.google.com will return google.com
     * @param host 
     * @return 
     */
    public static String getBaseDomain(String url) {
        String host = getHost(url);

        int startIndex = 0;
        int nextIndex = host.indexOf('.');
        int lastIndex = host.lastIndexOf('.');
        while (nextIndex < lastIndex) {
            startIndex = nextIndex + 1;
            nextIndex = host.indexOf('.', startIndex);
        }
        if (startIndex > 0) {
            return host.substring(startIndex);
        } else {
            return host;
        }
    }
}
