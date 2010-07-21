package org.iucn.sis.shared.data;

import java.util.ArrayList;

import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.RegionCache;
import org.iucn.sis.shared.xml.XMLUtils;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class FindResultData {

	public static class OldNewStrings {

		public String oldString;
		public String newString;
		public int index;

		public OldNewStrings(String oldString, String newString, String index) {
			this.oldString = oldString;
			this.newString = newString;
			this.index = Integer.parseInt(index);
			// SysDebugger.getInstance().println("this is index " + index);
		}
	}

	public static class SentenceReplacementData {
		String groupIndex;
		String beginIndex;
		ArrayList replacementString;
		int numberOfIndicies;
		String originalString;
		boolean decided;
		int currentIndex;
		String resultDescription;

		public SentenceReplacementData(String resultDescription, String index, String beginIndex,
				String originalString, ArrayList texts, int numberOfIndicies) {
			this.groupIndex = index;
			this.originalString = originalString;
			this.beginIndex = beginIndex;
			this.resultDescription = resultDescription;
			decided = false;
			currentIndex = 0;
			this.replacementString = texts;
			this.numberOfIndicies = numberOfIndicies;
		}

		public String getBeginIndex() {
			return beginIndex;
		}

		public int getCurrentIndex() {
			return currentIndex;
		}

		public String getCurrentText() {
			String returnString = null;
			if (currentIndex < replacementString.size()) {
				returnString = ((OldNewStrings) replacementString.get(currentIndex)).oldString;
			}
			return returnString;
		}

		public String getIndex() {
			return groupIndex;
		}

		public int getNumberOfIndicies() {
			return numberOfIndicies;
		}

		public String getNumberOfIndiciesString() {
			return numberOfIndicies + "";
		}

		public String getOldWord() {
			try {
				String old = ((OldNewStrings) replacementString.get(currentIndex)).oldString;
				if (old == null) {
					return "";
				} else
					return old;
			} catch (Exception e) {
				return "";
			}
		}

		public int getOldWordIndex() {
			try {
				// SysDebugger.getInstance().println("returning " +
				// ((OldNewStrings)replacementString.get(currentIndex)).index);
				return ((OldNewStrings) replacementString.get(currentIndex)).index;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}

		public String getOriginalString() {
			return originalString;
		}

		public ArrayList getReplacementString() {
			return replacementString;
		}

		public String getReplacementString(int index) {
			String returnString = null;
			try {
				returnString = ((OldNewStrings) replacementString.get(index)).newString;
			} catch (Exception e) {

			}
			return returnString;
		}

		public String getResultDescription() {
			return resultDescription;
		}

		boolean isDecided() {
			return decided;
		}

		boolean needsReplacing() {
			boolean needsRepling = false;
			for (int i = 0; i < numberOfIndicies && !needsRepling; i++) {
				needsRepling = ((OldNewStrings) replacementString.get(i)).newString != null;
			}

			return needsRepling;
		}

		public boolean replaceWith(String replacementString) {
			((OldNewStrings) this.replacementString.get(currentIndex)).newString = XMLUtils.clean(replacementString);
			currentIndex++;
			if (currentIndex >= numberOfIndicies) {
				setDecided(true);
			}

			return decided;
		}

		void setDecided(boolean decided) {
			this.decided = decided;
		}

		public void setIndex(String index) {
			this.groupIndex = index;
		}

		public void setOriginalString(String originalString) {
			this.originalString = originalString;
		}

		boolean skip() {
			((OldNewStrings) this.replacementString.get(currentIndex)).newString = null;
			currentIndex++;
			if (currentIndex >= numberOfIndicies) {
				setDecided(true);
			}
			return decided;
		}

		public void updateBeginIndex(int offset) {
			try {
				beginIndex = (Integer.parseInt(beginIndex) + offset) + "";
			} catch (Exception e) {
			}
		}

	}

	/**
	 * Takes a node with one FindResultData object represented in xml format
	 * 
	 * @param doc
	 */
	public static FindResultData fromXML(NativeElement element) {
		FindResultData returnData = new FindResultData();
		returnData.text = XMLUtils.cleanFromXML(element.getElementByTagName("text").getTextContent());
		returnData.url = XMLUtils.cleanFromXML(element.getElementByTagName("url").getTextContent());
		returnData.name = XMLUtils.cleanFromXML(element.getElementByTagName("name").getTextContent());
		
		while (returnData.name.matches(".*\\$\\{-*\\d+\\}.*")) {
			String id = returnData.name.substring(returnData.name.indexOf("${")+2, returnData.name.indexOf("}"));
			returnData.name = returnData.name.replaceFirst("\\$\\{-*\\d+\\}", RegionCache.impl.getRegionByID(id).getRegionName());
		}

		returnData.sentenceDataList = new ArrayList();
		NativeNodeList list = element.getElementsByTagName("line");
		for (int i = 0; i < list.getLength(); i++) {
			NativeElement lineElement = (NativeElement) list.item(i);
			try {
				NativeNodeList textList = lineElement.getElementByTagName("texts").getElementsByTagName("text");
				ArrayList texts = new ArrayList();
				for (int j = 0; j < textList.getLength(); j++) {
					NativeElement item = (NativeElement) textList.item(j);
					texts.add(new OldNewStrings(item.getTextContent(), null, item.getAttribute("index")));
					// SysDebugger.getInstance().println("adding index " +
					// item.getAttribute("index"));
				}
				// SysDebugger.getInstance().println(
				// "Before originalSentence clean " +
				// lineElement.getElementByTagName("sentence").getText());
				SentenceReplacementData data = new SentenceReplacementData(lineElement.getElementByTagName(
						"description").getText(), lineElement.getElementByTagName("groupIndex").getText(), lineElement
						.getElementByTagName("beginIndex").getText(), XMLUtils.clean(lineElement.getElementByTagName(
						"sentence").getText()), texts, Integer.parseInt(lineElement.getElementByTagName("indexCount")
						.getText()));
				returnData.sentenceDataList.add(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (returnData.url.matches(".*(draft).*"))
			returnData.type = BaseAssessment.DRAFT_ASSESSMENT_STATUS;
		else if (returnData.url.matches(".*(browse/ass).*"))
			returnData.type = BaseAssessment.PUBLISHED_ASSESSMENT_STATUS;
		else
			returnData.type = "taxon";

		return returnData;
	}

	String url;
	String text;
	String type;
	String name;

	boolean isFinished;

	ArrayList sentenceDataList;

	int currentSentence;

	public FindResultData() {
		url = null;
		text = null;
		name = null;
		sentenceDataList = new ArrayList();
		isFinished = false;
		currentSentence = 0;
	}

	public String getCurrentDescription() {
		try {
			return getCurrentSentence().getResultDescription();
		} catch (Exception e) {
			return null;
		}
	}

	public SentenceReplacementData getCurrentSentence() {
		if (currentSentence < sentenceDataList.size())
			return (SentenceReplacementData) sentenceDataList.get(currentSentence);
		else
			return null;
	}

	public int getCurrentSentenceIndex() {
		int index = 0;
		for (int i = 0; i < currentSentence; i++) {
			index = index + getSentenceData(i).getNumberOfIndicies();
		}
		index = index + getCurrentSentence().currentIndex + 1;
		return index;
	}

	public String getName() {
		return name;
	}

	public int getNumberOfResults() {
		int number = 0;
		for (int i = 0; i < sentenceDataList.size(); i++) {
			number = number + getSentenceData(i).numberOfIndicies;
		}
		return number;
	}

	public SentenceReplacementData getSentenceData(int index) {
		return (SentenceReplacementData) sentenceDataList.get(index);
	}

	public int getSizeOfSentenceDataList() {
		return sentenceDataList.size();
	}

	public String getText() {
		return text;
	}

	public String getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public boolean needsReplacing() {
		boolean replace = false;

		for (int i = 0; i < sentenceDataList.size() && !replace; i++) {
			replace = ((SentenceReplacementData) sentenceDataList.get(i)).needsReplacing();
		}

		return replace;
	}

	public void replaceCurrentSentence(String replacementText) {
		boolean update = getCurrentSentence().replaceWith(replacementText);
		if (update)
			updateCurrentIndex();
	}

	public void resetCurrentIndex() {
		currentSentence = 0;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public void skipCurrentSentence() {
		boolean updateIndex = getCurrentSentence().skip();
		if (updateIndex)
			updateCurrentIndex();
	}

	public String toXML() {
		String xml = "<result>\r\n";
		xml += "<text>" + XMLUtils.clean(text) + "</text>\r\n";

		for (int i = 0; i < sentenceDataList.size(); i++) {
			SentenceReplacementData data = (SentenceReplacementData) sentenceDataList.get(i);
			// SysDebugger.getInstance().println(
			// "This is teh data I am looking at " +
			// data.getReplacementString());
			if (data.needsReplacing()) {

				String groupIndex = data.getIndex();
				String beginIndex = data.getBeginIndex();
				String sentence = data.getOriginalString();
				String numberOfIndicies = data.getNumberOfIndiciesString();
				String description = data.getResultDescription();
				xml += "<line>\r\n <groupIndex>" + groupIndex + "</groupIndex>\r\n" + "<beginIndex>" + beginIndex
						+ "</beginIndex>\r\n<sentence>" + XMLUtils.clean(sentence) + "</sentence>\r\n<indexCount>"
						+ numberOfIndicies + "</indexCount>\r\n<description>" + description + "</description>\r\n";
				xml += "<replacement>\r\n";
				for (int j = 0; j < data.getNumberOfIndicies(); j++) {
					if (data.getReplacementString(j) != null) {
						xml += "<index count=\"" + j + "\">" + XMLUtils.clean(data.getReplacementString(j))
								+ "</index>\r\n";
					}
				}
				xml += "</replacement>\r\n";
				xml += "</line>\r\n";
			}
		}
		xml += "<url>" + url + "</url>\r\n";
		xml += "<name>" + name + "</name>\r\n";
		xml += "</result>\r\n";

		return xml;
	}

	public void updateCurrentIndex() {
		currentSentence++;
	}

}
