/*
 * Copyright (C) 2007-2008 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * GoGoEgo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * GoGoEgo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GoGoEgo.  If not, see http://www.gnu.org/licenses/.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, only the GNU General Public License
 * grants you rights to modify or redistribute this code.
 */

package org.iucn.sis.server.baserestlets;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.jcip.annotations.NotThreadSafe;

import org.outerj.daisy.diff.HtmlCleaner;
import org.outerj.daisy.diff.XslFilter;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

@NotThreadSafe
public class DiffResource extends Resource {

	final Reference base;
	final Reference from;
	final Reference to;

	public DiffResource(final Context context, final Request request, final Response response) {
		super(context, request, response);
		base = request.getResourceRef();
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		final Form f;
		if (Method.POST.equals(request.getMethod())) {
			f = request.getEntityAsForm();
		} else if (Method.GET.equals(request.getMethod())) {
			f = request.getResourceRef().getQueryAsForm();
		} else {
			from = null;
			to = null;
			return; // no form, no variants
		}
		from = constructProperReference(f.getFirstValue("from"));
		to = constructProperReference(f.getFirstValue("to"));
	}

	private Reference constructProperReference(final String input) {
		return new Reference("riap://host" + (input.startsWith("/") ? input : "/" + input)).getTargetRef();
	}

	@Override
	public Representation represent(final Variant variant) {
		try {
			System.out.println("Fetch from: " + from.toString());
			final String fromText = getContext().getClientDispatcher().get(from).getEntity().getText();

			System.out.println("Fetch to: " + to.toString());
			final String toText = getContext().getClientDispatcher().get(to).getEntity().getText();

			final StringWriter writer = new StringWriter();
			final SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

			final TransformerHandler result = tf.newTransformerHandler();
			result.setResult(new StreamResult(writer));

			final XslFilter filter = new XslFilter();

			final ContentHandler postProcess = filter.xsl(result, "org/outerj/daisy/diff/htmlheader.xsl");

			final Locale locale = Locale.getDefault();
			final String prefix = "diff";

			final HtmlCleaner cleaner = new HtmlCleaner();

			final InputSource oldSource = new InputSource(new StringReader(fromText));
			final InputSource newSource = new InputSource(new StringReader(toText));

			final DomTreeBuilder oldHandler = new DomTreeBuilder();
			cleaner.cleanAndParse(oldSource, oldHandler);
			System.out.print(".");
			final TextNodeComparator leftComparator = new TextNodeComparator(oldHandler, locale);

			final DomTreeBuilder newHandler = new DomTreeBuilder();
			cleaner.cleanAndParse(newSource, newHandler);
			System.out.print(".");
			final TextNodeComparator rightComparator = new TextNodeComparator(newHandler, locale);

			postProcess.startDocument();
			postProcess.startElement("", "diffreport", "diffreport", new AttributesImpl());
			postProcess.startElement("", "diff", "diff", new AttributesImpl());
			final HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(postProcess, prefix);

			final HTMLDiffer differ = new HTMLDiffer(output);
			differ.diff(leftComparator, rightComparator);
			System.out.print(".");
			postProcess.endElement("", "diff", "diff");
			postProcess.endElement("", "diffreport", "diffreport");
			postProcess.endDocument();

			return new StringRepresentation(writer.toString(), MediaType.TEXT_HTML);
		} catch (final Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

}
