package org.iucn.sis.shared.data.assessments;

import java.util.ArrayList;

import org.iucn.sis.client.displays.ClassificationScheme;
import org.iucn.sis.client.displays.Display;
import org.iucn.sis.client.displays.Field;
import org.iucn.sis.client.displays.SISRow;
import org.iucn.sis.shared.DisplayData;
import org.iucn.sis.shared.DisplayDataProcessor;
import org.iucn.sis.shared.FieldData;
import org.iucn.sis.shared.TreeData;
import org.iucn.sis.shared.TreeDataRow;
import org.iucn.sis.shared.structures.BooleanRule;
import org.iucn.sis.shared.structures.ContentRule;
import org.iucn.sis.shared.structures.SelectRule;
import org.iucn.sis.shared.xml.XMLUtils;

import com.google.gwt.xml.client.Node;
import com.solertium.lwxml.gwt.debug.SysDebugger;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNode;
import com.solertium.lwxml.shared.NativeNodeList;

/**
 * Parses a field tag into the appropriate Display object.
 * 
 * @author adam.schwartz
 * 
 */
public class FieldParser {
	private static final String BASIC_INFORMATION_TAG_NAME = "basicInformation";
	private static final String ASSESSMENT_FIELD_TAG_NAME = "field";
	private static final String STRUCTURES_DELIMETER_TAG_NAME = "structures";

	private static final String ASSESSMENT_TREE_TAG_NAME = "tree";
	private static final String TREE_ROOT_TAG_NAME = "treeRoot";

	private static final String CANONICAL_NAME_TAG_NAME = "canonicalName";
	private static final String DESCRIPTION_TAG_NAME = "description";
	private static final String CLASS_OF_SERVICE_TAG_NAME = "classOfService";
	private static final String LOCATION_TAG_NAME = "location";
	private static final String REFERENCES_TAG_NAME = "references";

	/**
	 * Recursively-used helper function for processing tree rows and their
	 * children
	 * 
	 * @param currentRow
	 *            the DisplayData to process to make the tree
	 * @return the SISRow made from the DisplayData, complete with children
	 */
	public static SISRow processTree(TreeDataRow currentRow) {
		SISRow row = new SISRow(DisplayDataProcessor.processDisplayStructure(currentRow));

		// Set options
		row.setCodeable(!(currentRow.getCodeable().equalsIgnoreCase("false")));
		row.setExpanded(!(currentRow.getExpanded().equalsIgnoreCase("false")));
		row.setUsesDefaultStructure(currentRow.getUsesDefaultStructure());

		row.setRowID(currentRow.getRowNumber());
		row.setLabel(currentRow.getDescription());
		row.setDepth(currentRow.getDepth());

		// Add kids
		if (currentRow.hasChildren()) {
			for (int j = 0; j < currentRow.getChildren().size(); j++) {
				row.addChild(processTree(((TreeDataRow) currentRow.getChildren().get(j))));
			}
		}
		return row;
	}

	protected Display doOperate(DisplayData currentDisplayData) {
		if (currentDisplayData.getType().equalsIgnoreCase(DisplayData.FIELD)) {
			FieldData currentFieldData = (FieldData) currentDisplayData;
			final Field field = new Field(currentFieldData);
			field.addStructure(DisplayDataProcessor.processDisplayStructure(currentDisplayData));

			return field;
		} else if (currentDisplayData.getType().equalsIgnoreCase(DisplayData.TREE)) {
			TreeData currentTreeData = (TreeData) currentDisplayData;

			// TODO: USE A CLASSIFICATIONSCHEME INSTEAD OF A TREE
			ClassificationScheme scheme = new ClassificationScheme(currentTreeData);
			return scheme;

			// SISTree tree = new SISTree(currentTreeData);
			// for (int i = 0; i < currentTreeData.getTreeRoots().size(); i++) {
			// tree.addRoot(processTree((TreeDataRow)currentTreeData.getTreeRoots
			// ().get(i)));
			// }
			// tree.setDefaultTreeStructure(DisplayDataProcessor.
			// processDisplayStructure(currentTreeData.getDefaultStructure()));
			//
			// return tree;
		} else
			return null;
	}

