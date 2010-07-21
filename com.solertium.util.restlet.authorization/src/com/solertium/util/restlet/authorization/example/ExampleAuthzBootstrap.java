/**
 * 
 */
package com.solertium.util.restlet.authorization.example;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.w3c.dom.Document;

import com.solertium.db.DBException;
import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.restlet.StandardServerComponent;
import com.solertium.util.restlet.authorization.base.BaseAuthorizer;
import com.solertium.util.restlet.authorization.base.Structure;
import com.solertium.util.restlet.authorization.filters.AuthorizationFilter;

/**
 * ExampleAuthzBootstrap.java
 *
 * @author carl.scott <carl.scott@solertium.com>
 *
 */
public class ExampleAuthzBootstrap extends StandardServerComponent {
	
	public static void main(String[] args) {
		try {
			final ExampleAuthzBootstrap component = new ExampleAuthzBootstrap();
			component.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
				 		
	}

	public ExampleAuthzBootstrap() throws DBException {
		super(11001, 11002);
	}
	
	protected void setupDefaultVirtualHost() {
		final Document document = BaseDocumentUtils.impl.getInputStreamFile(
			getClass().getResourceAsStream("struct.xml")
		);
		
		final Structure struct = new Structure(document);
		
		final Context ctx = getContext().createChildContext();
		
		final AuthorizationFilter filter = 
			new AuthorizationFilter(ctx, new BaseAuthorizer(struct));
		
		filter.setNext(new Restlet(ctx) {
			public void handle(final Request request, final Response response) {
				response.setEntity(new StringRepresentation(
					"Congratuations, you are authorized to perform this action!"
				));
				response.setStatus(Status.SUCCESS_OK);
			}
		});
		
		getDefaultHost().attach(filter);
	}

}
