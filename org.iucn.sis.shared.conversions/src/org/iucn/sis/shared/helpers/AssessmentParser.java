package org.iucn.sis.shared.helpers;

import java.util.ArrayList;
import java.util.HashMap;

import com.solertium.lwxml.java.JavaNativeDocument;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class AssessmentParser {
	/**
	 * HashMap<String(CanonicalName), DataList(Structure data)>
	 */
	private HashMap map;

	/**
	 * HashMap<String(CanonicalName), ArrayList(References)>
	 */
	private HashMap referenceMap;

	private AssessmentData assessment;

	public AssessmentParser() {

	}

	/**
	 * Creates a new parser and invokes parse(...) with the supplied argument.
	 * 
	 * @param ndoc
	 *            a NativeDocument representing an assessment
	 */
	public AssessmentParser(NativeDocument ndoc) {
		parse(ndoc);
	}

	/**
	 * Creates a new parser and invokes parse(...) with the supplied argument.
	 * 
	 * @param ndoc
	 *            a NativeDocument representing an assessment
	 */
	public AssessmentParser(NativeElement nEl) {
		parse(nEl);
	}

	public AssessmentData getAssessment() {
		return assessment;
	}

	public void parse(NativeDocument doc) {
		parse(doc.getDocumentElement());
	}

	public void parse(NativeElement docElement) {
		if (docElement == null || !docElement.getNodeName().equalsIgnoreCase("assessment")) {
			assessment = null;
			return;
		}

		map = new HashMap();
		referenceMap = new HashMap();
		assessment = new AssessmentData();

		parseBasicInformation(docElement);
		parseData(docElement);
	}

	public void parseBasicInformation(NativeElement ndocElement) {
		NativeElement basicInformation = ndocElement.getElementByTagName("basicInformation");

		assessment.setAssessmentID(safeGetElementText(basicInformation, "assessmentID", ""));
		assessment.setSpeciesName(safeGetElementText(basicInformation, "speciesName", ""));
		assessment.setSpeciesID(safeGetElementText(basicInformation, "speciesID", ""));
		assessment.setSource(safeGetElementText(basicInformation, "source", ""));
		assessment.setSourceDate(safeGetElementText(basicInformation, "sourceDate", ""));

		String dateMod = safeGetElementText(basicInformation, "dateModified", "");
		if (dateMod.matches("\\d+"))
			assessment.setDateModified(org.iucn.sis.shared.api.data.LongUtils.safeParseLong(dateMod));

		assessment.setDateAdded(safeGetElementText(basicInformation, "dateAdded", ""));
		assessment.setDateFinalized(safeGetElementText(basicInformation, "dateFinalized", ""));

		assessment.setCritVersion(safeGetElementText(basicInformation, "critVersion", ""));
		assessment.setType(safeGetElementText(basicInformation, "validationStatus", ""));
		if (assessment.getType().equals(""))
			assessment.setType(BaseAssessment.PUBLISHED_ASSESSMENT_STATUS);

		assessment.setCategoryAbbreviation(safeGetElementText(basicInformation, "categoryAbbreviation", ""));
		assessment.setCategoryCriteria(safeGetElementText(basicInformation, "categoryCriteria", ""));
		assessment.setDone(safeGetElementText(basicInformation, "isDone", "true").equalsIgnoreCase("true"));
		assessment
				.setHistorical(safeGetElementText(basicInformation, "isHistorical", "false").equalsIgnoreCase("true"));

		assessment.setCategoryFuzzyResult(safeGetElementText(basicInformation, "categoryFuzzyResult", ""));
		assessment.setManualCategoryAbbreviation(safeGetElementText(basicInformation, "manualCategoryAbbreviation",
				null));
		assessment.setManualCategoryCriteria(safeGetElementText(basicInformation, "manualCategoryCriteria", null));
		
		assessment.setCrCriteria((safeGetElementText(basicInformation, "crCriteria", "")));
		assessment.setEnCriteria((safeGetElementText(basicInformation, "enCriteria", "")));
		assessment.setVuCriteria((safeGetElementText(basicInformation, "vuCriteria", "")));
		
		assessment.setUserLastUpdated(XMLUtils.cleanFromXML((safeGetElementText(basicInformation, "lastUpdatedBy", ""))));
		
	}

	private HashMap<String, Object> parseData(NativeElement docElement) {
		// GET GLOBAL REFERENCES
		try {
			NativeElement globalRefs = docElement.getElementByTagName("globalReferences");
			if (globalRefs != null) {
				final ArrayList refList = new ArrayList();
				NativeNodeList globals = globalRefs.getElementsByTagName("reference");
				parseReferences("Global", refList, globals, null);
				referenceMap.put("Global", refList);
			}
		} catch (Exception ignored) {
		} // No global references

		// GET FIELD'S STRUCTURE DATA
		NativeNodeList fields = docElement.getElementsByTagName("field");
		for (int i = 0; i < fields.getLength(); i++) {
			NativeElement fieldTag = fields.elementAt(i);
			String fieldName = fieldTag.getAttribute("id");

			if (fieldName == null || fieldName.equals(""))
				continue;

			NativeNodeList structures = fieldTag.getElementsByTagName("structure");
			ArrayList data = new ArrayList();

			if (fieldName.equalsIgnoreCase("RedListAssessmentDate")) {
				if (structures.elementAt(0) != null && !structures.elementAt(0).getTextContent().equals("")) {
					assessment.setDateFinalized(structures.elementAt(0).getTextContent());
					data.add(assessment.getDateFinalized());
				} else
					data.add(assessment.getDateFinalized());
			} else
				for (int j = 0; j < structures.getLength(); j++)
					data.add(structures.elementAt(j).getText());

			ArrayList refList = new ArrayList();
			NativeNodeList refs = fieldTag.getElementsByTagName("reference");
			parseReferences(fieldName, refList, refs, null);

			map.put(fieldName, data);
			referenceMap.put(fieldName, refList);
		}

		// GET CLASSIFICATION SCHEME'S DATA
		NativeNodeList schemes = docElement.getElementsByTagName("classificationScheme");
		for (int i = 0; i < schemes.getLength(); i++) {
			NativeElement curScheme = schemes.elementAt(i);
			String name = curScheme.getAttribute("id");

			NativeNodeList selectedList = curScheme.getElementsByTagName("selected");
			HashMap selected = new HashMap();

			for (int j = 0; j < selectedList.getLength(); j++) {
				NativeElement curSelected = selectedList.elementAt(j);
				String id = curSelected.getAttribute("id");

				NativeNodeList structures = curSelected.getElementsByTagName("structure");
				ArrayList data = new ArrayList();

				for (int k = 0; k < structures.getLength(); k++)
					data.add(structures.elementAt(k).getText());

				selected.put(id, data);
				
				ArrayList refList = new ArrayList();
				NativeNodeList refs = curSelected.getElementsByTagName("reference");
				String refSelectedID = name + "." + id;
				parseReferences(refSelectedID, refList, refs, curSelected);
				
				if( refList.size() > 0 ) {
					referenceMap.put(refSelectedID, refList);
				}
			}

			// References
			ArrayList refList = new ArrayList();
			NativeNodeList refs = curScheme.getElementsByTagName("reference");
			parseReferences(name, refList, refs, curScheme);
			
			map.put(name, selected);
			referenceMap.put(name, refList);
		}

		assessment.setData(map);
		assessment.setReferences(referenceMap);

//		//TODO: TAKE THIS OUT AND JUST SCRIPT THESE CHANGES
//		List<String> regionInfo = (List<String>)assessment.getDataMap().get(CanonicalNames.RegionInformation);
//		if( regionInfo.size() == 3 ) {
//			if( regionInfo.get(1).equals("") && 
//					regionInfo.get(0).equals("false") ) {
//				regionInfo.set(1, "-1");
//				regionInfo.set(2, "true");
//			}
//			
//			regionInfo.remove(0);
//		}
		
		return assessment.getDataMap();
	}

	private void parseReferences(String fieldName, ArrayList refList, NativeNodeList refs, NativeElement parentEl) {
		for (int j = 0; j < refs.getLength(); j++) {
			NativeElement cur = refs.elementAt(j);

			if( parentEl != null && !cur.getParent().getNodeName().equals(parentEl.getNodeName()) )
				continue;
			
			String contents = cur.getText();

			if (contents != null && contents.startsWith("<field")) {
				// Wrap it one level deep so it maintains its id attribute
				contents = "<references>\n<reference id=\"" + cur.getAttribute("id") + "\">" + contents
						+ "</reference>\n</references>";

				NativeDocument newRef = new JavaNativeDocument();
				newRef.parse(contents);

				ReferenceUI ref = new ReferenceUI(newRef.getDocumentElement().getElementByTagName("reference"));
				ref.setAssociatedField(fieldName);
				ref.generateCitationIfNotAlreadyGenerate();
				refList.add(ref);
			} else {
				ReferenceUI ref = new ReferenceUI(cur);
				ref.setAssociatedField(fieldName);
				ref.generateCitationIfNotAlreadyGenerate();
				refList.add(ref);
			}
		}
	}

	/**
	 * Will parse out only data from this native document, leaving basic
	 * information alone.
	 * 
	 * @param doc
	 * @return HashMap of data parsed out
	 */
	public HashMap<String, Object> parseDataOnly(NativeDocument doc) {
		map = new HashMap<String, Object>();
		referenceMap = new HashMap<String, Object>();
		assessment = new AssessmentData();

		return parseData(doc.getDocumentElement());
	}

	public HashMap<String, Object> parseDataOnly(NativeElement docElement) {
		map = new HashMap<String, Object>();
		referenceMap = new HashMap<String, Object>();
		assessment = new AssessmentData();

		return parseData(docElement);
	}

	private String safeGetElementText(NativeElement basicInformation, String tagName, String errorReturnValue) {
		try {
			return basicInformation.getElementByTagName(tagName).getText();
		} catch (Exception e) {
			return errorReturnValue;
		}
	}
}
