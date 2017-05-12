package example.ws.ex1;



import javax.jws.WebService;
@WebService
public interface Servicable {

	public String sayHi(String name) ;
	
	public String getFixedName();
	
	public String getInputName(String lname,String fname);
	
}
