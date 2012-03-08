package org.iucn.sis.client.panels.assessments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iucn.sis.client.api.caches.TaxonomyCache;
import org.iucn.sis.client.api.utils.BasicWindow;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.utils.CaseInsensitiveAlphanumericComparator;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.util.gwt.ui.DrawsLazily;

public class AssessmentReferencesPanel extends BasicWindow implements DrawsLazily {
	
	private final Assessment assessment;
	private int totalAssessmentRefs,totalTaxonRefs,totalTaxaNotesRefs;
	
	public AssessmentReferencesPanel(Assessment assessment) {
		super("View References", "icon-book");
		setSize(800, 600);
		setLayout(new FillLayout());
		
		this.assessment = assessment;
		totalAssessmentRefs = 0;
		totalTaxonRefs = 0;
		totalTaxaNotesRefs = 0;
	}
	
	@Override
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				open();
			}
		});
	}
	
	public void open() {
		super.show();
	}

	
	@Override
	public void draw(final DoneDrawingCallback callback) {
		LayoutContainer panel = new LayoutContainer(new AccordionLayout());
		panel.setBorders(false);
		
	    ContentPanel dataPanel = new ContentPanel();  
	    dataPanel.add(new HTML(getAssessmentReferences()));
	    dataPanel.setAnimCollapse(false);  
	    dataPanel.setHeading("Assessment References ("+totalAssessmentRefs+")");  
	    dataPanel.setLayout(new FitLayout()); 
	    dataPanel.setScrollMode(Scroll.AUTOY);
		panel.add(dataPanel);
				
		dataPanel = new ContentPanel();
		dataPanel.add(new HTML(getTaxonReferences()));
		dataPanel.setAnimCollapse(false);  
		dataPanel.setHeading("Taxon References ("+totalTaxonRefs+")");  
		dataPanel.setLayout(new FitLayout());  
		dataPanel.setScrollMode(Scroll.AUTOY);
		panel.add(dataPanel);
	    
		dataPanel = new ContentPanel();  
		dataPanel.add(new HTML(getTaxonomicNotesReferences()));
		dataPanel.setAnimCollapse(false);  
		dataPanel.setHeading("Taxonomic Notes References ("+totalTaxaNotesRefs+")");  
		dataPanel.setLayout(new FitLayout());  
		dataPanel.setScrollMode(Scroll.AUTOY);
		panel.add(dataPanel);
	
		add(panel);
		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		}));
		
		callback.isDrawn();
	}

	public String getAssessmentReferences(){
		
		String referenceStr = "";

		Set<String> references = new HashSet<String>();
		if (assessment.getReference() != null)
			references.addAll(addReferences(assessment.getReference()));
		
		for (Field field : assessment.getField()) {
			if (field.getReference() != null) 
				references.addAll(addReferences(field.getReference()));			
		}
	
		if (references.isEmpty())
			referenceStr = "There are no references for this section.";
		else {
			totalAssessmentRefs = references.size();			
			referenceStr = printReferences(references);
		}
		
		return referenceStr;
	}
	
	public String getTaxonReferences(){
		
		String referenceStr = "";
		Taxon taxon = assessment.getTaxon();
		Set<String> references = new HashSet<String>();
		
		if (taxon.getReference() != null)
			references.addAll(addReferences(taxon.getReference()));
				
		if (references.isEmpty())
			referenceStr = "There are no references for this section.";
		else {
			totalTaxonRefs = references.size();
			referenceStr = printReferences(references);
		}
		
		return referenceStr;
	}	
	
	public String getTaxonomicNotesReferences(){
		
		String referenceStr = "";
		Set<String> references = new HashSet<String>();
	
		if(assessment.isDraft()){
			Field field = TaxonomyCache.impl.getCurrentTaxon().getTaxonomicNotes();
			if(field != null)
				references.addAll(addReferences(field.getReference()));			
		}		
		
		if (references.isEmpty())
			referenceStr = "There are no references for this section.";
		else {
			totalTaxaNotesRefs = references.size();
			referenceStr = printReferences(references);
		}
		
		return referenceStr;
	}	
	
	public String printReferences(Set<String> list){
		
		List<String> values = new ArrayList<String>(list);
		Collections.sort(values, new CaseInsensitiveAlphanumericComparator());
		
		StringBuilder builder = new StringBuilder();
		for (String row : values)
			builder.append(row + "<br/><br/>");
		
		return builder.toString();
	}
	
	public Set<String> addReferences(Set<Reference> references){
		Set<String> list = new HashSet<String>();
		if(references.size() > 0){
			for (Reference reference : references) {
				String citation = reference.generateCitationIfNotAlreadyGenerate();
				if (citation != null && !"".equals(citation))
					list.add(citation);
			}
		}
		return list;
	}	
}