	public Display parseField(NativeDocument doc) {
		return parseField(doc.getDocumentElement());
	}

	public Display parseField(NativeElement fieldElement) {
		// Process the display objects
		if (fieldElement.getNodeName().equalsIgnoreCase(ASSESSMENT_FIELD_TAG_NAME)) {
			try {
				return doOperate(processFieldTag(fieldElement));
			} catch (Exception e) {
				e.printStackTrace();
				SysDebugger.getInstance().println(
						"Failed to process field " + fieldElement.getElementByTagName("canonicalName").getText());
			}
		} else {
			try {
				return doOperate(processTreeTags(fieldElement));
			} catch (Exception e) {
				e.printStackTrace();
				SysDebugger.getInstance().println(
						"Failed to process classification scheme "
								+ fieldElement.getElementByTagName("canonicalName").getText());
			}
		}

		return null;
	}

	public DisplayData parseFieldData(NativeDocument doc) {
		return parseFieldData(doc.getDocumentElement());
	}

	public DisplayData parseFieldData(NativeElement fieldElement) {
		// Process the display objects
		if (fieldElement.getNodeName().equalsIgnoreCase(ASSESSMENT_FIELD_TAG_NAME)) {
			try {
				return processFieldTag(fieldElement);
			} catch (Exception e) {
				e.printStackTrace();
				SysDebugger.getInstance().println(
						"Failed to process field " + fieldElement.getElementByTagName("canonicalName").getText());
			}
		} else {
			try {
				return processTreeTags(fieldElement);
			} catch (Exception e) {
				e.printStackTrace();
				SysDebugger.getInstance().println(
						"Failed to process classification scheme "
								+ fieldElement.getElementByTagName("canonicalName").getText());
			}
		}

		return null;
	}

