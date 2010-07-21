package org.iucn.sis.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.solertium.util.SysDebugger;

public class XMLContentServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String file = request.getPathInfo();

		// in hosted mode, getPathInfo() inexplicably has full info in front of
		// it.
		// hosted mode does not set contextPath or servletPath.
		// this would not be there in a real servlet environment. strip this.
		if (file.startsWith("/org.iucn.SIS/xmlContent"))
			file = file.substring(24);

		// this strip might apply to any given servlet container
		if (file.startsWith("/"))
			file = file.substring(1);

		SysDebugger.getInstance().println("XMLContentServlet received request for " + file);
		if (file == null || ("/".equals(file))) {
			response.sendError(500, "no file given"); // server error on usage
			return;
		}
		InputStream is = getClass().getResourceAsStream(file);
		StringWriter swr = new StringWriter();
		response.setContentType("text/xml");
		// these fake headers cause caching for a day in most browsers
		response.setDateHeader("Last-Modified", System.currentTimeMillis());
		response.setDateHeader("Expires", System.currentTimeMillis() + (1 * 86400 * 1000)); // 1
		// day
		Reader i = new InputStreamReader(is);
		Writer o = swr;
		char[] buf = new char[4096];
		int in;
		do {
			in = i.read(buf);
			if (in > -1) {
				o.write(buf, 0, in);
			}
		} while (in > -1);
		i.close();
		o.flush();
		String s = swr.toString();
		response.setContentLength(s.length()); // this allows us to set the
		// content length
		response.getWriter().write(s);
		response.getWriter().flush();
	}

}
