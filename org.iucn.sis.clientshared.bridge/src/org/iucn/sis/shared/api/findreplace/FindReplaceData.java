package org.iucn.sis.shared.api.findreplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iucn.sis.shared.api.utils.XMLUtils;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class FindReplaceData {

	public static class OldNewStrings {

		public static final String ROOT_TAG = "oldNew";
		
		public String oldString;
		public String newString;
		public int index;

		protected OldNewStrings() {}
		
		public OldNewStrings(String oldString, String newString, int index) {
			this.oldString = oldString;
			this.newString = newString;
			this.index = index;
		}
		
		public String toXML() {
			StringBuilder xml = new StringBuilder();
			xml.append("<" + ROOT_TAG + ">");
			xml.append("<old><![CDATA[" + oldString + "]]></old>");
			if (newString != null)
				xml.append("<new><![CDATA[" + newString + "]]></new>");
			xml.append("<index>" + index + "</index>");
			xml.append("</" + ROOT_TAG + ">");
			return xml.toString();
		}
		
		public static OldNewStrings fromXML(NativeElement element) {
			OldNewStrings ons = new OldNewStrings();
			ons.oldString = element.getElementsByTagName("old").elementAt(0).getTextContent();
			NativeNodeList news = element.getElementsByTagName("new");
			if (news.getLength() > 0)
				ons.newString = news.elementAt(0).getTextContent();
			ons.index = Integer.parseInt(element.getElementsByTagName("index").elementAt(0).getTextContent());
			return ons;
		}
		
	}

	public static class FieldReplacementInfo {
		
		public static final String ROOT_TAG = "foundInfo";
		public List<OldNewStrings> oldToNewStrings;
		public Integer fieldID;
		public String value;
		public String fieldName;
		protected boolean decided;
		protected boolean replace;
		int currentIndex;
		
		
		protected FieldReplacementInfo(){
			oldToNewStrings = new ArrayList<OldNewStrings>();
			decided = false;
			currentIndex = 0;
		}
		
		public FieldReplacementInfo(Integer fieldID, String value, String fieldName, List<OldNewStrings> list) {
			this.oldToNewStrings = list;
			this.fieldID = fieldID;
			this.value = value;
			this.fieldName = fieldName;
		}
		
		public String toXML() {
			StringBuilder xml = new StringBuilder();
			xml.append("<" + ROOT_TAG + " fieldID=\"" + fieldID + "\" fieldName=\"" + fieldName + "\">");
			xml.append("<value><![CDATA[" + value + "]]></value>");
			for (OldNewStrings ons : oldToNewStrings)
				xml.append(ons.toXML());
			xml.append("</" + ROOT_TAG + ">" );
			return xml.toString();
		}
		
		public static FieldReplacementInfo fromXML(NativeElement element) {
			FieldReplacementInfo data = new FieldReplacementInfo();
			data.fieldID = Integer.valueOf(element.getAttribute("fieldID"));
			data.fieldName = (element.getAttribute("fieldName"));
			data.value = element.getElementsByTagName("value").elementAt(0).getTextContent();
			NativeNodeList list = element.getElementsByTagName(OldNewStrings.ROOT_TAG);
			for (int i = 0; i < list.getLength(); i++)
				data.oldToNewStrings.add(OldNewStrings.fromXML(list.elementAt(i)));				
			return data;
		}
		
		public Map<Integer, OldNewStrings> getMap() {
			HashMap<Integer, OldNewStrings> map = new HashMap<Integer, OldNewStrings>();
			for (OldNewStrings ons : oldToNewStrings)
				map.put(ons.index, ons);
			return map;
		}

		public boolean skip() {
			decided = true;
			replace = replace || false;
			return decided;
		}

		public boolean needsReplacing() {
			return replace;
		}

		public boolean replaceWith(String replacementString) {
			this.oldToNewStrings.get(currentIndex).newString = XMLUtils.clean(replacementString);
			currentIndex++;
			replace = true;
			decided =  (currentIndex >= oldToNewStrings.size());
			return decided;	
		}

		public String getCurrentText() {
			String returnString = null;
			if (currentIndex < oldToNewStrings.size()) {
				returnString = ((OldNewStrings) oldToNewStrings.get(currentIndex)).oldString;
			}
			return returnString;
		}

		public int getOldWordIndex() {
			try {
				return ((OldNewStrings) oldToNewStrings.get(currentIndex)).index;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}

		

	}
	
	public String toXML() {
		String xml = "<result>\r\n";
		xml += "<text><![CDATA[" + text + "]]></text>\r\n";

		for (int i = 0; i < sentenceDataList.size(); i++) {
			FieldReplacementInfo data = (FieldReplacementInfo) sentenceDataList.get(i);
			xml += data.toXML();			
		}
		
		xml += "<assessmentID>" + assessmentID + "</assessmentID>\r\n";
		xml += "<assessmentName>" + assessmentName + "</assessmentName>\r\n";
		xml += "</result>\r\n";

		return xml;
	}

	/**
	 * Takes a node with one FindResultData object represented in xml format
	 * 
	 * @param doc
	 */
	public static FindReplaceData fromXML(NativeElement element) {
		FindReplaceData returnData = new FindReplaceData();
		returnData.text = element.getElementsByTagName("text").item(0).getTextContent();
		returnData.assessmentID = Integer.valueOf(element.getElementsByTagName("assessmentID").item(0).getTextContent());
		returnData.assessmentName = element.getElementsByTagName("assessmentName").item(0).getTextContent();

		returnData.sentenceDataList = new ArrayList();
		NativeNodeList list = element.getElementsByTagName(FieldReplacementInfo.ROOT_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			returnData.sentenceDataList.add(FieldReplacementInfo.fromXML(list.elementAt(i)));			
		}

		return returnData;
	}

	Integer assessmentID;
	String assessmentName;
	String text;
	boolean isFinished;
	int currentSentence;
	ArrayList<FieldReplacementInfo> sentenceDataList;
	String assessmentType;
	
	
	protected FindReplaceData() {
		sentenceDataList = new ArrayList<FieldReplacementInfo>();
		currentSentence = 0;
		isFinished = false;
	}

	public FindReplaceData(Integer assessmentID, String assessmentName, String assessmentType, String searchText) {
		this.assessmentID = assessmentID;
		this.assessmentType = assessmentType;
		this.text = searchText;
		this.assessmentName = assessmentName;
		sentenceDataList = new ArrayList<FieldReplacementInfo>();
		isFinished = false;
	}

	public void addNewFinding(String value, int fieldID,  String fieldName, List<OldNewStrings> foundInfo) {
		sentenceDataList.add(new FieldReplacementInfo(fieldID, value, fieldName, foundInfo));
	}

	public FieldReplacementInfo getSentenceData(int index) {
		return (FieldReplacementInfo) sentenceDataList.get(index);
	}

	public ArrayList<FieldReplacementInfo> getSentenceDataList() {
		return sentenceDataList;
	}

	public String getText() {
		return text;
	}

	public Integer getAssessmentID() {
		return assessmentID;
	}
	
	public String getAssessmentName() {
		return assessmentName;
	}
	
	public String getAssessmentType() {
		return assessmentType;
	}

	public void skipCurrentSentence() {
		boolean updateIndex = getCurrentSentence().skip();
		if (updateIndex)
			updateCurrentIndex();
		
	}

	private void updateCurrentIndex() {
		currentSentence++;
	}

	public FieldReplacementInfo getCurrentSentence() {
		if (currentSentence < sentenceDataList.size())
			return sentenceDataList.get(currentSentence);
		else
			return null;
	}

	public boolean needsReplacing() {
		for (FieldReplacementInfo fields : sentenceDataList) {
			if (fields.needsReplacing())
				return true;
		}
		return false;
		
	}

	public void replaceCurrentSentence(String replacementString) {
		boolean update = getCurrentSentence().replaceWith(replacementString);
		if (update)
			updateCurrentIndex();
		
	}
	
	public int getNumberOfResults() {
		int number = 0;
		for (int i = 0; i < sentenceDataList.size(); i++) {
			number = number + getSentenceData(i).oldToNewStrings.size();
		}
		return number;
	}
	
	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public String getCurrentDescription() {
		return getCurrentSentence().fieldName;
	}

	public int getCurrentSentenceIndex() {
		int index = 0;
		for (int i = 0; i < currentSentence; i++) {
			index = index + getSentenceData(i).oldToNewStrings.size();
		}
		index = index + getCurrentSentence().currentIndex + 1;
		return index;
	}


	

}