	/**
	 * Fills in the data arraylist of the currentField based on the Structure
	 * 
	 * @param NativeNodeList
	 *            with XML data about structures
	 * @return an ArrayList containing DisplayData for structures
	 */
	private ArrayList parseStructures(NativeNodeList structureTags) {
		ArrayList structureSet = new ArrayList();
		DisplayData currentDisplayData;

		NativeElement current = null;
		NativeElement structureTag = null;

		for (int structs = 0; structs < structureTags.getLength(); structs++) {
			structureTag = structureTags.elementAt(structs);

			if (structureTag == null || !structureTag.getNodeName().equalsIgnoreCase("structure"))
				continue;

			if (structureTag.getFirstChild().getNodeType() == Node.TEXT_NODE)
				current = structureTag.getChildNodes().elementAt(1);
			else
				current = structureTag.getChildNodes().elementAt(0);

			String structureType = current.getNodeName();
			String name = XMLUtils.getXMLAttribute(structureTag, "name", null);
			String descript = XMLUtils.getXMLAttribute(structureTag, "description", null);
			String isVisible = XMLUtils.getXMLAttribute(structureTag, "isVisible", null);
			String style = XMLUtils.getXMLAttribute(structureTag, "style", null);
			String id = XMLUtils.getXMLAttribute(structureTag, "id", null);

			currentDisplayData = new FieldData();
			currentDisplayData.setStructure(structureType);
			if (descript != null)
				currentDisplayData.setDescription(descript);
			if (id != null)
				currentDisplayData.setUniqueId(id);

			currentDisplayData.setName(name);
			currentDisplayData.setIsVisible(isVisible);
			currentDisplayData.setStyle(style);

			// Do the work based on the structure
			if (structureType.equalsIgnoreCase(XMLUtils.TEXT_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.NUMBER_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.DATE_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.NARRATIVE_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.RICH_TEXT_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.JUSTIFICATION_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.IMAGE_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.FILE_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.QUALIFIER_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.BOOLEAN_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.BOOLEAN_UNKNOWN_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.USE_TRADE)
					|| structureType.equalsIgnoreCase(XMLUtils.OPTIONS_LIST)
					|| structureType.equalsIgnoreCase(XMLUtils.NOTE_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.LABEL_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.LIVELIHOODS)
					|| structureType.equalsIgnoreCase(XMLUtils.REFERENCE_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.RED_LIST_CATEGORIES_CRITERIA)
					|| structureType.equalsIgnoreCase(XMLUtils.EMPTY_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.BOOLEAN_RANGE_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.REGIONAL_EXPERT_QUESTIONS_STRUCTURE)) {
				currentDisplayData.setData(XMLUtils.getXMLValue(current, current.getText()));
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.ONE_TO_MANY)) {
				ArrayList parsedStructs = parseStructures(current.getChildNodes());
				currentDisplayData.setData(parsedStructs.get(0));
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.CLASSIFICATION_SCHEME_STRUCTURE)) {
				NativeElement defaultStructureElement = current.getElementByTagName("defaultStructure");

				NativeNodeList options = current.getElementsByTagName("option");
				ArrayList data = new ArrayList();

				for (int k = 0; k < options.getLength(); k++) {
					data.add(options.elementAt(k).getText());
				}

				currentDisplayData.setData(data);
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.RANGE_STRUCTURE)) {
				NativeNodeList rangeList = current.getChildNodes();
				ArrayList data = new ArrayList();

				for (int k = 0; k < rangeList.getLength(); k++) {
					if (rangeList.item(k).getNodeName().equalsIgnoreCase(XMLUtils.RANGE_LOW_GUESS))
						data.add(XMLUtils.getXMLValue(rangeList.item(k), ""));
					else if (rangeList.item(k).getNodeName().equalsIgnoreCase(XMLUtils.RANGE_HIGH_GUESS))
						data.add(XMLUtils.getXMLValue(rangeList.item(k), ""));
					/*
					 * else if(
					 * rangeList.item(k).getNodeName().equalsIgnoreCase(
					 * XMLUtils.RANGE_BEST_GUESS) ) data.add(
					 * XMLUtils.getXMLValue(rangeList.item(k), "") );
					 */
				}

				currentDisplayData.setData(data);
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.MULTIPLE_SELECT_STRUCTURE)
					|| structureType.equalsIgnoreCase(XMLUtils.SINGLE_SELECT_STRUCTURE)) {
				NativeNodeList selectOptions = current.getChildNodes();

				ArrayList data = new ArrayList();
				ArrayList options = new ArrayList();
				ArrayList selected = new ArrayList();

				for (int k = 0; k < selectOptions.getLength(); k++) {
					if (selectOptions.item(k).getNodeName().equalsIgnoreCase("option"))
						options.add(XMLUtils.getXMLValue(selectOptions.item(k), ""));

					if (selectOptions.item(k).getNodeName().equalsIgnoreCase("selected"))
						selected.add(XMLUtils.getXMLValue(selectOptions.item(k), ""));
				}

				data.add(0, options);
				data.add(1, selected);

				currentDisplayData.setData(data);
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.MULTIPLE_TEXT_STRUCTURE)) {
				ArrayList data = new ArrayList();
				NativeNodeList textNodes = current.getChildNodes();

				for (int i = 0; i < textNodes.getLength(); i++) {
					NativeNode currentTextNode = textNodes.item(i);

					if (currentTextNode.getNodeName().equalsIgnoreCase("text")) {
						data.add(XMLUtils.getXMLValue(currentTextNode, ""));
					}
				}

				currentDisplayData.setData(data);
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.THREAT_STRUCTURE)) {
				NativeNodeList details = current.getChildNodes();
				ArrayList threatData = new ArrayList();

				for (int m = 0; m < details.getLength(); m++) {

					if (details.item(m).getNodeName().equalsIgnoreCase("id")) {
						threatData.add(XMLUtils.getXMLValue(details.item(m), ""));
					} else if (details.item(m).getNodeName().equalsIgnoreCase("desc")) {
						currentDisplayData.setDescription(XMLUtils.getXMLValue(details.item(m), ""));
						threatData.add(XMLUtils.getXMLValue(details.item(m), ""));
					} else if (details.item(m).getNodeName().equalsIgnoreCase("codeable")) {
						threatData.add(XMLUtils.getXMLValue(details.item(m), ""));
					}

					try {
						if (details.item(m).getNodeName().equalsIgnoreCase("timing")) {
							threatData.add(XMLUtils.getXMLValue(details.item(m), ""));
						} else if (details.item(m).getNodeName().equalsIgnoreCase("scope")) {
							threatData.add(XMLUtils.getXMLValue(details.item(m), ""));
						} else if (details.item(m).getNodeName().equalsIgnoreCase("severity")) {
							threatData.add(XMLUtils.getXMLValue(details.item(m), ""));
						} else if (details.item(m).getNodeName().equalsIgnoreCase("impact")) {
							threatData.add(XMLUtils.getXMLValue(details.item(m), ""));
						}
					} catch (Exception e) {
						threatData = null;
						break;
					}
				}

				currentDisplayData.setData(threatData);
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.RELATED_STRUCTURE)) {
				// Has 3 children I am interested in ...
				/* 1 */FieldData dominantStructure = new FieldData();
				/* 2 */ArrayList dependentStructures = new ArrayList();
				/* 3 */ArrayList rules = new ArrayList();

				NativeNodeList children = current.getChildNodes();

				for (int i = 0; i < children.getLength(); i++) {
					String key = children.item(i).getNodeName();
					// Look for dominant
					if (key.equalsIgnoreCase("dominantStructures")) {
						ArrayList dominantData = parseStructures(children.elementAt(i).getChildNodes());
						if (dominantData.size() == 1) {
							dominantStructure.setStructure(((FieldData) dominantData.get(0)).getStructure());
							dominantStructure.setData(((FieldData) dominantData.get(0)).getData());
							dominantStructure.setDescription((((FieldData) dominantData.get(0)).getDescription()));
							dominantStructure.setName(((FieldData) dominantData.get(0)).getName());
							dominantStructure.setIsVisible(((FieldData) dominantData.get(0)).getIsVisible());
							dominantStructure.setStyle(((FieldData) dominantData.get(0)).getStyle());
						} else {
							dominantStructure.setStructure(XMLUtils.DOMINANT_STRUCTURE_COLLECTION);
							dominantStructure.setData(dominantData);
						}
					}

					else if (key.equalsIgnoreCase("dependentStructures")) {
						dependentStructures = parseStructures(children.elementAt(i).getChildNodes());
					}

					else if (key.equalsIgnoreCase("rules")) {
						NativeNodeList ruleSet = children.item(i).getChildNodes();
						for (int ruleCount = 0; ruleCount < ruleSet.getLength(); ruleCount++) {
							if (ruleSet.item(ruleCount).getNodeName().equalsIgnoreCase(XMLUtils.BOOLEAN_RULE)) {
								BooleanRule rule = null;
								for (int bRule = 0; bRule < ruleSet.item(ruleCount).getChildNodes().getLength(); bRule++) {
									if (ruleSet.item(ruleCount).getChildNodes().item(bRule).getNodeName()
											.equalsIgnoreCase("activateOnRule")) {
										rule = new BooleanRule(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
												.getChildNodes().item(bRule)));
									} else if (ruleSet.item(ruleCount).getChildNodes().item(bRule).getNodeName()
											.equalsIgnoreCase("actions")) {
										for (int acts = 0; acts < ruleSet.item(ruleCount).getChildNodes().item(bRule)
												.getChildNodes().getLength(); acts++) {
											if (ruleSet.item(ruleCount).getChildNodes().item(bRule).getChildNodes()
													.item(acts).getNodeName().equalsIgnoreCase("onTrue")) {
												rule.setOnTrue(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
														.getChildNodes().item(bRule).getChildNodes().item(acts)));
											} else if (ruleSet.item(ruleCount).getChildNodes().item(bRule)
													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
															"onFalse")) {
												rule.setOnFalse(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
														.getChildNodes().item(bRule).getChildNodes().item(acts)));
											} else if (ruleSet.item(ruleCount).getChildNodes().item(bRule)
													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
															"affectedDependentStructures")) {
												rule.addAffectedObjectIndex(XMLUtils.getXMLValue(ruleSet
														.item(ruleCount).getChildNodes().item(bRule).getChildNodes()
														.item(acts)));
											}
										}
									}
								}
								rules.add(rule);
							} else if (ruleSet.item(ruleCount).getNodeName().equalsIgnoreCase(XMLUtils.SELECT_RULE)) {
								SelectRule rule = null;
								for (int sRule = 0; sRule < ruleSet.item(ruleCount).getChildNodes().getLength(); sRule++) {
									if (ruleSet.item(ruleCount).getChildNodes().item(sRule).getNodeName()
											.equalsIgnoreCase("activateOnIndex")) {
										// SysDebugger.getInstance().println(
										// "Found rule");
										rule = new SelectRule(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
												.getChildNodes().item(sRule)));
									} else if (ruleSet.item(ruleCount).getChildNodes().item(sRule).getNodeName()
											.equalsIgnoreCase("actions")) {
										for (int acts = 0; acts < ruleSet.item(ruleCount).getChildNodes().item(sRule)
												.getChildNodes().getLength(); acts++) {
											// SysDebugger.getInstance().println(
											// "Action: " +
											// ruleSet.item(ruleCount
											// ).getChildNodes
											// ().item(sRule).getChildNodes
											// ().item(acts).getNodeName());
											if (ruleSet.item(ruleCount).getChildNodes().item(sRule).getChildNodes()
													.item(acts).getNodeName().equalsIgnoreCase("onTrue")) {
												rule.setOnTrue(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
														.getChildNodes().item(sRule).getChildNodes().item(acts)));
											} else if (ruleSet.item(ruleCount).getChildNodes().item(sRule)
													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
															"onFalse")) {
												rule.setOnFalse(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
														.getChildNodes().item(sRule).getChildNodes().item(acts)));
											} else if (ruleSet.item(ruleCount).getChildNodes().item(sRule)
													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
															"affectedDependentStructures")) {
												rule.addAffectedObjectIndex(XMLUtils.getXMLValue(ruleSet
														.item(ruleCount).getChildNodes().item(sRule).getChildNodes()
														.item(acts)));
											}
										}
									}
								}
								rules.add(rule);
							} else if (ruleSet.item(ruleCount).getNodeName().equalsIgnoreCase(XMLUtils.CONTENT_RULE)) {
								ContentRule rule = null;
								for (int cRule = 0; cRule < ruleSet.item(ruleCount).getChildNodes().getLength(); cRule++) {
									if (ruleSet.item(ruleCount).getChildNodes().item(cRule).getNodeName()
											.equalsIgnoreCase("activateOnContent")) {
										rule = new ContentRule(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
												.getChildNodes().item(cRule), ""));
									} else if (ruleSet.item(ruleCount).getChildNodes().item(cRule).getNodeName()
											.equalsIgnoreCase("actions")) {
										for (int acts = 0; acts < ruleSet.item(ruleCount).getChildNodes().item(cRule)
												.getChildNodes().getLength(); acts++) {
											if (ruleSet.item(ruleCount).getChildNodes().item(cRule).getChildNodes()
													.item(acts).getNodeName().equalsIgnoreCase("onTrue")) {
												rule.setOnTrue(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
														.getChildNodes().item(cRule).getChildNodes().item(acts)));
											} else if (ruleSet.item(ruleCount).getChildNodes().item(cRule)
													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
															"onFalse")) {
												rule.setOnFalse(XMLUtils.getXMLValue(ruleSet.item(ruleCount)
														.getChildNodes().item(cRule).getChildNodes().item(acts)));
											} else if (ruleSet.item(ruleCount).getChildNodes().item(cRule)
													.getChildNodes().item(acts).getNodeName().equalsIgnoreCase(
															"affectedDependentStructures")) {
												rule.addAffectedObjectIndex(XMLUtils.getXMLValue(ruleSet
														.item(ruleCount).getChildNodes().item(cRule).getChildNodes()
														.item(acts)));
											}
										}
									}
								}
								rules.add(rule);
							}
						}// for
					}// else
				}

				ArrayList relatedStructureData = new ArrayList();
				relatedStructureData.add(dominantStructure);
				relatedStructureData.add(dependentStructures);
				relatedStructureData.add(rules);
				relatedStructureData.add(XMLUtils.getXMLAttribute(current, "layout", ""));
				relatedStructureData.add(XMLUtils.getXMLAttribute(current, "dependentsLayout", ""));

				currentDisplayData.setData(relatedStructureData);
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.MAP_STRUCTURE)) {
				ArrayList myMapData = new ArrayList();
				ArrayList ids = new ArrayList();
				ArrayList descriptions = new ArrayList();
				ArrayList latitudes = new ArrayList();
				ArrayList longitudes = new ArrayList();

				NativeNodeList dataPoints = current.getChildNodes();
				boolean insufficientData = false;

				for (int m = 0; m < dataPoints.getLength(); m++) {
					if (dataPoints.item(m).getNodeName().equalsIgnoreCase("dataPoint")) {
						NativeNodeList data = dataPoints.item(m).getChildNodes();
						for (int k = 0; k < data.getLength(); k++) {
							try {
								if (data.item(k).getNodeName().equalsIgnoreCase("id"))
									ids.add((XMLUtils.getXMLValue(data.item(k), "")));
								else if (data.item(k).getNodeName().equalsIgnoreCase("description"))
									descriptions.add(XMLUtils.getXMLValue(data.item(k), ""));
								else if (data.item(k).getNodeName().equalsIgnoreCase("latitude"))
									latitudes
											.add(new Double(Double.parseDouble(XMLUtils.getXMLValue(data.item(k), ""))));
								else if (data.item(k).getNodeName().equalsIgnoreCase("longitude"))
									longitudes.add(new Double(Double
											.parseDouble(XMLUtils.getXMLValue(data.item(k), ""))));
							} catch (Exception e) {
								insufficientData = true;
							}
						}
					}
				}
				if (insufficientData) {
					myMapData = null;
				} else {
					myMapData.add(ids);
					myMapData.add(latitudes);
					myMapData.add(longitudes);
					myMapData.add(descriptions);
				}

				currentDisplayData.setData(myMapData);
			}

			else if (structureType.equalsIgnoreCase(XMLUtils.TREE_STRUCTURE)) {
				TreeData treeData = new TreeData();
				TreeData defaultTreeStructure = new TreeData();

				NativeNodeList treeRoots = current.getChildNodes();

				for (int m = 0; m < treeRoots.getLength(); m++) {
					NativeNode currentRoot = treeRoots.item(m);
					if (currentRoot.getNodeName().equalsIgnoreCase("defaultStructure")) {
						for (int k = 0; k < currentRoot.getChildNodes().getLength(); k++) {
							if (currentRoot.getChildNodes().item(k).getNodeName().equalsIgnoreCase("treeStructures")) {
								// SysDebugger.getInstance().println(
								// "Made default tree structure");
								defaultTreeStructure = new TreeData();
								ArrayList defaultTreeStructureSet = parseStructures(currentRoot.getChildNodes()
										.elementAt(k).getChildNodes());
								if (structureSet.size() == 1) {
									defaultTreeStructure.setStructure(((FieldData) defaultTreeStructureSet.get(0))
											.getStructure());
									defaultTreeStructure
											.setData(((FieldData) defaultTreeStructureSet.get(0)).getData());
								} else {
									defaultTreeStructure.setStructure(XMLUtils.STRUCTURE_COLLECTION);
									defaultTreeStructure.setData(defaultTreeStructureSet);
								}
								treeData.setDefaultStructure(defaultTreeStructure);
							}
						}
					} else if (currentRoot.getNodeName().equalsIgnoreCase("root")) {
						treeData.addTreeRoot(processRoot(currentRoot, defaultTreeStructure));
					}
				}

				if (current.getAttribute("description") != null)
					treeData.setDescription(current.getAttribute("description"));
				currentDisplayData.setData(treeData);
			}

			else if (structureType.equalsIgnoreCase("complex")) {
				// DEPRECATED TYPE.
				currentDisplayData.setData("");
			}

			structureSet.add(currentDisplayData);

		}
		return structureSet;

	}

	private void processBasicDisplayData(DisplayData displayData, NativeElement displayTag) {
		for (int i = 0; i < displayTag.getChildNodes().getLength(); i++) {
			NativeNode current = displayTag.getChildNodes().item(i);

			if (current.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String curNodeName = current.getNodeName();
			// SysDebugger.getInstance().println("Processing a " +
			// current.getNodeName() + " tag.");
			if (curNodeName.equalsIgnoreCase(CANONICAL_NAME_TAG_NAME))
				displayData.setCanonicalName(XMLUtils.getXMLValue(current).trim());
			else if (curNodeName.equalsIgnoreCase(CLASS_OF_SERVICE_TAG_NAME))
				displayData.setClassOfService(XMLUtils.getXMLValue(current));
			else if (curNodeName.equalsIgnoreCase(LOCATION_TAG_NAME))
				displayData.setLocation(XMLUtils.getXMLValue(current));
			else if (curNodeName.equalsIgnoreCase(DESCRIPTION_TAG_NAME))
				displayData.setDescription(XMLUtils.getXMLValue(current));
			else if (curNodeName.equalsIgnoreCase(REFERENCES_TAG_NAME)) {
				NativeNodeList referenceIdNodes = current.getChildNodes();
				ArrayList refs = new ArrayList();
				for (int k = 0; k < referenceIdNodes.getLength(); k++)
					if (referenceIdNodes.item(k).getNodeName().equalsIgnoreCase("referenceId"))
						refs.add(XMLUtils.getXMLValue(referenceIdNodes.item(k), ""));
				displayData.setReferences(refs);
				break;
			}
		}
	}

	/**
	 * Processes XML tags with Field data
	 * 
	 * @param fieldTags
	 *            NativeNodeList of Field XML data
	 */
	private FieldData processFieldTag(NativeElement root) {
		FieldData currentField = new FieldData();
		NativeElement structuresTag = root.getElementByTagName(STRUCTURES_DELIMETER_TAG_NAME);

		// Gather Display Data
		processBasicDisplayData(currentField, root);
		// processReferences(currentField, fieldTags, i);
		currentField.setDisplayId(XMLUtils.getXMLAttribute(root, "id"));

		// Build the structure
		ArrayList structureSet = parseStructures(structuresTag.getChildNodes());
		if (structureSet.size() == 1) {
			currentField.setStructure(((FieldData) structureSet.get(0)).getStructure());
			currentField.setData(((FieldData) structureSet.get(0)).getData());
			// currentField.setDescription(((FieldData)structureSet.get(0)).
			// getDescription());
		} else {
			currentField.setStructure(XMLUtils.STRUCTURE_COLLECTION);
			currentField.setData(structureSet);
			((FieldData) structureSet.get(0)).setDescription(currentField.getDescription());

			String layout = root.getAttribute("layout");
			if (layout != null)
				currentField.setStyle(layout);
		}

//		SysDebugger.getInstance().println(
//				currentField.getStructure() + ": " + currentField.getDescription() + ":" + structureSet.size());
		return currentField;
	}

	private void processReferences(DisplayData displayData, NativeNode displayTag) {
		for (int i = 0; i < displayTag.getChildNodes().getLength(); i++) {
			NativeNode current = displayTag.getChildNodes().item(i);
			if (current.getNodeName().equalsIgnoreCase(REFERENCES_TAG_NAME)) {
				NativeNodeList referenceIdNodes = current.getChildNodes();
				ArrayList refs = new ArrayList();
				for (int k = 0; k < referenceIdNodes.getLength(); k++)
					if (referenceIdNodes.item(k).getNodeName().equalsIgnoreCase("referenceId"))
						refs.add(XMLUtils.getXMLValue(referenceIdNodes.item(k), ""));
				displayData.setReferences(refs);
				break;
			}
		}
	}

	/**
	 * Recursively-used helper function to process a SISRow's root rows
	 * 
	 * @param currentRoot
	 *            the NativeNode containing root XML data
	 * @return a TreeDataRow object representation of the XML
	 */
	private TreeDataRow processRoot(NativeNode currentRoot, TreeData defaultTreeStructure) {
		String value;
		ArrayList structureSet;
		boolean override = false;
		TreeDataRow currentRow = new TreeDataRow();

		// Optional parameters
		currentRow.setCodeable(XMLUtils.getXMLAttribute(currentRoot, "codeable"));
		// currentRow.setExpanded(XMLUtils.getXMLAttribute(currentRoot,
		// "expanded"));
		currentRow.setRowNumber(XMLUtils.getXMLAttribute(currentRoot, "id", ""));
		currentRow.setDepth(XMLUtils.getXMLAttribute(currentRoot, "depth", ""));
		currentRow.setDisplayId(XMLUtils.getXMLAttribute(currentRoot, "code", ""));

		// Parse the necessary root data
		NativeNodeList currentRootData = currentRoot.getChildNodes();
		for (int k = 0; k < currentRootData.getLength(); k++) {
			// This is "root" data for a non-leaf node
			if (currentRootData.item(k).getNodeName().equalsIgnoreCase("label")
					&& !(value = XMLUtils.getXMLValue(currentRootData.item(k)))
							.equalsIgnoreCase(XMLUtils.NO_NODE_VALUE_FOUND)) {
				currentRow.setDescription(value);
				currentRow.setTitle(XMLUtils.getXMLAttribute(currentRootData.item(k), "title", null));
			}
			// As is this
			else if (currentRootData.item(k).getNodeName().equalsIgnoreCase("treeStructures")) {
				override = true;
				structureSet = parseStructures(currentRootData.elementAt(k).getChildNodes());
				if (structureSet.size() == 1) {
					currentRow.setStructure(((FieldData) structureSet.get(0)).getStructure());
					currentRow.setData(((FieldData) structureSet.get(0)).getData());
				} else {
					currentRow.setStructure(XMLUtils.STRUCTURE_COLLECTION);
					currentRow.setData(structureSet);
				}
			}

			// Now for the (optional) children (recursion!). Recursion halts on
			// leaf nodes (no child)
			else if (currentRootData.item(k).getNodeName().equalsIgnoreCase("child")) {
				currentRow.addChild(processRoot(currentRootData.item(k), defaultTreeStructure));
			}
		}

		// If there was no overriding structure given
		if (!override) {
			// SysDebugger.getInstance().println(
			// "No override given, use default data to make a " +
			// defaultTreeStructure.getStructure());
			currentRow.setStructure(defaultTreeStructure.getStructure());
			currentRow.setData(defaultTreeStructure.getData());
		}

		currentRow.setUsesDefaultStructure(!override);

		return currentRow;
	}

	/**
	 * Processes XML tags with Tree data
	 * 
	 * @param treeTags
	 *            NativeNodeList of Tree XML data
	 */
	private TreeData processTreeTags(NativeElement root) {

		TreeData treeData = new TreeData();

		// Gather Display Data
		processBasicDisplayData(treeData, root);
		treeData.setDisplayId(XMLUtils.getXMLAttribute(root, "id"));

		TreeData defaultTreeStructure = null;

		// Parse each particular root
		NativeNodeList treeRoots = root.getElementByTagName("treeRoot").getChildNodes();
		for (int j = 0; j < treeRoots.getLength(); j++) {
			NativeNode currentRoot = treeRoots.item(j);
			String curNodeName = currentRoot.getNodeName();
			if (curNodeName.equalsIgnoreCase("defaultStructure")) {
				for (int k = 0; k < currentRoot.getChildNodes().getLength(); k++) {
					NativeNodeList curList = currentRoot.getChildNodes();
					if (curList.item(k).getNodeName().equalsIgnoreCase("treeStructures")) {
						defaultTreeStructure = new TreeData();
						ArrayList structureSet = parseStructures(curList.elementAt(k).getChildNodes());
						if (structureSet.size() == 1) {
							defaultTreeStructure.setStructure(((FieldData) structureSet.get(0)).getStructure());
							defaultTreeStructure.setData(((FieldData) structureSet.get(0)).getData());
						} else {
							defaultTreeStructure.setStructure(XMLUtils.STRUCTURE_COLLECTION);
							defaultTreeStructure.setData(structureSet);

							String layout = curList.elementAt(k).getAttribute("layout");
							if (layout != null)
								defaultTreeStructure.setStyle(layout);
						}
						treeData.setDefaultStructure(defaultTreeStructure);
					}
				}
			} else if (curNodeName.equalsIgnoreCase("root")) {
				treeData.addTreeRoot(processRoot(currentRoot, defaultTreeStructure));
			}
		}

		return treeData;
	}

}
