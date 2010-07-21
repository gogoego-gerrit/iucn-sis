package org.iucn.sis.shared.taxonomyTree;

import java.util.ArrayList;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.shared.data.assessments.Note;
import org.iucn.sis.shared.xml.XMLUtils;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class CommonNameFactory {
	public static CommonNameData buildFromXML(NativeElement commonNameTag) {
		String iso = commonNameTag.getAttribute("iso");
		String language = commonNameTag.getAttribute("language");
		boolean validated = commonNameTag.getAttribute("validated").equalsIgnoreCase("true");
		String name = commonNameTag.getAttribute("name");
		boolean primary = commonNameTag.getAttribute("primary").equalsIgnoreCase("true");
		String reason = commonNameTag.getAttribute("reason");
		String assessment = commonNameTag.getAttribute("assessment");
		String assessmentStatus = commonNameTag.getAttribute("assessmentStatus");
		CommonNameData curName = new CommonNameData(name, language, iso, validated, primary);
		if (!validated)
			curName.setChangeReason(Integer.valueOf(reason).intValue());
		NativeNodeList refs = commonNameTag.getElementsByTagName("reference");

		for (int i = 0; i < refs.getLength(); i++)
			curName.addSource(new org.iucn.sis.client.referenceui.ReferenceUI((NativeElement) refs.item(i)));

		NativeNodeList notes = commonNameTag.getElementsByTagName("note");

		for (int i = 0; i < notes.getLength(); i++) {
			Note note = new Note();
			NativeElement current = (NativeElement) notes.item(i);
			note.setBody(current.getTextContent());
			note.setCanonicalName(current.getAttribute("canonicalName"));
			note.setDate(current.getAttribute("date"));
			note.setId(Long.valueOf(current.getAttribute("id")).longValue());
			note.setUser(current.getAttribute("user"));
			curName.addNote(note);
		}

		curName.setAssessmentAttachedToID(assessment);
		curName.setAssessmentStatus(assessmentStatus);

		return curName;
	}

	public static CommonNameData createCommonName(String name, String language, String isoCode, boolean primary) {
		return createCommonName(name, language, isoCode, false, primary);
	}

	public static CommonNameData createCommonName(String name, String language, String isoCode, boolean validated,
			boolean isPrimary) {
		return new CommonNameData(name, language, isoCode, validated, isPrimary);
	}

	public static String nameToXML(CommonNameData name) {
		String xml = "<commonName iso=\"" + XMLUtils.clean(name.getIsoCode()) + "\" language=\""
				+ XMLUtils.clean(name.getLanguage()) + "\" validated=\"" + name.isValidated() + "\" name=\""
				+ XMLUtils.clean(name.getName()) + "\" primary=\"" + name.isPrimary() + "\" assessment=\""
				+ name.getAssessmentAttachedToID() + "\" assessmentStatus=\"" + name.getAssessmentStatus();
		if (!name.isValidated())
			xml += "\" reason=\"" + name.getChangeReason();
		xml += "\">";
		ArrayList refs = name.getSources();
		if (refs.size() > 0) {
			xml += "<sources>";
			for (int i = 0; i < refs.size(); i++) {
				xml += ((ReferenceUI) refs.get(i)).toXML();
			}
			xml += "</sources>";
		}
		ArrayList notes = name.getNotes();
		if (notes.size() > 0) {
			xml += "<notes>";
			for (int i = 0; i < notes.size(); i++) {
				xml += ((Note) notes.get(i)).toXML();
			}
			xml += "</notes>";
		}
		xml += "</commonName>\r\n";

		return xml;
	}
}
