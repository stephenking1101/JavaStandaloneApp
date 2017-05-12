package example.ws.ex1;

import javax.xml.ws.Endpoint;

public class DeployService {

	public static void deployService() {
		System.out.println("Server start...");
		Service service = new Service();
		
		String address = "http://localhost:8000/TestWSTD?wsdl";
		Endpoint.publish(address, service);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws InterruptedException {

		deployService();
		System.out.println("server ready...");
		Thread.sleep(10000 * 600);
		System.out.println("server exiting");

		System.exit(0);
	}
}
