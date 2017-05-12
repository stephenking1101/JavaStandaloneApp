package example.ws.ex1;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;


public class Client {
	public static void main(String[] args) {
	
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(Servicable.class);
		factory.setAddress("http://localhost:8000/TestWSTD");
		
		Servicable service = (Servicable) factory.create();
		
		System.out.println("[result]" + service.sayHi("Chan Dai Man"));
		
		System.out.println("[result]" + "\n" + service.getInputName("Wang" , "XiaoJuan"));
		
		System.out.println("[result]" + "\n" + service.getFixedName());
		
	}

}
