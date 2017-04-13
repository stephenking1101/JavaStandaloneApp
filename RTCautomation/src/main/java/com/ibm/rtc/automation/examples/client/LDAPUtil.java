package com.ibm.rtc.automation.examples.client;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LDAPUtil {
	private static Log logger = LogFactory.getLog(LDAPUtil.class);

	public static void addUser(String adminName, String adminPassword, String userId, String groupName, String keystore) {

		Hashtable<String, String> env = new Hashtable<String, String>();
		adminName = "CN=" + adminName + ",OU=ibmPeople,DC=InfoDir,DC=Prod,DC=ibm";
		String userName = "CN=" + userId +",OU=ibmPeople,DC=InfoDir,DC=Prod,DC=ibm";
		//groupName = "CN=Infodir-RTC-JazzUsers,OU=HBEU,OU=RTC,OU=Applications,OU=Groups,DC=InfoDir,DC=Prod,DC=ibm";

		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");

		// set security credentials, note using simple cleartext authentication
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, adminName);
		env.put(Context.SECURITY_CREDENTIALS, adminPassword);
		env.put(Context.SECURITY_PROTOCOL, "ssl");

		// connect to my domain controller
		env.put(Context.PROVIDER_URL, "ldaps://glue.systems.uk.ibm:3269");

		//keystore = "C:\\backup\\Software\\jdk\\1.6.0_30\\jre\\lib\\security\\cacerts";
		System.setProperty("javax.net.ssl.trustStore", keystore);

		try {

			// Create the initial directory context
			//DirContext ctx = new InitialLdapContext(env, null);
			LdapContext ctx = new InitialLdapContext(env, null);
			// Create the search controls
			SearchControls searchCtls = new SearchControls();

			// Specify the attributes to return
			String returnedAtts[] = { "employeeID", "sn", "givenName", "mail", "memberOf" };
			searchCtls.setReturningAttributes(returnedAtts);

			// Specify the search scope
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			// specify the LDAP search filter
			String searchFilter = "(&(CN=" + userId +")(mail=*))";

			// Specify the Base for the search
			String searchBase = "OU=ibmPeople,DC=InfoDir,DC=Prod,DC=ibm";

			// initialize counter to total the results
			int totalResults = 0;
			boolean groupExists = false;

			// Search for objects using the filter
			NamingEnumeration<?> answer = ctx.search(searchBase, searchFilter,searchCtls);

			// Loop through the search results
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();

				totalResults++;

				logger.debug(">>>" + sr.getName());

				// Print out some of the attributes, catch the exception if the
				// attributes have no values
				Attributes attrs = sr.getAttributes();
				if (attrs != null) {
					try {
						logger.debug("   employeeID: "
								+ attrs.get("employeeID").get());
						logger.debug("   surname: "
								+ attrs.get("sn").get());
						logger.debug("   firstname: "
								+ attrs.get("givenName").get());
						logger.debug("   mail: "
								+ attrs.get("mail").get());
						Attribute memberOf = attrs.get("memberOf");
						for(int i=0; i < memberOf.size(); i++)  {
							if(memberOf.get(i).toString().equalsIgnoreCase(groupName)) {
								groupExists = true;
								logger.info("   memberOf: " + memberOf.get(i));
							}
							//logger.debug("   memberOf: " + memberOf.get(i));	
						}

					} catch (NullPointerException e) {
						logger.error("Errors listing attributes: " + e);
					}
				}

				if(!groupExists){
					ModificationItem member[] = new ModificationItem[1];
		            member[0]= new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("member", userName)); 
		       
		            ctx.modifyAttributes(groupName,member);
		            logger.info("Added user to group: " + groupName);
				}
			}

			logger.debug("Total results: " + totalResults);
			
			ctx.close();

		} catch (NamingException e) {
			e.printStackTrace();
			logger.error("Problem searching directory: " + e);
		}
	}
}
