package org.iucn.sis.shared.api.displays;

import org.iucn.sis.client.api.caches.FieldWidgetCache.CreatesDisplay;
import org.iucn.sis.shared.api.data.DisplayDataProcessor;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.displays.threats.Threats;
import org.iucn.sis.shared.api.views.FieldParser;
import org.iucn.sis.shared.api.views.components.DisplayData;
import org.iucn.sis.shared.api.views.components.FieldData;
import org.iucn.sis.shared.api.views.components.TreeData;

import com.google.gwt.core.client.GWT;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

public class ClientFieldParser extends FieldParser implements CreatesDisplay {

	protected Display doOperate(DisplayData currentDisplayData) {
		if (currentDisplayData.getType().equalsIgnoreCase(DisplayData.FIELD)) {
			FieldData currentFieldData = (FieldData) currentDisplayData;
			
			final FieldDisplay field = new FieldDisplay(currentFieldData);
			try {
				field.addStructure(DisplayDataProcessor.processDisplayStructure(currentDisplayData));
			} catch (Throwable e) {
				e.printStackTrace();
				GWT.log("FieldParser Error", e);
			}

			return field;
		} else if (currentDisplayData.getType().equalsIgnoreCase(DisplayData.TREE)) {
			TreeData currentTreeData = (TreeData) currentDisplayData;

			ClassificationScheme scheme = new ClassificationScheme(currentTreeData);
			return scheme;
		} else
			return null;
	}

	public Display parseField(NativeDocument doc) {
		Display display = null;
		if ("fields".equals(doc.getDocumentElement().getNodeName())) {
			final NativeNodeList nodes = doc.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodes.getLength() && display == null; i++) {
				final NativeNode current = nodes.item(i);
				if (NativeNode.TEXT_NODE != current.getNodeType() && current instanceof NativeElement) {
					display = parseField((NativeElement)current);
				}
			}
			return display;
		}
		else
			display = parseField(doc.getDocumentElement());
		
		return display;
	}

	public Display parseField(NativeElement fieldElement) {
		// Process the display objects
		if (fieldElement.getNodeName().equalsIgnoreCase(ASSESSMENT_FIELD_TAG_NAME)) {
			try {
				return doOperate(processFieldTag(fieldElement));
			} catch (Throwable e) {
				e.printStackTrace();
				Debug.println(
						"Failed to process field " + fieldElement.getElementByTagName("canonicalName").getText());
			}
		} else if (fieldElement.getNodeName().equalsIgnoreCase("threats")) {
			return new Threats(fieldElement);
		} else {
			try {
				return doOperate(processTreeTags(fieldElement));
			} catch (Throwable e) {
				e.printStackTrace();
				Debug.println(
						"Failed to process classification scheme "
								+ fieldElement.getElementByTagName("canonicalName").getText());
			}
		}

		return null;
	}

}
