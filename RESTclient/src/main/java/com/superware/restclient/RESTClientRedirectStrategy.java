package com.superware.restclient;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class RESTClientRedirectStrategy extends DefaultRedirectStrategy {

	@Override
	public boolean isRedirected(HttpRequest request, HttpResponse response,
			HttpContext context) throws ProtocolException {
		boolean isRedirect = super.isRedirected(request, response, context);
		if (!isRedirect) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 301 || responseCode == 302) {
                return true;
            }
        }
        return isRedirect;
	}

}
