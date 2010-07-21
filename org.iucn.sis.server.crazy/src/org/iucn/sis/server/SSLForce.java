package org.iucn.sis.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirects GET requests to HTTPS. Forbids POST requests to non-HTTPS.
 * 
 * TODO: Once this has happened with a ticket in the clear, the damage is
 * basically done. Re-evaluate this mechanism in the future to see if any ticket
 * presented can be invalidated (and the user immediately kicked out) if they
 * accidentally break out of the secure channel.
 * 
 * @author rob.heittman
 */
public class SSLForce implements Filter {

	public void destroy() {
		// does nothing
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		if ("http".equals(request.getScheme())) { // need an SSL redirect
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			HttpServletResponse httpServletResponse = (HttpServletResponse) response;
			if ("POST".equals(httpServletRequest.getMethod())) {
				httpServletResponse.sendError(403, "POST not supported over regular HTTP.  Use authenticated HTTPS.");
				// redirected - no further processing in chain
				return;
			}
			String qs = httpServletRequest.getQueryString();
			if (qs == null)
				qs = "";
			if (!"".equals(qs))
				qs = "?" + qs;
			httpServletResponse.sendRedirect("https://" + httpServletRequest.getServerName()
					+ httpServletRequest.getRequestURI() + qs);
			// redirected - no further processing in chain
			return;
		}
		chain.doFilter(request, response);
	}

	public void init(FilterConfig arg0) throws ServletException {
		// does nothing
	}

}
