package org.iucn.sis.server.utils.scripts;

import java.io.File;

import org.iucn.sis.server.crossport.demimport.DEMImportTaxaUpdater;
import org.restlet.Context;
import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

public class DEMRerunner {
	public static class DEMRerunnerResource extends Resource {

		public DEMRerunnerResource() {
		}

		public DEMRerunnerResource(final Context context, final Request request, final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			DEMRerunner.reRunDEMs(getContext().getClientDispatcher());

			StringBuilder sb = new StringBuilder();
			sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
			sb.append("Now it's done!");
			sb.append("</body></html>");

			return new StringRepresentation(sb, MediaType.TEXT_HTML);
		}

	}

	public static void reRunDEMs(Uniform uniform) {
		DEMImportTaxaUpdater gaa = new DEMImportTaxaUpdater(new File("/usr/data/GAA/gaa.mdb"), uniform, "gaa", "rerun");
		new Thread(gaa).run();

		DEMImportTaxaUpdater gma = new DEMImportTaxaUpdater(new File("/usr/data/GMA/gma.mdb"), uniform, "gma", "rerun");
		new Thread(gma).run();

		DEMImportTaxaUpdater groupers = new DEMImportTaxaUpdater(new File("/usr/data/groupers.mdb"), uniform,
				"groupers", "rerun");
		new Thread(groupers).run();
	}
}
