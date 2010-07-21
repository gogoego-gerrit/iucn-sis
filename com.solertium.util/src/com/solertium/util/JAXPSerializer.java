/*
 * Copyright (C) 2002-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package com.solertium.util;

import java.io.Writer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a serializer for partial DOMs, which uses the JAXP API and XSL
 * stylesheets to generate pretty-printed XML output for a given Node. This
 * serializer should work similarly with any JAXP compliant XSL transformation
 * engine (e.g. Xalan), but, transformers do have their idiosyncrasies. Be
 * warned!
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class JAXPSerializer {

	private static TransformerFactory tfac = TransformerFactory.newInstance();

	public static void serialize(final Document d, final Writer w)
			throws Exception {
		final Transformer t = tfac.newTransformer();
		t.setOutputProperty("method", "xml");
		t.setOutputProperty("omit-xml-declaration", "no");
		t.transform(new DOMSource(d), new StreamResult(w));
	}

	public static void serialize(final Node n, final Writer w) throws Exception {
		final Transformer t = tfac.newTransformer();
		t.setOutputProperty("method", "xml");
		t.setOutputProperty("omit-xml-declaration", "yes");
		t.transform(new DOMSource(n), new StreamResult(w));
	}

	public static void serializeChildren(final Node n, final Writer w)
			throws Exception {
		final NodeList nl = n.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			final Node nn = nl.item(i);
			if (nn.getNodeType() != Node.ATTRIBUTE_NODE)
				JAXPSerializer.serialize(nn, w);
		}
	}
}