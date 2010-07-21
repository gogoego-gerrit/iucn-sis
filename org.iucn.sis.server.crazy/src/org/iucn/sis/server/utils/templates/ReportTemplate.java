package org.iucn.sis.server.utils.templates;

import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;

public interface ReportTemplate {

	public Document getDocument();

	public void parse(AssessmentData assessment);

	public void parse(AssessmentData assessment, TaxonNode taxon);

	public void setWriter(DocWriter writer);

}
