package org.iucn.sis.server.extensions.reports;

import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Taxon;


public interface ReportTemplate {

	public Document getDocument();

	public void parse(Assessment assessment);

	public void parse(Assessment assessment, Taxon taxon);

	public void setWriter(DocWriter writer);

}
