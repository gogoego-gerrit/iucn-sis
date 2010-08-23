package org.iucn.sis.shared.api.structures;

import org.iucn.sis.shared.api.utils.XMLUtils;


public class StructureSerializer {
	private static String simpleXML(boolean value, String id) {
		return simpleXML(new Boolean(value), id);
	}

	private static String simpleXML(int value, String id) {
		return simpleXML(new Integer(value), id);
	}

	private static String simpleXML(Object value, String id) {
		String xmlRetString = "<structure id=\"" + id + "\">";
		xmlRetString += value;
		xmlRetString += "</structure>\n";
		return xmlRetString;
	}

	public static String toXML(SISAutoComplete structure) {
		return simpleXML(structure.textbox.getText(), structure.getId());
	}

	public static String toXML(SISBoolean structure) {
		return simpleXML(structure.getData(), structure.getId());
	}

	public static String toXML(SISBooleanList structure) {
		return simpleXML(structure.getData(), structure.getId());
	}

	public static String toXML(SISBooleanRange structure) {
		return simpleXML(XMLUtils.clean(structure.getData()), structure.getId());
	}

	public static String toXML(SISButton structure) {
		return simpleXML(structure.getButton().getText(), structure.getId());
	}

	public static String toXML(SISDate structure) {
		// Data is cleaned in the structure
		return simpleXML(XMLUtils.clean(structure.getData()), structure.getId());
	}

	public static String toXML(SISDominantStructureCollection structure) {
		String xmlRetString = "";
		for (int i = 0; i < structure.getStructures().size(); i++) {
			xmlRetString += ((DominantStructure) structure.getStructures().get(i)).toXML();
		}
		return xmlRetString;
	}

	public static String toXML(SISHideableNote structure) {
		return simpleXML(XMLUtils.clean(structure.getTextarea().getText()), structure.getId());
	}

	public static String toXML(SISImage structure) {
		String xmlRetString = "<structure>\n";
		xmlRetString += "<image>\n";
		xmlRetString += "<path>" + structure.getImage().getUrl() + "</path>\n";
		xmlRetString += "</image>\n";
		xmlRetString += "</structure>\n";
		return xmlRetString;
	}

	public static String toXML(SISMap structure) {
		String xmlRetString = "<structure  id=\"" + structure.getId() + "\">\n";
		xmlRetString += "\t<map>\n";
		for (int i = 0; i < structure.getMyMapData().size(); i++) {
			SISMapData current = (SISMapData) structure.getMyMapData().get(i);
			xmlRetString += "\t\t<dataPoint>\n";
			xmlRetString += "\t\t\t<id>" + current.getId() + "</id>\n";
			xmlRetString += "\t\t\t<description>" + current.getDescription() + "</description>\n";
			xmlRetString += "\t\t\t<latitude>" + current.getLatitude() + "</latitude>\n";
			xmlRetString += "\t\t\t<longitude>" + current.getLongitude() + "</longitude>\n";
			xmlRetString += "\t\t</dataPoint>\n";
		}
		xmlRetString += "</map>\n";
		xmlRetString += "</structure>\n";
		return xmlRetString;
	}

	public static String toXML(SISMultiSelect structure) {
		return "<structure>" + structure.getData() + "</structure>\r\n";
	}

	public static String toXML(SISNumber structure) {
		return simpleXML(XMLUtils.clean(structure.getTextbox().getText()), structure.getId());
	}

	public static String toXML(SISOptionsList structure) {
		// Data is cleaned in the structure
		return simpleXML(structure.getData(), structure.getId());
	}

	public static String toXML(SISQualifier structure) {
		return simpleXML(structure.getListbox().getSelectedIndex(), structure.getId());
	}

	public static String toXML(SISRange structure) {
		return simpleXML(XMLUtils.clean(structure.getData()), structure.getId());
	}

	public static String toXML(SISReference structure) {
		return simpleXML(XMLUtils.clean(structure.getData()), structure.getId());
	}

	public static String toXML(SISRegionalExpertStructure structure) {
		return "<structure>" + structure.getData() + "</structure>\r\n";
	}

	public static String toXML(SISRegionInformation structure) {
		return simpleXML(structure.getData(), structure.getId());
	}

	public static String toXML(SISRelatedStructures structure) {
		String xmlRetString = structure.getDominantStructure().toXML();

		xmlRetString += structure.getDependentXML();

		// for (int i = 0; i < structure.getDependantStructures().size(); i++)
		// xmlRetString +=
		// ((Structure)structure.getDependantStructures().get(i)).toXML();

		return xmlRetString;
	}

	public static String toXML(SISRichTextArea structure) {
		return simpleXML(XMLUtils.clean(structure.getData().toString()), structure.getId());
	}

	public static String toXML(SISSelect structure) {
		return "<structure>" + structure.getData() + "</structure>\r\n";
	}

	public static String toXML(SISStructureCollection structure) {
		String xmlRetString = "";
		for (int i = 0; i < structure.getStructures().size(); i++)
			xmlRetString += ((Structure) structure.getStructures().get(i)).toXML();
		return xmlRetString;
	}

	public static String toXML(SISText structure) {
		return simpleXML(XMLUtils.clean(structure.getTextbox().getText()), structure.getId());
	}

	public static String toXML(SISTextArea structure) {
		return simpleXML(XMLUtils.clean(structure.getTextarea().getText()), structure.getId());
	}

	public static String toXML(SISThreatStructure structure) {
		String xmlRetString = "<structure>" + structure.getTiming().getSelectedIndex() + "</structure>\n";
		xmlRetString += "<structure>" + structure.getScope().getSelectedIndex() + "</structure>\n";
		xmlRetString += "<structure>" + structure.getSeverity().getSelectedIndex() + "</structure>\n";
		xmlRetString += "<structure>" + structure.getImpactScore().getText() + "</structure>\n";
//		xmlRetString += "<structure>" + XMLUtils.clean(structure.getThreatNotes().getText()) + "</structure>\n";

		return xmlRetString;
	}

	public static String toXML(SISUpload structure) {
		return simpleXML(structure.getFileUpload().getFilename(), structure.getId());
	}
}
