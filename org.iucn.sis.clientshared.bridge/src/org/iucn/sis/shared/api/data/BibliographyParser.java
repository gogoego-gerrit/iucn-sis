package org.iucn.sis.shared.api.data;

import org.iucn.sis.shared.api.utils.XMLUtils;

import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public abstract class BibliographyParser {

	private NativeDocument bibliography = null;
	private NativeElement root;

	public void doParse(NativeDocument ndoc) {
		bibliography = ndoc;
		root = ndoc.getDocumentElement();

		NativeNodeList rows = root.getElementsByTagName("z:row");
		BibliographyData data = new BibliographyData();

		for (int i = 0; i < rows.getLength(); i++) {
			NativeNode currentRow = rows.item(i);
			for (int j = 0; j < currentRow.getChildNodes().getLength(); j++) {
				NativeNode current = currentRow.getChildNodes().item(j);
				String name = current.getNodeName();

				if (name.equalsIgnoreCase(XMLUtils.BIB_CREATED))
					data.setCREATED(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_REF_TYPE))
					data.setREF_TYPE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_AUTHOR))
					data.setAUTHOR(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_TITLE))
					data.setTITLE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_PUB_YEAR))
					data.setPUB_YEAR(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_PUB_DATE))
					data.setPUB_DATE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_PERIODICAL_FULL))
					data.setPERIODICAL_FULL(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_PERIODICAL_ABBREV))
					data.setPERIODICAL_ABBREV(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_VOLUME))
					data.setVOLUME(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_ISSUE))
					data.setISSUE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_START_PAGE))
					data.setSTART_PAGE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_OTHER_PAGES))
					data.setOTHER_PAGES(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_EDITION))
					data.setEDITION(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_PUBLISHER))
					data.setPUBLISHER(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_PLACE_OF_PUBLICATION))
					data.setPLACE_OF_PUBLICATION(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_ISSN_ISBN))
					data.setISSN_ISBN(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_LANGUAGE))
					data.setLANGUAGE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_FOREIGN_TITLE))
					data.setFOREIGN_TITLE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_LINKS))
					data.setLINKS(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_ABSTRACT))
					data.setABSTRACT(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_NOTES))
					data.setNOTES(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_RETRIEVED_DATE))
					data.setRETRIEVED_DATE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_URL))
					data.setURL(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_WEBSITE_TITLE))
					data.setWEBSITE_TITLE(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_WEBSITE_EDITOR))
					data.setWEBSITE_EDITOR(XMLUtils.getXMLValue(current, ""));
				else if (name.equalsIgnoreCase(XMLUtils.BIB_COMMENTS))
					data.setCOMMENTS(XMLUtils.getXMLValue(current, ""));
			}
		}// for

		extractBibliographyData(data);
	}

	protected abstract void extractBibliographyData(BibliographyData data);

}
