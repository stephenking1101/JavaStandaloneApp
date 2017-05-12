package example.ws.ex1;

import javax.jws.WebService;

@WebService
public class Service implements Servicable {

	public String sayHi(String name) {

		return name + " say: Hello World ";
	}

	public String getFixedName() {

		String declaration = "<?xml version=\"1.0\"?>";
		String rootElementStartTag = "<myname>";
		String rootElementEndTag = "</myname>";
		String lnameStartTag = "<lanme>";
		String lnameEndTag = "</lname>";
		String fnameStartTag = "<fanme>";
		String fnameEndTag = "</fname>";

		return declaration + "\n" + rootElementStartTag + "\n\t"
				+ fnameStartTag + "Chen" + fnameEndTag + "\n\t" + lnameStartTag
				+ "DaWen" + lnameEndTag + "\n" + rootElementEndTag;
	}

	public String getInputName(String lname,String fname) {

		String declaration = "<?xml version=\"1.0\"?>";
		String rootElementStartTag = "<myname>";
		String rootElementEndTag = "</myname>";
		String lnameStartTag = "<lanme>";
		String lnameEndTag = "</lname>";
		String fnameStartTag = "<fanme>";
		String fnameEndTag = "</fname>";

		return declaration + "\n" + rootElementStartTag + "\n\t"
				+ fnameStartTag + lname + fnameEndTag + "\n\t" + lnameStartTag
				+ fname + lnameEndTag + "\n" + rootElementEndTag;
	}

}
