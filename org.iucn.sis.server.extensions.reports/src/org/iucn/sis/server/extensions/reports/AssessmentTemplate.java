//package org.iucn.sis.server.extensions.reports;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.Graphics2D;
//import java.awt.GraphicsEnvironment;
//import java.awt.Image;
//import java.awt.Rectangle;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map.Entry;
//
//import org.dom4j.DocumentException;
//import org.iucn.sis.server.ServerApplication;
//import org.iucn.sis.server.utils.DocumentUtils;
//import org.iucn.sis.server.utils.FilenameStriper;
//import org.iucn.sis.shared.api.data.DisplayDataProcessor;
//import org.iucn.sis.shared.api.models.Assessment;
//import org.iucn.sis.shared.api.models.Taxon;
//import org.iucn.sis.shared.api.models.TaxonFactory;
//import org.iucn.sis.shared.api.utils.FieldParser;
//import org.w3c.dom.Element;
//
//import com.solertium.lwxml.shared.NativeDocument;
//import com.solertium.util.AlphanumericComparator;
//import com.solertium.vfs.VFS;
//
//public class AssessmentTemplate implements ReportTemplate {
//
//	static class CheckedCellEvent implements PdfPCellEvent {
//		public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
//			PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
//			cb.rectangle(Rectangle.LEFT, Rectangle.BOTTOM, 5, 5);
//			cb.moveTo(Rectangle.LEFT, Rectangle.BOTTOM);
//			cb.lineTo(Rectangle.LEFT + 5, Rectangle.BOTTOM + 5);
//			cb.moveTo(Rectangle.LEFT + 5, Rectangle.BOTTOM);
//			cb.lineTo(Rectangle.LEFT, Rectangle.BOTTOM + 5);
//			cb.stroke();
//		}
//	}
//
//	static class UncheckedCellEvent implements PdfPCellEvent {
//		public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
//			PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
//			cb.rectangle(Rectangle.LEFT, Rectangle.BOTTOM, 5, 5);
//			cb.stroke();
//		}
//	}
//
//	Taxon taxon;
//
//	Document document;
//	PdfWriter writer;
//
//	PdfPTable headingTable;
//
//	PdfPTable taxonomyInfoTable;
//	PdfPTable hierarchyInnerTable;
//	PdfPTable commonNameInnerTable;
//	PdfPTable synonymInnerTable;
//
//	PdfPTable fieldTable;
//	PdfPTable classSchemesTable;
//
//	PdfPTable assessmentInfoTable;
//	PdfPTable catAndCritInnerTable;
//	PdfPTable summaryAssInfoInnerTable;
//
//	Font timesBoldSmall;
//	Font timesSmall;
//
//	Font timesBoldMedium;
//	Font timesMedium;
//
//	Font timesBoldLarge;
//	Font timesLarge;
//
//	Font timesBoldItalicsSmall;
//
//	Image iucnLogo;
//
//	public AssessmentTemplate() {
//
//	}
//
//	private void addReasonCell(PdfPTable table, org.w3c.dom.Document doc, String field) {
//
//		PdfPCell cell = new PdfPCell();
//
//		cell.setBorder(0);
//		cell.addElement(new Phrase("   " + field, timesSmall));
//
//		if (doc.getDocumentElement().getElementsByTagName(field).getLength() > 0)
//			if (((Element) doc.getDocumentElement().getElementsByTagName(field).item(0)).getTextContent()
//					.equals("true")) {
//				cell.setCellEvent(new CheckedCellEvent());
//
//			} else {
//				cell.setCellEvent(new UncheckedCellEvent());
//
//			}
//		else {
//			cell.setCellEvent(new UncheckedCellEvent());
//
//		}
//
//		table.addCell(cell);
//
//	}
//
//	private void buildAssessmentInfo(Assessment assessment, VFS vfs) {
//		fieldTable = new PdfPTable(1);
//		fieldTable.setWidthPercentage(100);
//
//		classSchemesTable = new PdfPTable(1);
//		classSchemesTable.setWidthPercentage(100);
//
//		PdfPCell cell;
//
//		Object[] entries = assessment.getDataMap().entrySet().toArray();
//		Arrays.sort(entries, new Comparator() {
//
//			private AlphanumericComparator comparator = new AlphanumericComparator();
//
//			public int compare(Object lhs, Object rhs) {
//				Entry le = (Entry) lhs;
//				Entry re = (Entry) rhs;
//
//				return comparator.compare((String) le.getKey(), (String) re.getKey());
//			}
//		});
//
//		for (int i = 0; i < entries.length; i++) {
//			Entry curEntry = (Entry) entries[i];
//			String canonicalName = curEntry.getKey().toString();
//
//			DisplayData currentDisplayData = fetchDisplayData(canonicalName, vfs);
//			try {
//				Structure defaultStruct = null;
//
//				if (currentDisplayData instanceof FieldData)
//					defaultStruct = DisplayDataProcessor.processDisplayStructure(currentDisplayData);
//
//				cell = new PdfPCell();
//				cell.setBorder(0);
//
//				fieldTable.getDefaultCell().setBorder(0);
//				fieldTable.completeRow();
//
//				if (curEntry.getValue() instanceof ArrayList) {
//					cell = parseField((ArrayList<String>) curEntry.getValue(), canonicalName, 8, defaultStruct, false);
//					if (cell != null)
//						fieldTable.addCell(cell);
//				} else {
//					cell = parseClassificationScheme((HashMap<String, ArrayList<String>>) curEntry.getValue(),
//							canonicalName, vfs, currentDisplayData);
//					if (cell != null)
//						classSchemesTable.addCell(cell);
//				}
//
//			} catch (Exception e) {
//				System.out.println("DIED TRYING TO BUILD " + canonicalName);
//				e.printStackTrace();
//			}
//
//		}
//	}
//
//	private void buildCommonNames(Taxon taxon) {
//		// PdfPCell header = new PdfPCell(new Phrase("Common Names",
//		// timesBoldMedium));
//		// header.setColspan( 2 );
//		// header.setBorder(0);
//		// commonNameInnerTable.addCell(header);
//
//		if (taxon.getCommonNames().isEmpty()) {
//			PdfPCell none = new PdfPCell(new Phrase("No Common Names", FontFactory.getFont(FontFactory.TIMES, 6)));
//			none.setColspan(2);
//			none.setBorder(0);
//			commonNameInnerTable.addCell(none);
//		} else {
//			HashMap<String, String> dupes = new HashMap<String, String>();
//
//			for (int i = 0; i < taxon.getCommonNames().size(); i++) {
//				CommonNameData curCN = taxon.getCommonNames().get(i);
//
//				if (!dupes.containsKey(curCN.getName()))
//					dupes.put(curCN.getName(), curCN.getLanguage());
//				else if (dupes.get(curCN.getName()) == null || dupes.get(curCN.getName()).equals(""))
//					if (curCN.getLanguage() != null)
//						dupes.put(curCN.getName(), curCN.getLanguage());
//			}
//
//			for (Entry<String, String> curEntry : dupes.entrySet()) {
//				commonNameInnerTable.addCell(new Phrase(curEntry.getKey(), timesSmall));
//				commonNameInnerTable.addCell(new Phrase(curEntry.getValue(), timesSmall));
//			}
//		}
//	}
//
//	private void buildHeadingTable() {
//		headingTable = new PdfPTable(2);
//		headingTable.getDefaultCell().setBorder(0);
//		headingTable.setWidthPercentage(100);
//		headingTable.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
//
//		PdfPCell cell;
//
//		PdfPTable nameAuthority = new PdfPTable(1);
//		nameAuthority.getDefaultCell().setBorder(0);
//		nameAuthority.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
//
//		cell = wrapInBorderlessCell(new Phrase(taxon.getFullName(), timesBoldLarge));
//		cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
//		cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_TOP);
//		nameAuthority.addCell(cell);
//
//		cell = wrapInBorderlessCell(new Phrase(taxon.getTaxonomicAuthority(), timesBoldMedium));
//		cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
//		cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_TOP);
//		nameAuthority.addCell(cell);
//
//		if (iucnLogo != null) {
//			cell = new PdfPCell(iucnLogo);
//			cell.setBorder(0);
//			cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_LEFT);
//			cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_TOP);
//			headingTable.addCell(cell);
//		}
//
//		cell = new PdfPCell(nameAuthority);
//		cell.setBorder(0);
//		cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
//		cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_TOP);
//		headingTable.addCell(cell);
//	}
//
//	private void buildHierarchyTable(Taxon taxon) {
//		// PdfPCell header = new PdfPCell(new Phrase("Taxonomic Hierarchy",
//		// timesBoldMedium));
//		// header.setColspan( 2 );
//		// header.setBorder(0);
//		// hierarchyInnerTable.addCell(header);
//
//		for (int i = 0; i < taxon.getFootprint().length; i++) {
//			hierarchyInnerTable.addCell(new Phrase(Taxon.getDisplayableLevel(i), timesSmall));
//			hierarchyInnerTable.addCell(new Phrase(taxon.getFootprint()[i], timesSmall));
//		}
//
//		hierarchyInnerTable.addCell(new Phrase(Taxon.getDisplayableLevel(taxon.getLevel()), timesSmall));
//		hierarchyInnerTable.addCell(new Phrase(taxon.getName(), timesSmall));
//	}
//
//	private HashMap buildMap(TreeData treeData) {
//		HashMap codeToDesc = new HashMap();
//		// buildMaps(codeToDesc, curRow, parentDesc);
//		for (Iterator iter = treeData.getTreeRoots().listIterator(); iter.hasNext();)
//			codeToDesc = buildMaps(codeToDesc, (TreeDataRow) iter.next(), "");
//		return codeToDesc;
//	}
//
//	private HashMap buildMaps(HashMap codeToDesc, TreeDataRow curRow, String parentDesc) {
//		String code = curRow.getDisplayId();
//		String levelID = curRow.getRowNumber();
//		String description = curRow.getDescription();
//		boolean codeable = curRow.getCodeable().equalsIgnoreCase("true");
//
//		// String selectedDescription = "";
//		//
//		// if( levelID.charAt(0) >= '0' && levelID.charAt(0) <= '9' )
//		// selectedDescription += levelID + ". ";
//		//
//		// selectedDescription += parentDesc + description;
//
//		String displayableDesc = (levelID.equals("0") ? "" : levelID) + " " + description;
//
//		codeToDesc.put(code, displayableDesc);
//		// codeToLevelID.put(code, levelID);
//		// codeToSelectedDesc.put(code, selectedDescription);
//		// codeToCodeable.put(code, new Boolean(codeable));
//
//		/*
//		 * if( levelID.charAt(0) >= '0' && levelID.charAt(0) <= '9' )
//		 * sortOnMe.put(code, levelID); else sortOnMe.put(code, description);
//		 */
//		for (Iterator iter = curRow.getChildren().listIterator(); iter.hasNext();)
//			buildMaps(codeToDesc, (TreeDataRow) iter.next(), levelID.equals("0") ? "" : parentDesc + description
//					+ " -> ");
//
//		return codeToDesc;
//	}
//
//	private void buildSynonyms(Taxon taxon) {
//		// PdfPCell header = new PdfPCell(new Phrase("Synonyms",
//		// timesBoldMedium));
//		// header.setColspan( 2 );
//		// header.setBorder(0);
//		// synonymInnerTable.addCell(header);
//
//		if (taxon.getSynonyms().isEmpty()) {
//			PdfPCell none = new PdfPCell(new Phrase("No Synonyms", FontFactory.getFont(FontFactory.TIMES, 6)));
//			none.setBorder(0);
//			none.setColspan(2);
//			synonymInnerTable.addCell(none);
//		} else {
//			HashMap<String, String> dupes = new HashMap<String, String>();
//
//			for (int i = 0; i < taxon.getSynonyms().size(); i++) {
//				SynonymData curSyn = taxon.getSynonyms().get(i);
//
//				if (!dupes.containsKey(curSyn.getName()))
//					dupes.put(curSyn.getName(), curSyn.getAuthorityString());
//				else if (dupes.get(curSyn.getName()) == null || dupes.get(curSyn.getName()).equals(""))
//					if (curSyn.getAuthorityString() != null)
//						dupes.put(curSyn.getName(), curSyn.getAuthorityString());
//			}
//
//			for (Entry<String, String> curEntry : dupes.entrySet()) {
//				synonymInnerTable.addCell(new Phrase(curEntry.getKey(), timesSmall));
//				synonymInnerTable.addCell(new Phrase(curEntry.getValue(), timesSmall));
//			}
//		}
//	}
//
//	private void buildTaxonomyHeaderInformation() throws DocumentException {
//		PdfPCell header = wrapInBorderlessCell(new Phrase("Taxonomy Information", timesBoldMedium));
//		header.setColspan(3);
//
//		taxonomyInfoTable = new PdfPTable(3);
//		taxonomyInfoTable.getDefaultCell().setBorder(0);
//		taxonomyInfoTable.setSplitLate(false);
//		taxonomyInfoTable.addCell(header);
//
//		taxonomyInfoTable.addCell(wrapInBorderlessCell(new Phrase("Hierarchy Information", timesBoldSmall)));
//		taxonomyInfoTable.addCell(wrapInBorderlessCell(new Phrase("Common Names", timesBoldSmall)));
//		taxonomyInfoTable.addCell(wrapInBorderlessCell(new Phrase("Synonyms", timesBoldSmall)));
//		taxonomyInfoTable.setHeaderRows(2);
//		taxonomyInfoTable.setSpacingAfter(10);
//
//		hierarchyInnerTable = new PdfPTable(2);
//		hierarchyInnerTable.getDefaultCell().setBorder(0);
//		hierarchyInnerTable.setSplitLate(false);
//
//		commonNameInnerTable = new PdfPTable(2);
//		commonNameInnerTable.getDefaultCell().setBorder(0);
//		commonNameInnerTable.setSplitLate(false);
//
//		synonymInnerTable = new PdfPTable(2);
//		synonymInnerTable.getDefaultCell().setBorder(0);
//		synonymInnerTable.setSplitLate(false);
//
//		buildHierarchyTable(taxon);
//		buildCommonNames(taxon);
//		buildSynonyms(taxon);
//
//		taxonomyInfoTable.setTotalWidth(document.right() - 20);
//		headingTable.setTotalWidth(document.right() - 20);
//
//		taxonomyInfoTable.addCell(wrapInBorderlessCell(hierarchyInnerTable));
//		taxonomyInfoTable.addCell(wrapInBorderlessCell(commonNameInnerTable));
//		taxonomyInfoTable.addCell(wrapInBorderlessCell(synonymInnerTable));
//		taxonomyInfoTable.setWidthPercentage(90);
//
//		headingTable.calculateHeightsFast();
//		taxonomyInfoTable.calculateHeightsFast();
//
//		// System.out.println("Heading table total height: " +
//		// headingTable.getTotalHeight());
//		// PdfContentByte cb = writer.getDirectContentUnder();
//		// cb.setGrayFill(.8f);
//		// cb.roundRectangle(document.left()-5,
//		// document.top()-taxonomyInfoTable.getTotalHeight()-headingTable.
//		// getTotalHeight(),
//		// document.right()-20, taxonomyInfoTable.getTotalHeight(), 15);
//		// cb.fillStroke();
//		// cb.beginText();
//	}
//
//	protected DisplayData fetchDisplayData(String canonicalName, VFS vfs) {
//		NativeDocument jnd = ServerApplication.newNativeDocument(null);
//		jnd.fromXML(DocumentUtils.serializeNodeToString(DocumentUtils.getVFSFileAsDocument("/browse/docs/fields/"
//				+ canonicalName + ".xml", vfs)));
//
//		FieldParser parser = new FieldParser();
//		DisplayData currentDisplayData = parser.parseFieldData(jnd);
//		return currentDisplayData;
//	}
//
//	private PdfPCell formatHeaderCell(Phrase phrase) {
//		PdfPCell cell = new PdfPCell();
//		cell.setBorder(0);
//		cell.setPadding(0);
//		cell.setIndent(0);
//		cell.setPaddingTop(6);
//		cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_TOP);
//		cell.addElement(phrase);
//		return cell;
//
//	}
//
//	public Document getDocument() {
//		document = new Document();
//		return document;
//	}
//
//	public void parse(Assessment assessment) {
//		this.parse(assessment, null);
//	}
//
//	// private void buildRedListAssessment(org.w3c.dom.Document doc) throws
//	// DocumentException{
//	// Paragraph p4 = new Paragraph(new Chunk("IUCN Red Listing\n",
//	// FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
//	//		
//	// PdfPTable redTable = new PdfPTable(2);
//	// redTable.setWidthPercentage(100);
//	// PdfPCell cell = new PdfPCell();
//	// cell.setBorder(0);
//	// Phrase phrase = new Phrase( "Red List Assessment:" ,timesBoldSmall);
//	// if(doc.getDocumentElement().getElementsByTagName("source").getLength()>0)
//	// phrase.add(new
//	// Chunk(((Element)doc.getDocumentElement().getElementsByTagName("source").
//	// item(0)).getTextContent()+"\n",
//	// timesSmall));
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	// //p4.add(phrase);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase( "Red List Criteria: " ,timesBoldSmall);
//	// if(doc.getDocumentElement().getElementsByTagName("categoryCriteria").
//	// getLength()>0)
//	// phrase.add(new
//	// Chunk(((Element)doc.getDocumentElement().getElementsByTagName(
//	// "categoryCriteria").item(0)).getTextContent()+"\n",
//	// timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase(
//	// "Date Last Seen (only for EX, EW or Possibly EX species): "
//	// ,timesBoldSmall);
//	// if(doc.getDocumentElement().getElementsByTagName("lastSeen").getLength()>0
//	// )
//	// phrase.add(new
//	// Chunk(((Element)doc.getDocumentElement().getElementsByTagName("lastSeen").
//	// item(0)).getTextContent()+"\n",
//	// timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase( "Is the species Possibly Extinct? "
//	// ,timesBoldSmall);
//	// if(doc.getDocumentElement().getElementsByTagName("possiblyExtinct").
//	// getLength()>0)
//	// phrase.add(new
//	// Chunk(((Element)doc.getDocumentElement().getElementsByTagName(
//	// "possiblyExtinct").item(0)).getTextContent()+"    ",
//	// timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	//
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase( "Possibly Extinct Candidate? " ,timesBoldSmall);
//	// if(doc.getDocumentElement().getElementsByTagName("possiblyExtinctCandidate"
//	// ).getLength()>0)
//	// phrase.add(new
//	// Chunk(((Element)doc.getDocumentElement().getElementsByTagName(
//	// "possiblyExtinctCandidate").item(0)).getTextContent()+"\n",
//	// timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	//	
//	//		  
//	// // Rationale for the Red List Assessment
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase(
//	// "Reason(s) for Change in Red List Category from the Previous Assessment: "
//	// ,timesBoldSmall);
//	// if(doc.getDocumentElement().getElementsByTagName("changeReason").getLength
//	// ()>0)
//	// phrase.add(new
//	// Chunk(((Element)doc.getDocumentElement().getElementsByTagName(
//	// "changeReason").item(0)).getTextContent()+"\n",
//	// timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// PdfPTable table=new PdfPTable(3);
//	//		
//	// addReasonCell(table, doc, "genuineChange");
//	// addReasonCell(table, doc, "genuineSinceFirst");
//	// addReasonCell(table, doc, "genuineRecent");
//	// addReasonCell(table, doc, "nonGenuineChange");
//	// addReasonCell(table, doc, "criteriaRevision");
//	// addReasonCell(table, doc, "knowledgeNew");
//	// addReasonCell(table, doc, "knowledgeCorrection");
//	// addReasonCell(table, doc, "knowledgeCriteria");
//	// addReasonCell(table, doc, "taxonomy");
//	// addReasonCell(table, doc, "other");
//	// addReasonCell(table, doc, "noChange");
//	// addReasonCell(table, doc, "same");
//	// addReasonCell(table, doc, "criteriaChange");
//	//		
//	// cell.setColspan(2);
//	// cell.addElement(table);
//	// //p4.add(table);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// doc.getDocumentElement().getElementsByTagName("currentPopulationTrend");
//	//		
//	//		
//	// doc.getDocumentElement().getElementsByTagName("assessors");
//	// doc.getDocumentElement().getElementsByTagName("evaluators");
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase( "% population decline in the past: "
//	// ,timesBoldSmall);
//	// phrase.add(new Chunk("", timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase(
//	// "Time period over which the past decline has been measured for: "
//	// ,timesBoldSmall);
//	// phrase.add(new Chunk("", timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase(
//	// "applying Criterion A or C1 (in years or generations): "
//	// ,timesBoldSmall);
//	// phrase.add(new Chunk("", timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase( "% population decline in the future: "
//	// ,timesBoldSmall);
//	// phrase.add(new Chunk("", timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase(
//	// "Time period over which the future decline has been measured for: "
//	// ,timesBoldSmall);
//	// phrase.add(new Chunk("", timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase(
//	// " applying Criterion A or C1 (in years or generations): "
//	// ,timesBoldSmall);
//	// phrase.add(new Chunk("", timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase( "Number of Locations: " ,timesBoldSmall);
//	// phrase.add(new Chunk("", timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	//		
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// phrase = new Phrase( "Number of Mature Individuals: " ,timesBoldSmall);
//	// phrase.add(new Chunk("", timesSmall));
//	// //p4.add(phrase);
//	// cell.addElement(phrase);
//	// redTable.addCell(cell);
//	// cell = new PdfPCell();
//	// cell.setBorder(0);
//	// redTable.addCell(cell);
//	//		
//	// p4.add(redTable);
//	// document.add(p4);
//	//		
//	// }
//
//	public void parse(Assessment assessment, Taxon node) {
//		document.open();
//
//		timesBoldSmall = FontFactory.getFont(FontFactory.TIMES_BOLD, 8);
//		timesSmall = FontFactory.getFont(FontFactory.TIMES, 8);
//		timesBoldItalicsSmall = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 8);
//
//		timesBoldMedium = FontFactory.getFont(FontFactory.TIMES_BOLD, 10);
//		timesMedium = FontFactory.getFont(FontFactory.TIMES, 10);
//
//		timesBoldLarge = FontFactory.getFont(FontFactory.TIMES_BOLD, 18);
//		timesLarge = FontFactory.getFont(FontFactory.TIMES, 18);
//
//		VFS vfs = null;
//		try {
//			vfs = ServerApplication.getStaticVFS();
//
//			byte[] b = new byte[(int) vfs.getLength("/images/logo-iucn.gif")];
//			int readValue = vfs.getInputStream("/images/logo-iucn.gif").read(b);
//			iucnLogo = Image.getInstance(b);
//			iucnLogo.scalePercent(50);
//
//			if (!(readValue > 0))
//				System.out.println("No Image Read.");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (BadElementException e) {
//			e.printStackTrace();
//		} catch (NullPointerException e) {
//			System.out.println("You're not going to have a logo ... sorry!");
//		}
//
//		try {
//			if (node == null) {
//				String specieID = assessment.getSpeciesID();
//
//				String sInfo = DocumentUtils.getVFSFileAsString("/browse/nodes/"
//						+ FilenameStriper.getIDAsStripedPath(specieID) + ".xml", vfs);
//				NativeDocument taxaDoc = ServerApplication.newNativeDocument(null);
//				taxaDoc.parse(sInfo);
//
//				taxon = TaxonFactory.createNode(taxaDoc);
//			} else
//				taxon = node;
//
//			// Add IUCN logo and species Name/Authority
//			buildHeadingTable();
//			document.add(headingTable);
//
//			// Add taxonomy info
//			buildTaxonomyHeaderInformation();
//			document.add(taxonomyInfoTable);
//
//			// Add assessment info
//			buildAssessmentInfo(assessment, vfs);
//			document.add(fieldTable);
//			document.add(classSchemesTable);
//
//			// Paragraph p3 = new Paragraph(new Chunk(
//			// "Species Utilisation",
//			// FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
//			// document.add(p3);
//			// buildRedListAssessment(assessment);
//			/*
//			 * Rationale for the Red List Assessment Bibliography
//			 */
//
//			document.close();
//		} catch (DocumentException e) {
//			System.out.println("Error building the document.");
//			e.printStackTrace();
//		}
//	}
//
//	private PdfPCell parseClassificationScheme(HashMap<String, ArrayList<String>> selected, String canonicalName,
//			VFS vfs, DisplayData currentDisplayData) {
//		if (selected.keySet().size() == 0)
//			return null;
//
//		PdfPCell cell;
//
//		PdfPTable classSchemeTable = new PdfPTable(1);
//		classSchemeTable.setWidthPercentage(100);
//		classSchemeTable.getDefaultCell().setBorder(0);
//		classSchemeTable.setSplitLate(false);
//
//		Phrase classSchemeName = new Phrase(" --- " + canonicalName.replaceAll("[A-Z][a-z]", " $0") + " --- \n",
//				timesBoldMedium);
//		cell = new PdfPCell(classSchemeName);
//		cell.setBorder(0);
//		cell.setColspan(1);
//		classSchemeTable.addCell(classSchemeName);
//		classSchemeTable.setHeaderRows(1);
//
//		HashMap structMap = new HashMap();
//
//		TreeData currentTreeData = (TreeData) currentDisplayData;
//		structMap = buildMap(currentTreeData);
//
//		Structure defaultStruct = DisplayDataProcessor.processDisplayStructure(currentTreeData.getDefaultStructure());
//
//		Object[] entries = structMap.entrySet().toArray();
//		Arrays.sort(entries, new Comparator() {
//
//			private AlphanumericComparator comparator = new AlphanumericComparator();
//
//			public int compare(Object lhs, Object rhs) {
//				Entry le = (Entry) lhs;
//				Entry re = (Entry) rhs;
//
//				return comparator.compare((String) le.getKey(), (String) re.getKey());
//			}
//		});
//
//		for (int i = 0; i < entries.length; i++) {
//			Entry<String, String> curEntry = (Entry) entries[i];
//
//			if (selected.containsKey(curEntry.getKey())) {
//				PdfPCell cur = parseField(selected.get(curEntry.getKey()), curEntry.getValue(), 16, defaultStruct, true);
//
//				if (cur != null)
//					classSchemeTable.addCell(cur);
//			}
//		}
//
//		PdfPCell ret = new PdfPCell(classSchemeTable);
//		ret.setBorder(Rectangle.TOP + Rectangle.BOTTOM);
//		ret.setPadding(5);
//		return ret;
//	}
//
//	private PdfPCell parseField(ArrayList<String> data, String canonicalName, int dataIndent,
//			Structure defaultStructure, boolean forceShowCanonicalName) {
//		if (data.size() == 0)
//			return null;
//
//		PdfPCell cell;
//		PdfPTable fieldTable = new PdfPTable(data.size());
//		fieldTable.setWidthPercentage(100);
//		fieldTable.getDefaultCell().setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
//
//		ArrayList prettyData = new ArrayList();
//		defaultStructure.getDisplayableData(data, prettyData, 0);
//
//		ArrayList headers = defaultStructure.extractDescriptions();
//
//		cell = new PdfPCell();
//		cell.setBorder(0);
//		cell.setColspan(data.size());
//		if (forceShowCanonicalName)
//			cell.addElement(new Phrase(canonicalName, timesBoldSmall));
//		else
//			cell.addElement(new Phrase(canonicalName, timesBoldMedium));
//		fieldTable.addCell(cell);
//
//		if (headers.size() == prettyData.size())
//			for (Iterator iter = headers.listIterator(); iter.hasNext();)
//				fieldTable.addCell(wrapInBorderlessCell(new Phrase(iter.next().toString(), timesBoldItalicsSmall)));
//
//		boolean actuallyData = false;
//
//		for (Iterator iter = prettyData.listIterator(); iter.hasNext();) {
//			String tempText = iter.next().toString();
//
//			if (tempText.equals(""))
//				tempText = "(Not specified)";
//			else if (!tempText.equals("(Not Specified)"))
//				actuallyData = true;
//
//			if (canonicalName.endsWith("Documentation") || canonicalName.equalsIgnoreCase("RedListRationale")) {
//				try {
//					String htmlContent = tempText;
//
//					htmlContent = "<html><head><style type=\"text/css\">body { "
//							+ "font-size: 10pt; font-family: Helvetica, Times, serif; "
//							+ "line-height: 20pt; }</style></head><body>" + htmlContent;
//
//					htmlContent += "</body></html>";
//
//					ByteArrayOutputStream o = new ByteArrayOutputStream();
//					ByteArrayInputStream in = new ByteArrayInputStream(htmlContent.getBytes("UTF-8"));
//
//					Tidy t = new Tidy();
//					t.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
//
//					org.w3c.dom.Document pretty = t.parseDOM(in, o);
//
//					BufferedImage image = renderToImage(pretty, 1000);
//
//					cell = new PdfPCell();
//					cell.setBorder(0);
//
//					Image temp = Image.getInstance(image, Color.BLACK);
//					temp.setIndentationLeft(dataIndent);
//					cell.addElement(temp);
//
//					fieldTable.addCell(cell);
//				} catch (Exception e) {
//					e.printStackTrace();
//					Phrase struct = new Phrase(tempText, timesSmall);
//					cell = wrapInBorderlessCell(struct);
//					cell.setIndent(dataIndent);
//					cell.setFollowingIndent(dataIndent);
//					fieldTable.addCell(cell);
//				}
//			} else {
//				Phrase struct = new Phrase(tempText, timesSmall);
//				cell = wrapInBorderlessCell(struct);
//				cell.setIndent(dataIndent);
//				cell.setFollowingIndent(dataIndent);
//				fieldTable.addCell(cell);
//			}
//		}
//
//		if (fieldTable.getRows().size() == 1 || !actuallyData)
//			return null;
//
//		fieldTable.setSplitLate(false);
//
//		cell = new PdfPCell();
//		cell.setPadding(5);
//		cell.setBorder(0);
//
//		if (!forceShowCanonicalName)
//			cell.setBorder(Rectangle.TOP + Rectangle.BOTTOM);
//
//		cell.addElement(fieldTable);
//
//		return cell;
//	}
//
//	public BufferedImage renderToImage(org.w3c.dom.Document doc, int width) {
//		Graphics2DRenderer g2r = new Graphics2DRenderer();
//		g2r.setDocument(doc, "");
//		g2r.getSharedContext().getTextRenderer().setSmoothingLevel(org.xhtmlrenderer.extend.TextRenderer.HIGH);
//
//		java.awt.Font awtFont = null;
//
//		try {
//			java.awt.Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//
//			if (fonts.length > 0) {
//				awtFont = fonts[0];
//
//				for (java.awt.Font f : fonts) {
//					if (f.getFontName().equalsIgnoreCase("Helvetica") || f.getFontName().equalsIgnoreCase("Times")) {
//						awtFont = f;
//						break;
//					}
//				}
//			} else
//				awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, AssessmentTemplate.class
//						.getResourceAsStream("TribuneRegular.ttf"));
//
//			g2r.getPanel().setFont(awtFont);
//			g2r.getPanel().setFontScalingFactor(1.5f);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		Dimension dim = new Dimension(width, 4000);
//
//		// do layout with temp buffer
//		BufferedImage buff = new BufferedImage((int) dim.getWidth(), (int) dim.getHeight(),
//				BufferedImage.TYPE_BYTE_GRAY);
//		Graphics2D g = (Graphics2D) buff.getGraphics();
//		g.setFont(awtFont);
//		g2r.layout(g, new Dimension(width, 4000));
//		g.dispose();
//
//		// get size
//		java.awt.Rectangle rect = g2r.getMinimumSize();
//
//		// render into real buffer
//		buff = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
//		g = (Graphics2D) buff.getGraphics();
//		g.setFont(awtFont);
//		g2r.render(g);
//		g.dispose();
//
//		// return real buffer
//		return buff;
//	}
//
//	public void setWriter(DocWriter writer) {
//		this.writer = ((PdfWriter) writer);
//	}
//
//	private PdfPCell wrapInBorderlessCell(PdfPTable table) {
//		PdfPCell cell = new PdfPCell(table);
//		cell.setBorder(0);
//		cell.setPadding(0);
//		cell.setIndent(0);
//		cell.setPaddingTop(6);
//
//		return cell;
//	}
//
//	private PdfPCell wrapInBorderlessCell(Phrase element) {
//		PdfPCell cell = new PdfPCell(element);
//		cell.setBorder(0);
//
//		return cell;
//	}
//}
//
///*
// * <sourceDate></sourceDate>
// * 
// * <dateAssessed></dateAssessed> <dateModified>2007-10-18
// * 10:03:15</dateModified> <dateAdded>2007-10-18 07:25:07</dateAdded>
// * <dateFinalized></dateFinalized> <critVersion>3.1</critVersion>
// * 
// * <rationale></rationale> <validationStatus>draft_status</validationStatus>
// * 
// * <global /> <categoryAbbreviation></categoryAbbreviation>
// * <categoryCriteria></categoryCriteria>
// * <categoryFuzzyResult></categoryFuzzyResult>
// * <manualCategoryAbbreviation></manualCategoryAbbreviation>
// * <manualCategoryCriteria></manualCategoryCriteria>
// * <categoryText></categoryText> <isDone>true</isDone> <note></note>
// */
