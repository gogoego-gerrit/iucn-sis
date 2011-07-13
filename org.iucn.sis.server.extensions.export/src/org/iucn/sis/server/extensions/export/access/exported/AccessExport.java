//package org.iucn.sis.server.extensions.export.access.exported;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.Reader;
//import java.io.StringWriter;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import javax.naming.NamingException;
//
//import org.gogoego.api.plugins.GoGoEgo;
//import org.iucn.sis.server.api.application.SIS;
//import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
//import org.iucn.sis.server.api.io.WorkingSetIO;
//import org.iucn.sis.server.api.utils.DocumentUtils;
//import org.iucn.sis.server.api.utils.FileZipper;
//import org.iucn.sis.server.api.utils.ServerPaths;
//import org.iucn.sis.server.api.utils.TaxonomyDocUtils;
//import org.iucn.sis.server.extensions.export.ExportApplication;
//import org.iucn.sis.shared.api.assessments.PublishedAssessmentsComparator;
//import org.iucn.sis.shared.api.models.Assessment;
//import org.iucn.sis.shared.api.models.AssessmentType;
//import org.iucn.sis.shared.api.models.Infratype;
//import org.iucn.sis.shared.api.models.Reference;
//import org.iucn.sis.shared.api.models.Taxon;
//import org.iucn.sis.shared.api.models.TaxonLevel;
//import org.iucn.sis.shared.api.models.User;
//import org.iucn.sis.shared.api.models.WorkingSet;
//import org.iucn.sis.shared.api.utils.FieldParser;
//import org.iucn.sis.shared.api.utils.FormattingStripper;
//import org.iucn.sis.shared.api.utils.XMLUtils;
//import org.restlet.Context;
//import org.restlet.Uniform;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//
//import com.solertium.db.CInteger;
//import com.solertium.db.CString;
//import com.solertium.db.Column;
//import com.solertium.db.ConversionException;
//import com.solertium.db.DBException;
//import com.solertium.db.DBSessionFactory;
//import com.solertium.db.ExecutionContext;
//import com.solertium.db.Row;
//import com.solertium.db.StringLiteral;
//import com.solertium.db.SystemExecutionContext;
//import com.solertium.db.query.ExperimentalSelectQuery;
//import com.solertium.db.query.InsertQuery;
//import com.solertium.lwxml.factory.NativeDocumentFactory;
//import com.solertium.lwxml.shared.NativeDocument;
//import com.solertium.util.ClasspathResources;
//import com.solertium.util.ElementCollection;
//import com.solertium.util.NodeCollection;
//import com.solertium.util.TagFilter;
//import com.solertium.util.TagFilter.Tag;
//import com.solertium.util.restlet.authorization.base.Structure;
//import com.solertium.vfs.NotFoundException;
//import com.solertium.vfs.VFS;
//import com.solertium.vfs.provider.FileVFS;
//
//public class AccessExport implements Runnable {
//
//	private class TrivialAssessment {
//
//		FieldParser fieldParser = new FieldParser();
//
//		/**
//		 * For draft assessments...
//		 * 
//		 * @param taxId
//		 */
//		public TrivialAssessment(final int taxId) {
//			if (exportDraftAssessments) {
//				AssessmentFilterHelper helper = new AssessmentFilterHelper(session, filter);
//
//				for (Assessment assessment : helper.getAssessments(taxId)) {
//					try {
//						process(assessment);
//					} catch (DBException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (Throwable e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//
//		/**
//		 * For published assessments...
//		 * 
//		 * @param id
//		 * @param taxId
//		 */
//		public TrivialAssessment(final String id, final int taxId) {
//			try {
//				// final String fn = "/browse/assessments/" +
//				// FilenameStriper.getIDAsStripedPath(id) + ".xml";
//				// final Document d = DocumentUtils.getVFSFileAsDocument(fn,
//				// SISContainerApp.getStaticVFS());
//				// if (d == null) {
//				// System.err.println("Could not open assessment " + fn);
//				// } else {
//				// process(id, taxId, d);
//				// }
//				process(SIS.get().getAssessmentIO().readAssessment(Integer.valueOf(id)));
//			} catch (final DBException ix) {
//				System.err.println("DB exception parsing assessment " + id);
//			}
//		}
//
//		public TrivialAssessment(final String id, final int taxId, final Document d) {
//			try {
//				process(id, taxId, d);
//			} catch (final DBException ix) {
//				System.err.println("DB exception parsing assessment " + id);
//			}
//		}
//
//		private void addClassification(final String id, String uid, final String scheme, final Element selected)
//				throws DBException {
//			// populate id and lookup column
//
//			final Row clsRow;
//			try {
//				clsRow = ec.getRow(scheme);
//
//				if (clsRow == null) {
//					System.out.println("No table found for classification scheme " + scheme);
//					return;
//				}
//			} catch (Exception e) {
//				System.out.println("No table found for classification scheme " + scheme);
//				return;
//			}
//
//			clsRow.get(0).setObject(id);
//			clsRow.get("uid").setString(uid);
//			final Column lkup = clsRow.get(2);
//			// System.out.println("Lookup column for "+scheme+" is
//			// "+lkup.getLocalName());
//			lkup.setString(selected.getAttribute("id"));
//			// populate remainder
//			int col = 3;
//			for (final Element structure : ElementCollection.childElementsByTagName(selected, "structure")) {
//				try {
//					final Column currentColumn = clsRow.get(col);
//					final String s = structure.getTextContent();
//					if (s != null && !"".equals(s)) {
//						currentColumn.setString(s);
//					}
//					col++;
//				} catch (final IndexOutOfBoundsException end) {
//					if (!"Threats".equals(scheme))
//						break;
//					// special handling for threats+stresses
//					addStresses(id, uid, selected);
//				} catch (final NumberFormatException badData) {
//					badData.printStackTrace();
//					System.out.println("ERROR parsing int from " + structure.getTextContent() + " for class scheme "
//							+ scheme);
//				}
//			}
//			// commit to database
//			try {
//				final InsertQuery iqr = new InsertQuery(scheme, clsRow);
//				ec.doUpdate(iqr);
//			} catch (final DBException dbx) {
//				dbx.printStackTrace();
//			}
//		}
//
//		private void addReference(final String id, String uid, final String fieldName, final Element reference)
//				throws DBException {
//			final String referenceHash = reference.getAttribute("id");
//			Integer numericReferenceId = seenReferences.get(referenceHash);
//
//			NativeDocument doc = NativeDocumentFactory.newNativeDocument();
//			doc.parse(org.iucn.sis.server.api.utils.DocumentUtils.serializeNodeToString(reference));
//			Reference ref = Reference.fromXML(doc.getDocumentElement());
//
//			if (numericReferenceId == null) { // not already seen
//				numericReferenceId = ++highReferenceId;
//				seenReferences.put(referenceHash, numericReferenceId);
//				final Row refRow = ec.getRow("reference");
//				refRow.get("id").setObject(numericReferenceId);
//				refRow.get("type").setString(reference.getAttribute("type"));
//				for (final Element e : ElementCollection.childElements(reference)) {
//					final Column c = refRow.get(e.getAttribute("name"));
//					if (c != null) {
//						final String s = e.getTextContent();
//						if (s != null && !"".equals(s)) {
//							c.setString(s);
//						}
//					}
//				}
//
//				final Column c = refRow.get("citation");
//				if (c.getString() == null || c.getString().equals("")) {
//					if (ref.getType().equalsIgnoreCase("rldb"))
//						ref.setType("Book");
//
//					ref.generateCitation();
//					c.setString(ref.getCitation());
//				}
//
//				try {
//					final InsertQuery iq = new InsertQuery("reference", refRow);
//					ec.doUpdate(iq);
//				} catch (final DBException dbx) {
//					dbx.printStackTrace();
//				}
//			}
//			final Row asmRefRow = ec.getRow("assessment_reference");
//			asmRefRow.get("asm_id").setObject(id);
//			asmRefRow.get("uid").setString(uid);
//			asmRefRow.get("field").setString(fieldName);
//			asmRefRow.get("ref_id").setObject(numericReferenceId);
//			try {
//				final InsertQuery iqr = new InsertQuery("assessment_reference", asmRefRow);
//				ec.doUpdate(iqr);
//			} catch (final DBException dbx) {
//				dbx.printStackTrace();
//			}
//		}
//
//		private void addStresses(final String id, String uid, final Element selected) throws DBException {
//			final ElementCollection structures = ElementCollection.childElementsByTagName(selected, "structure");
//			try {
//				final int stressCount = Integer.parseInt(structures.get(STRESS_COUNT_OFFSET).getTextContent());
//				final int threat = Integer.parseInt(selected.getAttribute("id"));
//				if (stressCount < 1)
//					return;
//				// System.out.println("Inserting "+stressCount+" stresses;
//				// threat "+threat+"; asm "+id);
//				for (int i = STRESS_COUNT_OFFSET + 1; i <= STRESS_COUNT_OFFSET + stressCount; i++) {
//					final Row strRow = ec.getRow("Stresses");
//					strRow.get("asm_id").setObject(id);
//					strRow.get("uid").setString(uid);
//					strRow.get("threat_id").setObject(threat);
//					strRow.get("stress_id").setString(structures.get(i).getTextContent());
//					final InsertQuery iqr = new InsertQuery("Stresses", strRow);
//					ec.doUpdate(iqr);
//				}
//			} catch (final Exception notVeryUnexpected) {
//				notVeryUnexpected.printStackTrace();
//			}
//		}
//
//		private void buildIndexedLookup(String tableName, String indexName, String descName, Object[] options,
//				int indexOffset) throws DBException {
//			Row prototype = new Row();
//			prototype.add(new CInteger(indexName, Integer.valueOf(0)));
//			prototype.add(new CString(descName, "sample description"));
//			ec.createTable(tableName, prototype);
//
//			for (int i = 0; i < options.length; i++) {
//				Row r = new Row();
//				r.add(new CInteger(indexName, Integer.valueOf(i + indexOffset)));
//				r.add(new CString(descName, options[i].toString()));
//
//				InsertQuery iq = new InsertQuery(tableName, r);
//				ec.doUpdate(iq);
//			}
//		}
//
//		private void buildStructureLookup(String fieldName, Structure struct) throws DBException {
//			// FIXME
//			// if (struct instanceof SISClassificationSchemeStructure) {
//			// SISClassificationSchemeStructure s =
//			// (SISClassificationSchemeStructure) struct;
//			// parseLookupFromDisplay("Stresses", s.getScheme());
//			// }
//			// if (struct instanceof SISStructureCollection) {
//			// SISStructureCollection s = (SISStructureCollection) struct;
//			//
//			// for (int i = 0; i < s.getStructures().size(); i++) {
//			// Structure cur = (Structure) s.getStructures().get(i);
//			//
//			// if (cur.getDescription() != null &&
//			// !cur.getDescription().equals("")) {
//			// String cleanDesc = cur.getDescription().replaceAll("\\s",
//			// "").replaceAll("\\W", "");
//			// try {
//			// if ("_lookup_".length() + fieldName.length() + cleanDesc.length()
//			// < 64)
//			// buildStructureLookup(fieldName + cleanDesc, cur);
//			// else
//			// buildStructureLookup(fieldName + i, cur);
//			// } catch (DBException duplicateDescriptionProbably) {
//			// buildStructureLookup(fieldName + i, cur);
//			// }
//			// } else
//			// buildStructureLookup(fieldName + i, cur);
//			// }
//			// } else if (struct instanceof SISOneToMany) {
//			// SISOneToMany s = (SISOneToMany) struct;
//			// buildStructureLookup(fieldName,
//			// DisplayDataProcessor.processDisplayStructure(s.getDefaultStructureData()));
//			// } else if (struct instanceof SISRelatedStructures) {
//			// SISRelatedStructures s = (SISRelatedStructures) struct;
//			//
//			// buildStructureLookup(fieldName, s.getDominantStructure());
//			//
//			// for (int i = 0; i < s.getDependantStructures().size(); i++) {
//			// Structure cur = (Structure) s.getDependantStructures().get(i);
//			//
//			// if (cur.getDescription() != null &&
//			// !cur.getDescription().equals("")) {
//			// String cleanDesc = cur.getDescription().replaceAll("\\s",
//			// "").replaceAll("\\W", "");
//			// try {
//			// if ("_lookup_".length() + fieldName.length() + cleanDesc.length()
//			// < 64)
//			// buildStructureLookup(fieldName + cleanDesc, cur);
//			// else
//			// buildStructureLookup(fieldName + i, cur);
//			// } catch (DBException duplicateDescriptionProbably) {
//			// buildStructureLookup(fieldName + i, cur);
//			// }
//			// } else
//			// buildStructureLookup(fieldName + i, cur);
//			// }
//			// } else if (struct instanceof SISMultiSelect || struct instanceof
//			// SISSelect) {
//			// String tableName = "_lookup_" + fieldName;
//			// String indexName = "index";
//			// String descName = "description";
//			//
//			// ArrayList options = (ArrayList) struct.getConstructionData();
//			//
//			// if( options.get(0) instanceof ArrayList )
//			// buildIndexedLookup(tableName, indexName, descName,
//			// ((ArrayList)options.get(0)).toArray(), 1);
//			// else
//			// buildIndexedLookup(tableName, indexName, descName,
//			// options.toArray(), 1);
//			// } else if (struct instanceof SISThreatStructure) {
//			// String[] timing = new String[] { "Past, Unlikely to Return",
//			// "Ongoing", "Future", "Unknown",
//			// "Past, Likely to Return" };
//			// String[] scope = new String[] { "Whole (>90%)",
//			// "Majority (50-90%)", "Minority (<50%)", "Unknown" };
//			// String[] severity = new String[] { "Very Rapid Declines",
//			// "Rapid Declines",
//			// "Slow, Significant Declines", "Causing/Could cause fluctuations",
//			// "Negligible declines",
//			// "No decline", "Unknown" };
//			//
//			// buildIndexedLookup("_lookup_ThreatTiming", "index",
//			// "description", timing, 1);
//			// buildIndexedLookup("_lookup_ThreatScope", "index", "description",
//			// scope, 1);
//			// buildIndexedLookup("_lookup_ThreatSeverity", "index",
//			// "description", severity, 1);
//			// } else if (struct instanceof UseTrade) {
//			// UseTrade useTrade = (UseTrade) struct;
//			//
//			// buildIndexedLookup("_lookup_UseTradePurpose", "index",
//			// "description", useTrade.getPurposeOptions(), 1);
//			// buildIndexedLookup("_lookup_UseTradeSource", "index",
//			// "description", useTrade.getSourceOptions(), 1);
//			// buildIndexedLookup("_lookup_UseTradeFormRemoved", "index",
//			// "description", useTrade.getFormRemovedOptions(), 1);
//			// buildIndexedLookup("_lookup_UseTradeUnits", "index",
//			// "description", useTrade.getUnitsOptions(), 0);
//			// } else if (struct instanceof SISLivelihoods) {
//			// SISLivelihoods l = (SISLivelihoods) struct;
//			//
//			// buildIndexedLookup("_lookup_LivelihoodsScale", "index",
//			// "description", l.getScaleOptions(), 1);
//			// buildIndexedLookup("_lookup_LivelihoodsUnits", "index",
//			// "description", l.getUnitsOptions(), 1);
//			// buildIndexedLookup("_lookup_LivelihoodsHumanReliance", "index",
//			// "description", l.getHumanRelianceOptions(), 1);
//			// buildIndexedLookup("_lookup_LivelihoodsGenderAge", "index",
//			// "description", l.getByGenderAgeOptions(), 1);
//			// buildIndexedLookup("_lookup_LivelihoodsSocioEcon", "index",
//			// "description", l.getBySocioEconOptions(), 1);
//			// buildIndexedLookup("_lookup_LivelihoodsPercentPopBenefit",
//			// "index", "description",
//			// l.getPercentPopulationBenefitingOptions(), 1);
//			// buildIndexedLookup("_lookup_LivelihoodsPercentConsume", "index",
//			// "description", l.getPercentConsumptionOptions(), 1);
//			// buildIndexedLookup("_lookup_LivelihoodsPercentIncome", "index",
//			// "description", l.getPercentIncomeOptions(), 1);
//			// } else if (struct instanceof SISCategoryAndCriteria) {
//			// buildIndexedLookup("_lookup_CriteriaVersions", "index",
//			// "crit_version", new String[] { "3.1", "2.3",
//			// "Earlier Version" }, 0);
//			// }
//		}
//
//		private void captureBasic(final Row row, final Element n, final String... desired) {
//			for (final String d : desired) {
//				for (final Element sub : new ElementCollection(n.getElementsByTagName(d))) {
//					final String s = sub.getTextContent();
//					if (s != null && !"".equals(s)) {
//						// System.out.println(" basic "+d+" = "+s);
//						row.get(d).setString(ec.formatLiteral(new StringLiteral(s)));
//					}
//				}
//			}
//		}
//
//		// private void createLookupTable(String fieldName) throws DBException {
//		// if (lookupTables.containsKey(fieldName))
//		// return;
//		// else
//		// lookupTables.put(fieldName, new Boolean(true));
//		//
//		// String url = ServerPaths.getFieldURL(fieldName);
//		//
//		// if (!SISContainerApp.getStaticVFS().exists(url)) {
//		// System.out.println("Could not find field definition for " + url);
//		// return;
//		// }
//		//
//		// NativeDocument fdoc = SISContainerApp.newNativeDocument(null);
//		// fdoc.parse(DocumentUtils.getVFSFileAsString(url,
//		// SISContainerApp.getStaticVFS()));
//		//
//		// Display f = fieldParser.parseField(fdoc);
//		// parseLookupFromDisplay(fieldName, f);
//		// }
//
//		private String generateTextFromUsers(List<User> userList) {
//			StringBuilder text = new StringBuilder();
//			for (int i = 0; i < userList.size(); i++) {
//				text.append(userList.get(i).getCitationName());
//
//				if (i + 1 < userList.size() - 1)
//					text.append(", ");
//
//				else if (i + 1 == userList.size() - 1)
//					text.append(" & ");
//			}
//
//			return text.toString();
//		}
//
//		// private void insertClassSchemeLookupRow(String fieldName, String
//		// parentID, ClassificationScheme scheme,
//		// TreeDataRow curRow, int level) throws DBException {
//		// Row r;
//		// String curCode = (String) curRow.getDisplayId();
//		// String curDesc = (String) scheme.getCodeToDesc().get(curCode);
//		// String curLevelID = (String) scheme.getCodeToLevelID().get(curCode);
//		//
//		// // If it's not a region in CountryOccurrence, do your stuff
//		// if (!(fieldName.equals(CanonicalNames.CountryOccurrence) &&
//		// curCode.length() > 10)) {
//		// r = new Row();
//		// r.add(new CString("id", curCode));
//		// r.add(new CString("parentID", parentID));
//		// r.add(new CInteger("level", level));
//		// r.add(new CString("ref", curLevelID));
//		// r.add(new CString("description", curDesc));
//		//
//		// InsertQuery i = new InsertQuery("_scheme_lookup_" + fieldName, r);
//		// ec.doUpdate(i);
//		//
//		// for (Object obj : curRow.getChildren())
//		// insertClassSchemeLookupRow(fieldName, curCode, scheme, (TreeDataRow)
//		// obj, level + 1);
//		// } else
//		// for (Object obj : curRow.getChildren())
//		// insertClassSchemeLookupRow(fieldName, "(root)", scheme, (TreeDataRow)
//		// obj, level);
//		// }
//
//		// private void parseLookupFromDisplay(String fieldName, Display f)
//		// throws DBException {
//		// if (f instanceof ClassificationScheme) {
//		// Row r = new Row();
//		// r.add(new CString("id", "sampleID"));
//		// r.add(new CString("parentID", "sampleID"));
//		// r.add(new CInteger("level", 10));
//		// r.add(new CString("ref", "1.1.1.1"));
//		// r.add(new CString("description", "sample description"));
//		//
//		// ec.createTable("_scheme_lookup_" + fieldName, r);
//		//
//		// // Generate classification scheme lookup table
//		// ClassificationScheme scheme = (ClassificationScheme) f;
//		// for (Object obj : scheme.getTreeData().getTreeRoots())
//		// insertClassSchemeLookupRow(fieldName, "(root)", scheme, (TreeDataRow)
//		// obj, 0);
//		//
//		// // Generate classification scheme's default structure lookup
//		// // table
//		// Structure defStructure = scheme.generateDefaultStructure();
//		// buildStructureLookup(fieldName + "Structure", defStructure);
//		// } else {
//		// // Generate a field's lookup table
//		// Field field = (Field) f;
//		// for (Object obj : field.getStructures()) {
//		// Structure struct = (Structure) obj;
//		// buildStructureLookup(fieldName, struct);
//		// }
//		// }
//		// }
//
//		private void process(Assessment assessment) throws DBException {
//			Document doc = org.iucn.sis.server.api.utils.DocumentUtils.createDocumentFromString(assessment.toXML());
//
//			final String validationStatus = ((Element) doc.getDocumentElement()
//					.getElementsByTagName("basicInformation").item(0)).getElementsByTagName("validationStatus").item(0)
//					.getTextContent();
//			final String uid = assessment.getAssessmentID() + "_" + assessment.getAssessmentType().getName();
//
//			final Row asmRow = ec.getRow("assessment");
//			asmRow.get("uid").setObject(uid);
//			asmRow.get("id").setObject(assessment.getAssessmentID());
//			asmRow.get("tax_id").setObject(assessment.getTaxon().getId());
//			asmRow.get("validationStatus").setString(validationStatus);
//
//			for (final Element e : ElementCollection.childElements(doc.getDocumentElement())) {
//				if ("field".equals(e.getNodeName())) {
//					// fields
//					final String fieldName = e.getAttribute("id");
//					final Column c = asmRow.get(fieldName);
//					// FIXME
//					// try {
//					// createLookupTable(fieldName);
//					// } catch (Exception lookup) {
//					// lookup.printStackTrace();
//					// }
//
//					ElementCollection structures = ElementCollection.childElementsByTagName(e, "structure");
//					if ("RegionInformation".equals(fieldName)) {
//						String isRegional = Boolean.valueOf(structures.get(0).getTextContent().contains("-1"))
//								.toString();
//						String regions = structures.get(0).getTextContent();
//						String endemic = structures.get(1).getTextContent();
//
//						asmRow.get("is_regional").setString(isRegional);
//						asmRow.get("is_endemic").setString(endemic);
//
//						String[] ids;
//						if (regions.contains(","))
//							ids = regions.split(",");
//						else
//							ids = new String[] { regions };
//
//						for (String curID : ids) {
//							Row regRow = ec.getRow("RegionInformation");
//							regRow.get("asm_id").setObject(assessment.getAssessmentID());
//							regRow.get("region_id").setObject(curID);
//							regRow.get("uid").setString(uid);
//
//							final InsertQuery iq = new InsertQuery("RegionInformation", regRow);
//							ec.doUpdate(iq);
//						}
//
//					} else if ("UseTradeDetails".equals(fieldName) || "Livelihoods".equals(fieldName)) {
//						int countOffset = "Livelihoods".equals(fieldName) ? 1 : 0;
//						int count = Integer.valueOf(structures.get(countOffset).getTextContent());
//						if (count > 0) {
//							int total = (structures.size() - countOffset - 1) / count;
//
//							for (int i = 0; i < count; i++) {
//								Row row = ec.getRow(fieldName);
//								row.get("asm_id").setObject(assessment.getAssessmentID());
//								row.get("uid").setString(uid);
//
//								for (int j = 0; j < total; j++) {
//									try {
//										row.get(j + 2).setObject(
//												structures.get((j + 1) + (total * i) + countOffset).getTextContent());
//									} catch (ConversionException e1) {
//										try {
//											if (structures.get((j + 1) + (total * i) + countOffset).getTextContent()
//													.equals(""))
//												row.get(j + 2).setObject(0);
//											else
//												row.get(j + 2).setObject(
//														Integer.valueOf(structures.get(
//																(j + 1) + (total * i) + countOffset).getTextContent()));
//										} catch (NumberFormatException e2) {
//											try {
//												row.get(j + 2).setObject(
//														Boolean.valueOf(structures.get(
//																(j + 1) + (total * i) + countOffset).getTextContent()));
//											} catch (ConversionException e3) {
//												if (structures.get((j + 1) + (total * i) + countOffset)
//														.getTextContent().equals("false")) {
//													countOffset++; // Wayward
//													// false.
//													// Gr.
//													// Increment
//													// the
//													// offset
//													// and try
//													// again.
//													j = -1;
//												} else
//													System.out.println("COULD NOT SET DATA for field " + fieldName
//															+ " at index " + ((j + 1) + (total * i) + countOffset));
//											}
//										}
//									}
//								}
//
//								try {
//									final InsertQuery iq = new InsertQuery(fieldName, row);
//									ec.doUpdate(iq);
//								} catch (final DBException dbx) {
//									dbx.printStackTrace();
//								}
//							}
//						}
//					} else if (c != null) { // just put it in the column
//						boolean narrative = fieldName.endsWith("Documentation")
//								|| fieldName.equalsIgnoreCase("RedListRationale");
//
//						if ("RedListEvaluators".equals(fieldName) || "RedListContributors".equals(fieldName)
//								|| "RedListAssessors".equals(fieldName)) {
//							if (structures.size() > 0) {
//								String s = structures.get(0).getTextContent();
//								if (s == null || s.equals("")) {
//									List<User> userList = new ArrayList<User>();
//									for (int i = 2; i < structures.size(); i++) {
//										// START AT 2 - index 1 is now just the
//										// total number of users...
//										String curID = structures.get(i).getTextContent();
//										if (!curID.equals("0")) {
//											if (users.containsKey(curID))
//												userList.add(users.get(curID));
//											else
//												System.out.println("Could not find user with ID " + curID);
//										}
//									}
//
//									s = generateTextFromUsers(userList);
//								}
//
//								c.setString(s);
//							}
//						} else {
//							for (final Element structure : structures) {
//								final String s = structure.getTextContent();
//								if (s != null && !"".equals(s)) {
//									if (narrative)// TODO: TEST THIS!
//										c.setString(FormattingStripper.stripText(XMLUtils.cleanFromXML(s)));
//									else
//										c.setString(s);
//									break;
//								}
//							}
//						}
//					} else { // complex structure like RedListCriteria
//						// System.out.println("Trying to find table for " +
//						// fieldName);
//						try {
//							final Row externRow = ec.getRow(fieldName);
//							externRow.get(0).setObject(assessment.getAssessmentID());
//							externRow.get("uid").setString(uid);
//							int count = 2;
//							for (final Element structure : structures) {
//								final Column externCol = externRow.get(count++);
//								final String s = structure.getTextContent();
//								if (s != null && !"".equals(s)) {
//									externCol.setString(s);
//								}
//							}
//							try {
//								final InsertQuery iq = new InsertQuery(fieldName, externRow);
//								ec.doUpdate(iq);
//							} catch (final DBException dbx) {
//								dbx.printStackTrace();
//							}
//						} catch (Exception e2) {
//							// System.out.println("Failed finding table for " +
//							// fieldName);
//							// No table found. It's cool.
//						}
//					}
//					// look for references
//					for (final Element reference : ElementCollection.childElementsByTagName(e, "reference")) {
//						addReference(assessment.getAssessmentID() + "", uid, fieldName, reference);
//					}
//				} else if ("classificationScheme".equals(e.getNodeName())) {
//					final String scheme = e.getAttribute("id");
//					// FIXME
//					// try {
//					// createLookupTable(scheme);
//					// } catch (Exception lookup) {
//					// lookup.printStackTrace();
//					// }
//
//					for (final Element selected : ElementCollection.childElementsByTagName(e, "selected"))
//						addClassification(assessment.getAssessmentID() + "", uid, scheme, selected);
//				} else if ("globalReferences".equals(e.getNodeName())) {
//					for (final Element reference : ElementCollection.childElementsByTagName(e, "reference"))
//						addReference(assessment.getAssessmentID() + "", uid, "global", reference);
//				} else if ("basicInformation".equals(e.getNodeName())) {
//					captureBasic(asmRow, e, "dateModified", "dateAdded", "dateFinalized", "isDone", "isHistorical",
//							"source", "sourceDate", "validationStatus");
//				}
//			}
//			try {
//				if (asmRow.get("is_regional") == null)
//					asmRow.get("is_regional").setString(ec.formatLiteral(new StringLiteral("true")));
//
//				final InsertQuery iq = new InsertQuery("assessment", asmRow);
//				// System.out.println("ASSESSMENT insert SQL: " +
//				// iq.getSQL(ec.getDBSession()));
//				ec.doUpdate(iq);
//			} catch (final DBException dbx) {
//				final InsertQuery iq = new InsertQuery("assessment", asmRow);
//				System.out.println("ASSESSMENT insert SQL: " + iq.getSQL(ec.getDBSession()));
//				((SQLException) dbx.getCause()).getNextException().printStackTrace();
//				dbx.printStackTrace();
//			}
//		}
//	}
//
//	// private void process(final String id, final int taxId, final Document
//	// doc) throws DBException {
//	// final String validationStatus =
//	// ((Element)doc.getDocumentElement().getElementsByTagName(
//	// "basicInformation").item(0)).getElementsByTagName("validationStatus").item(0).getTextContent();
//	// final String uid = id + "_" + validationStatus;
//	//			
//	// final Row asmRow = ec.getRow("assessment");
//	// asmRow.get("uid").setObject(uid);
//	// asmRow.get("id").setObject(id);
//	// asmRow.get("tax_id").setObject(taxId);
//	// asmRow.get("validationStatus").setString(validationStatus);
//	// for (final Element e :
//	// ElementCollection.childElements(doc.getDocumentElement())) {
//	// if ("field".equals(e.getNodeName())) {
//	// // fields
//	// final String fieldName = e.getAttribute("id");
//	// final Column c = asmRow.get(fieldName);
//	// try {
//	// createLookupTable(fieldName);
//	// } catch (Exception lookup) {
//	// lookup.printStackTrace();
//	// }
//	//
//	// ElementCollection structures =
//	// ElementCollection.childElementsByTagName(e, "structure");
//	// if ("RegionInformation".equals(fieldName)) {
//	// String isRegional =
//	// Boolean.valueOf(structures.get(0).getTextContent().contains("-1")).toString();
//	// String regions = structures.get(0).getTextContent();
//	// String endemic = structures.get(1).getTextContent();
//	//						
//	// asmRow.get("is_regional").setString(isRegional);
//	// asmRow.get("is_endemic").setString(endemic);
//	//
//	// String[] ids;
//	// if (regions.contains(","))
//	// ids = regions.split(",");
//	// else
//	// ids = new String[] { regions };
//	//
//	// for (String curID : ids) {
//	// Row regRow = ec.getRow("RegionInformation");
//	// regRow.get("asm_id").setObject(id);
//	// regRow.get("region_id").setObject(curID);
//	// regRow.get("uid").setString(uid);
//	//
//	// final InsertQuery iq = new InsertQuery("RegionInformation", regRow);
//	// ec.doUpdate(iq);
//	// }
//	//						
//	// } else if ("UseTradeDetails".equals(fieldName) ||
//	// "Livelihoods".equals(fieldName)) {
//	// int countOffset = "Livelihoods".equals(fieldName) ? 1 : 0;
//	// int count =
//	// Integer.valueOf(structures.get(countOffset).getTextContent());
//	// if( count > 0 ) {
//	// int total = (structures.size()-countOffset-1) / count;
//	//							
//	// for( int i = 0; i < count; i++ ) {
//	// Row row = ec.getRow(fieldName);
//	// row.get("asm_id").setObject(id);
//	// row.get("uid").setString(uid);
//	//								
//	// for( int j = 0; j < total; j++ ) {
//	// try {
//	// row.get(j+2).setObject(structures.get( (j+1)+(total*i)+countOffset
//	// ).getTextContent());
//	// } catch (ConversionException e1) {
//	// try {
//	// if( structures.get( (j+1)+(total*i)+countOffset
//	// ).getTextContent().equals("") )
//	// row.get(j+2).setObject(0);
//	// else
//	// row.get(j+2).setObject(Integer.valueOf(structures.get(
//	// (j+1)+(total*i)+countOffset ).getTextContent()));
//	// } catch (NumberFormatException e2) {
//	// try {
//	// row.get(j+2).setObject(Boolean.valueOf(structures.get(
//	// (j+1)+(total*i)+countOffset ).getTextContent()));
//	// } catch (ConversionException e3) {
//	// if( structures.get( (j+1)+(total*i)+countOffset
//	// ).getTextContent().equals("false") ) {
//	// countOffset++; //Wayward false. Gr. Increment the offset and try again.
//	// j = -1;
//	// } else
//	// System.out.println("COULD NOT SET DATA for field " + fieldName +
//	// " at index " + ((j+1)+(total*i)+countOffset));
//	// }
//	// }
//	// }
//	// }
//	//								
//	// try {
//	// final InsertQuery iq = new InsertQuery(fieldName, row);
//	// ec.doUpdate(iq);
//	// } catch (final DBException dbx) {
//	// dbx.printStackTrace();
//	// }
//	// }
//	// }
//	// } else if (c != null) { // just put it in the column
//	// boolean narrative = fieldName.endsWith("Documentation")
//	// || fieldName.equalsIgnoreCase("RedListRationale");
//	//						
//	// if( "RedListEvaluators".equals(fieldName) ||
//	// "RedListContributors".equals(fieldName) ||
//	// "RedListAssessors".equals(fieldName)) {
//	// if( structures.size() > 0 ) {
//	// String s = structures.get(0).getTextContent();
//	// if( s == null || s.equals("") ) {
//	// List<User> userList = new ArrayList<User>();
//	// for (int i = 2; i < structures.size(); i++) {
//	// //START AT 2 - index 1 is now just the total number of users...
//	// String curID = structures.get(i).getTextContent();
//	// if( !curID.equals("0") ) {
//	// if( users.containsKey(curID) )
//	// userList.add(users.get(curID));
//	// else
//	// System.out.println("Could not find user with ID " + curID);
//	// }
//	// }
//	//									
//	// s = generateTextFromUsers(userList);
//	// }
//	//								
//	// c.setString(s);
//	// }
//	// } else {
//	// for (final Element structure : structures) {
//	// final String s = structure.getTextContent();
//	// if (s != null && !"".equals(s)) {
//	// if (narrative)// TODO: TEST THIS!
//	// c.setString(FormattingStripper.stripText(XMLUtils.cleanFromXML(s)));
//	// else
//	// c.setString(s);
//	// break;
//	// }
//	// }
//	// }
//	// } else { // complex structure like RedListCriteria
//	// // System.out.println("Trying to find table for " +
//	// // fieldName);
//	// try {
//	// final Row externRow = ec.getRow(fieldName);
//	// externRow.get(0).setObject(id);
//	// externRow.get("uid").setString(uid);
//	// int count = 2;
//	// for (final Element structure : structures) {
//	// final Column externCol = externRow.get(count++);
//	// final String s = structure.getTextContent();
//	// if (s != null && !"".equals(s)) {
//	// externCol.setString(s);
//	// }
//	// }
//	// try {
//	// final InsertQuery iq = new InsertQuery(fieldName, externRow);
//	// ec.doUpdate(iq);
//	// } catch (final DBException dbx) {
//	// dbx.printStackTrace();
//	// }
//	// } catch (Exception e2) {
//	// // System.out.println("Failed finding table for " +
//	// // fieldName);
//	// // No table found. It's cool.
//	// }
//	// }
//	// // look for references
//	// for (final Element reference :
//	// ElementCollection.childElementsByTagName(e, "reference"))
//	// addReference(id, uid, fieldName, reference);
//	// } else if ("classificationScheme".equals(e.getNodeName())) {
//	// final String scheme = e.getAttribute("id");
//	// try {
//	// createLookupTable(scheme);
//	// } catch (Exception lookup) {
//	// lookup.printStackTrace();
//	// }
//	//
//	// for (final Element selected : ElementCollection.childElementsByTagName(e,
//	// "selected"))
//	// addClassification(id, uid, scheme, selected);
//	// } else if ("globalReferences".equals(e.getNodeName())) {
//	// for (final Element reference :
//	// ElementCollection.childElementsByTagName(e, "reference"))
//	// addReference(id, uid, "global", reference);
//	// } else if ("basicInformation".equals(e.getNodeName())) {
//	// captureBasic(asmRow, e, "dateModified", "dateAdded", "dateFinalized",
//	// "isDone", "isHistorical",
//	// "source", "sourceDate", "validationStatus");
//	// }
//	// }
//	// try {
//	// if( asmRow.get("is_regional") == null )
//	// asmRow.get("is_regional").setString(ec.formatLiteral(new
//	// StringLiteral("true")));
//	//				
//	// final InsertQuery iq = new InsertQuery("assessment", asmRow);
//	// // System.out.println("ASSESSMENT insert SQL: " +
//	// // iq.getSQL(ec.getDBSession()));
//	// ec.doUpdate(iq);
//	// } catch (final DBException dbx) {
//	// final InsertQuery iq = new InsertQuery("assessment", asmRow);
//	// System.out.println("ASSESSMENT insert SQL: " +
//	// iq.getSQL(ec.getDBSession()));
//	// ((SQLException) dbx.getCause()).getNextException().printStackTrace();
//	// dbx.printStackTrace();
//	// }
//	// }
//	// }
//
//	/**
//	 * This is a small-memory representation of a taxonomic node used during the
//	 * export. Creating it triggers the parse of its own taxon file and,
//	 * recursively, the associated assessments, plus the insertion in the
//	 * database of relevant rows.
//	 */
//	private class TrivialNode implements TagFilter.Listener {
//		TagFilter tf;
//		StringWriter taxw;
//		StringWriter asmw;
//		private String status = "";
//		private String deprecated = "";
//		private final int depth;
//		private final int id;
//		private final TrivialNode parent;
//		private final Taxon me;
//
//		private String hybrid;
//		private String taxonomicAuthority;
//		private boolean assessed = false;
//		private boolean export;
//
//		public TrivialNode(final int id, final String name, final TrivialNode parent, final int depth, boolean export) {
//			this.id = id;
//			this.parent = parent;
//			this.depth = depth;
//			this.export = export;
//
//			this.me = SIS.get().getTaxonIO().readTaxon(id);
//
//			try {
//				final Reader r = vfs.getReader(ServerPaths.getURLForTaxa("" + id));
//				tf = new TagFilter(r);
//				tf.shortCircuitClosingTags = false;
//				tf.registerListener(this);
//				tf.parse();
//
//				// Do draft assessment
//				if (export && exportDraftAssessments) {
//					if (hasDraftAssessment()) {
//						populateAssessed();
//						new TrivialAssessment(this.id);
//					}
//				}
//			} catch (final NotFoundException nf) {
//				System.err.println("No taxon file found for " + this.id);
//			} catch (final IOException ix) {
//				System.err.println("IO exception parsing " + id);
//			}
//			try {
//				String pid = "null";
//				if (parent != null)
//					pid = "" + parent.getId();
//				final String s = "insert into taxonomy (id,parent_tax_id,name,"
//						+ "taxonomic_authority,status,level,hybrid)" + " values (" + id + "," + pid + ","
//						+ ec.formatLiteral(new StringLiteral(name)) + ","
//						+ ec.formatLiteral(new StringLiteral(taxonomicAuthority)) + ","
//						+ ec.formatLiteral(new StringLiteral(status)) + ","
//						+ ec.formatLiteral(new StringLiteral(me.getDisplayableLevel())) + ","
//						+ ec.formatLiteral(new StringLiteral(hybrid)) + ")";
//				if (export)
//					ec.doUpdate(s);
//			} catch (final DBException dbx) {
//				dbx.printStackTrace();
//			}
//		}
//
//		private String formatLiteral(String prop) {
//			return ec.formatLiteral(new StringLiteral(prop == null ? "" : prop));
//		}
//
//		public int getDepth() {
//			return depth;
//		}
//
//		public int getId() {
//			return id;
//		}
//
//		public String getName() {
//			return me.getName();
//		}
//
//		public String getFullName() {
//			return me.getFriendlyName();
//		}
//
//		public TrivialNode getParent() {
//			return parent;
//		}
//
//		public String getTaxonomicAuthority() {
//			return taxonomicAuthority;
//		}
//
//		public boolean hasDraftAssessment() {
//			return true;
//		}
//
//		public List<String> interestingTagNames() {
//			if (nodeTagNames != null)
//				return nodeTagNames;
//
//			nodeTagNames = new ArrayList<String>();
//
//			// If we're going to export it, do the interesting stuff
//			// if( export ) {
//			// System.out.println("Adding others...");
//			nodeTagNames.add("taxonomicAuthority");
//			nodeTagNames.add("/taxonomicAuthority");
//			nodeTagNames.add("commonName");
//			nodeTagNames.add("synonym");
//			nodeTagNames.add("assessments");
//			nodeTagNames.add("/assessments");
//			// }
//
//			for (int i = 0; i < TaxonLevel.displayableLevel.length; i++) {
//				String curLevel = Taxon.getDisplayableLevel(i);
//				nodeTagNames.add(curLevel.toLowerCase());
//			}
//
//			return nodeTagNames;
//		}
//
//		private void populateAssessed() {
//			if (assessed)
//				return;
//			else
//				assessed = true;
//
//			Row r = new Row();
//			r.add(new CInteger("tax_id", id));
//			String[] tableNames = new String[] { "kingdom", "phylum", "class", "order", "family", "genus", "species",
//					"infrarank", "infratype", "subpopulation", "friendly_name" };
//
//			for (int i = 0; i <= 5; i++) {
//				// Kingdom -> Species
//				r.add(new CString(tableNames[i], me.getFootprint()[i]));
//			}
//
//			if (me.getTaxonLevel().getLevel() == TaxonLevel.INFRARANK
//					|| me.getTaxonLevel().getLevel() == TaxonLevel.INFRARANK_SUBPOPULATION) {
//				r.add(new CString(tableNames[6], me.getFootprint()[6]));
//				r.add(new CString(tableNames[7], me.getInfratype().getName().equals(Infratype.VARIETY_NAME) ? "var."
//						: "ssp."));
//
//				if (me.getTaxonLevel().getLevel() == TaxonLevel.INFRARANK_SUBPOPULATION) {
//					r.add(new CString(tableNames[8], me.getFootprint()[7]));
//					r.add(new CString(tableNames[9], me.getName()));
//				} else {
//					r.add(new CString(tableNames[8], me.getName()));
//					r.add(new CString(tableNames[9], null));
//				}
//
//			} else if (me.getTaxonLevel().getLevel() == TaxonLevel.SUBPOPULATION) {
//				r.add(new CString(tableNames[6], me.getFootprint()[6]));
//				r.add(new CString(tableNames[7], null));
//				r.add(new CString(tableNames[8], null));
//				r.add(new CString(tableNames[9], me.getName()));
//			} else {
//				r.add(new CString(tableNames[6], me.getName()));
//				r.add(new CString(tableNames[7], null)); // Infrarank == null
//				r.add(new CString(tableNames[8], null)); // Infratype == null
//				r.add(new CString(tableNames[9], null)); // Subpop == null
//			}
//
//			r.add(new CString("friendly_name", getFullName()));
//			try {
//				InsertQuery i = new InsertQuery();
//				i.setTable("assessed");
//				i.setRow(r);
//				ec.doUpdate(i);
//				// ec.doUpdate(s);
//			} catch (final DBException dbx) {
//				// System.out.println(s);
//				dbx.printStackTrace();
//			}
//		}
//
//		public void process(final Tag t) throws IOException {
//			try {
//				if ("assessments".equals(t.name)) {
//					if (!export)
//						return;
//
//					asmw = new StringWriter();
//					t.newTagText = "";
//					tf.divert(asmw);
//					populateAssessed();
//				} else if ("/assessments".equals(t.name)) {
//					if (!export)
//						return;
//
//					tf.stopDiverting();
//					final String assessments = asmw.toString();
//					final String[] asmids = assessments.split(",");
//					final List<Assessment> assessObjs = new ArrayList<Assessment>();
//					for (final String asmid : asmids) {
//						try {
//							if (!assessmentsToSkip.containsKey(asmid)) {
//								Assessment temp = SIS.get().getAssessmentIO().readAssessment(Integer.valueOf(asmid));
//								if (temp != null)
//									assessObjs.add(temp);
//							}
//						} catch (Exception e) {
//							System.out.println("COULD NOT FIND ASSESSMENT " + asmid);
//						}
//					}
//					Collections.sort(assessObjs, new PublishedAssessmentsComparator());
//					for (Assessment cur : assessObjs)
//						new TrivialAssessment(cur.getAssessmentID() + "", id);
//
//				} else if ("taxonomicAuthority".equals(t.name)) {
//					if (!export)
//						return;
//
//					taxw = new StringWriter();
//					t.newTagText = "";
//					tf.divert(taxw);
//				} else if ("/taxonomicAuthority".equals(t.name)) {
//					if (!export)
//						return;
//
//					tf.stopDiverting();
//					taxonomicAuthority = taxw.toString();
//				} else if ("synonym".equals(t.name)) {
//					if (!export)
//						return;
//
//					// Process synonyms
//					String name = t.getAttribute("name");
//
//					String synlevel = t.getAttribute("level");
//					String notes = t.getAttribute("notes");
//					String status = t.getAttribute("status");
//					String rlCat = t.getAttribute("rlCat");
//					String rlCrit = t.getAttribute("rlCrit");
//					String rlDate = t.getAttribute("rlDate");
//					String genusAuth = t.getAttribute(TaxonLevel.displayableLevel[(TaxonLevel.GENUS)]);
//					String spcAuth = t.getAttribute(TaxonLevel.displayableLevel[(TaxonLevel.SPECIES)]);
//					String infraAuth = t.getAttribute(TaxonLevel.displayableLevel[TaxonLevel.INFRARANK]);
//
//					String[] brokenName = name.split("\\s");
//
//					String genusName = t.getAttribute("genusName");
//					String spcName = t.getAttribute("speciesName");
//					String infraType = t.getAttribute("infraType");
//					String infraName = t.getAttribute("infraName");
//					String subpopName = t.getAttribute("stockName");
//					String upperLevelName = t.getAttribute("upperLevelName");
//
//					if (synlevel == null || synlevel.equals("") || synlevel.equals("-1")) {
//						synlevel = "N/A";
//
//						if (depth >= TaxonLevel.GENUS) {
//							genusName = brokenName[0];
//							synlevel = TaxonLevel.displayableLevel[TaxonLevel.GENUS];
//
//							if (brokenName.length > 1) {
//								if (brokenName[1].endsWith("ssp.")) {
//									name = new StringBuilder(name).insert(name.indexOf("ssp."), " ").toString();
//
//									System.out.println(brokenName[1] + " ends with ssp. New name is " + name);
//									brokenName = name.split("\\s");
//								} else if (brokenName[1].endsWith("var.")) {
//									name = new StringBuilder(name).insert(name.indexOf("var."), " ").toString();
//
//									System.out.println(brokenName[1] + " ends with var. New name is " + name);
//									brokenName = name.split("\\s");
//								} else if (brokenName[1].endsWith("fma.")) {
//									name = new StringBuilder(name).insert(name.indexOf("fma."), " ").toString();
//
//									System.out.println(brokenName[1] + " ends with fma. New name is " + name);
//									brokenName = name.split("\\s");
//								}
//
//								spcName = brokenName[1];
//								synlevel = TaxonLevel.displayableLevel[(TaxonLevel.SPECIES)];
//							}
//							if (brokenName.length > 2) {
//								if (brokenName[2].matches("^ssp\\.?$") || brokenName[2].matches("^var\\.?$")
//										|| brokenName[2].equals("fma.")) {
//									infraType = brokenName[2];
//									infraName = brokenName[3];
//									for (int i = 4; i < brokenName.length; i++)
//										infraName += " " + brokenName[i];
//
//									synlevel = TaxonLevel.displayableLevel[(TaxonLevel.INFRARANK)];
//								} else if (brokenName.length > 3
//										&& (brokenName[3].matches("^ssp\\.?$") || brokenName[3].matches("^var\\.?$") || brokenName[3]
//												.equals("fma."))) {
//									spcName += " " + brokenName[2];
//									infraType = brokenName[3];
//									infraName = brokenName[4];
//									for (int i = 5; i < brokenName.length; i++)
//										infraName += " " + brokenName[i];
//
//									synlevel = TaxonLevel.displayableLevel[(TaxonLevel.INFRARANK)];
//								} else {
//									String whatsLeft = brokenName[2];
//									for (int i = 3; i < brokenName.length; i++)
//										whatsLeft += " " + brokenName[i];
//
//									if (whatsLeft.toLowerCase().contains("stock")
//											|| whatsLeft.toLowerCase().contains("subpopulation")) {
//										subpopName = whatsLeft;
//										synlevel = TaxonLevel.displayableLevel[(TaxonLevel.SUBPOPULATION)];
//									} else
//										spcName += " " + whatsLeft;
//								}
//							}
//						}
//					} else if (synlevel.matches("\\d+")) {
//						int intLevel = Integer.valueOf(synlevel);
//						synlevel = TaxonLevel.displayableLevel[(intLevel)];
//					}
//
//					if (spcName == null)
//						spcName = "";
//					if (genusName == null)
//						genusName = "";
//					if (infraName == null)
//						infraName = "";
//					if (upperLevelName == null)
//						upperLevelName = "";
//					if (subpopName == null)
//						subpopName = "";
//
//					if (infraType == null)
//						infraType = "";
//					else if (infraType.equals("-1"))
//						infraType = "";
//
//					final String query = "insert into synonyms (tax_id,name,level,"
//							+ "genus_name,species_name,infra_type,infra_name,stock_name,"
//							+ "genus_author,species_author,infrarank_author," + "status,notes,rl_category,"
//							+ "rl_criteria,rl_date) values ("
//							+ id
//							+ ","
//							+ formatLiteral(name)
//							+ ","
//							+ formatLiteral(synlevel)
//							+ ","
//							+ formatLiteral(genusName)
//							+ ","
//							+ formatLiteral(spcName)
//							+ ","
//							+ formatLiteral(infraType)
//							+ ","
//							+ formatLiteral(infraName)
//							+ ","
//							+ formatLiteral(subpopName)
//							+ ","
//							+ formatLiteral(genusAuth)
//							+ ","
//							+ formatLiteral(spcAuth)
//							+ ","
//							+ formatLiteral(infraAuth)
//							+ ","
//							+ formatLiteral(status)
//							+ ","
//							+ formatLiteral(notes)
//							+ ","
//							+ formatLiteral(rlCat)
//							+ "," + formatLiteral(rlCrit) + "," + formatLiteral(rlDate) + ")";
//					try {
//						ec.doUpdate(query);
//					} catch (final DBException dbx) {
//						dbx.printStackTrace();
//					}
//				} else if ("commonName".equals(t.name)) {
//					if (!export)
//						return;
//
//					final String iso = t.getAttribute("iso");
//					final String name = t.getAttribute("name");
//					// switch "primary" for "principal" because easier to query
//					// in SQL
//					String primary = null;
//					if ("true".equals(t.getAttribute("primary")))
//						primary = "Y";
//					else
//						primary = "N";
//					String validated = null;
//					if ("true".equals(t.getAttribute("validated")))
//						validated = "Y";
//					else
//						validated = "N";
//					final String s = "insert into common_name (tax_id,common_name,iso_language,principal,validated)"
//							+ " values (" + id + "," + ec.formatLiteral(new StringLiteral(name)) + ","
//							+ ec.formatLiteral(new StringLiteral(iso)) + ",'" + primary + "','" + validated + "')";
//					// System.out.println(s);
//					try {
//						ec.doUpdate(s);
//					} catch (final DBException dbx) {
//						dbx.printStackTrace();
//					}
//				} else {
//					status = t.getAttribute("status");
//					// final String bDeprecated = t.getAttribute("deprecated");
//					// if ("true".equals(bDeprecated)) {
//					// deprecated = "Y";
//					// } else {
//					// deprecated = "N";
//					// }
//					final String bHybrid = t.getAttribute("hybrid");
//					if ("true".equals(bHybrid)) {
//						hybrid = "Y";
//					} else {
//						hybrid = "N";
//					}
//				}
//			} catch (final RuntimeException x) {
//				x.printStackTrace();
//			}
//		}
//
//		public void setTagFilter(final TagFilter tf) {
//			// TODO Auto-generated method stub
//		}
//	}
//
//	private static final HashMap<String, Boolean> lookupTables = new HashMap<String, Boolean>();
//
//	private static AtomicBoolean running = new AtomicBoolean(false);
//
//	/**
//	 * Specifies where to find the stresses count in a threat structure.
//	 */
//	private final static int STRESS_COUNT_OFFSET = 5;
//
//	private static ArrayList<String> nodeTagNames = null;
//	private static final String DS = "accessexport";
//
//	public static String getWorking() {
//
//		return "/usr/data/" + GoGoEgo.getInitProperties().getProperty("INSTANCE") + ".mdb";
//	}
//
//	public static String getWorkingZip() {
//		return "/usr/data/" + GoGoEgo.getInitProperties().getProperty("INSTANCE") + ".zip";
//	}
//
//	public static boolean isRunning() {
//		return running.get();
//	}
//
//	private final HashMap<String, Integer> seenReferences = new HashMap<String, Integer>();
//
//	private int highReferenceId = 0;
//
//	private ExecutionContext ec = null;
//
//	private Uniform dispatcher;
//
//	private int taxonCount = 0;
//	private String workingsetId = null;
//
//	private boolean exportDraftAssessments = false;
//	private boolean exportRegionalDraftAssessments = false;
//
//	protected HashMap<String, String> assessmentsToSkip;
//	protected HashMap<String, User> users;
//
//	private org.iucn.sis.shared.api.models.AssessmentFilter filter = null;
//	private VFS vfs;
//	private Context context;
//
//	public AccessExport(Uniform uniform, Context context) {
//		this(uniform, null, context);
//		exportDraftAssessments = false;
//		exportRegionalDraftAssessments = false;
//	}
//
//	public AccessExport(Uniform uniform, String workingsetId, Context context) {
//		this.workingsetId = workingsetId;
//		this.dispatcher = uniform;
//		exportDraftAssessments = true;
//		exportRegionalDraftAssessments = true;
//		vfs = ExportApplication.getApplication(context).getVFS();
//		this.context = context;
//	}
//
//	/**
//	 * Copy boilerplate DB to working DB, attach to working DB. Like DEMImport,
//	 * this relies on the presence of some magic files in /usr/data.
//	 */
//	private void createAndConnect(String id) throws IOException, NamingException, DBException {
//		final File src = new File("/usr/data/baseline.mdb");
//		String working;
//		if (id == null)
//			working = getWorking();
//		else
//			working = "/usr/data/export_" + id + ".mdb";
//		final File tgt = new File(working);
//		FileVFS.copyFile(src, tgt);
//		DBSessionFactory.unregisterDataSource(DS);
//		System.out.println("Connecting to working database");
//		DBSessionFactory
//				.registerDataSource(DS, "jdbc:access:///" + working, "com.hxtt.sql.access.AccessDriver", "", "");
//
//		ec = new SystemExecutionContext(DS);
//		ec.setExecutionLevel(ExecutionContext.ADMIN);
//		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
//
//		try {
//			ec.createStructure(ClasspathResources.getDocument(AccessExport.class, "export-struct.xml"));
//		} catch (final DBException e) {
//			e.printStackTrace();
//			throw e;
//		}
//	}
//
//	private void createAndConnectWithPostgres(String id) throws IOException, NamingException, DBException {
//		String working;
//		if (id == null)
//			working = "sis";
//		else
//			working = "sis_" + id;
//		DBSessionFactory.unregisterDataSource(DS);
//		System.out.println("Connecting to working database");
//		DBSessionFactory
//				.registerDataSource(DS, "jdbc:postgresql:" + working, "org.postgresql.Driver", "adam", "s3cr3t");
//
//		ec = new SystemExecutionContext(DS);
//		ec.setExecutionLevel(ExecutionContext.ADMIN);
//		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
//
//		try {
//			ec.createStructure(ClasspathResources.getDocument(AccessExport.class, "export-struct.xml"));
//		} catch (final DBException e) {
//			e.printStackTrace();
//			throw e;
//		}
//	}
//
//	public void export() throws IOException, NamingException, DBException {
//		createAndConnect(null);
//		populateDB();
//		zipResults();
//	}
//
//	public void export(String workingsetId) throws IOException, NamingException, DBException {
//		List<String> speciesIds;
//		WorkingSet ws = SIS.get().getWorkingSetIO().readWorkingSet(Integer.valueOf(workingsetId));
//
//		filter = ws.getFilter();
//		createAndConnect(ws.getSpeciesIDsAsString());
//		populateDB(speciesIds);
//		zipResults(workingsetId);
//	}
//
//	private void getUserInfo() throws NamingException {
//		users = new HashMap<String, User>();
//
//		SystemExecutionContext ec2 = new SystemExecutionContext("users");
//		ec2.setExecutionLevel(ExecutionContext.ADMIN);
//		ec2.setAPILevel(ExecutionContext.SQL_ALLOWED);
//
//		final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
//		query.select("user", "*");
//		query.select("profile", "firstname");
//		query.select("profile", "lastname");
//		query.select("profile", "initials");
//		query.select("profile", "affiliation");
//
//		final Row.Set rs = new Row.Set();
//		try {
//			ec2.doQuery(query, rs);
//
//			for (Row curRow : rs.getSet()) {
//				String f = curRow.get("firstname").getString();
//				String l = curRow.get("lastname").getString();
//				String i = curRow.get("initials").getString();
//				String id = curRow.get("id").getString();
//
//				User user = new User();
//				user.setFirstName(f);
//				user.setLastName(l);
//				user.setInitials(i);
//
//				users.put(id, user);
//			}
//		} catch (DBException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void populateDB() {
//		final long time = System.currentTimeMillis();
//		System.out.println("Getting taxonomy doc");
//		final Document taxdoc = TaxonomyDocUtils.getTaxonomyDocByID();
//		System.out.println("Taxonomy doc fetched in " + (System.currentTimeMillis() - time) + " ms");
//
//		System.out.println("Populating User information.");
//		try {
//			getUserInfo();
//		} catch (NamingException e) {
//			e.printStackTrace();
//			System.out.println("Error fetching User information. Bailing.");
//			return;
//		}
//
//		System.out.println("Populating Regions lookup table.");
//		Document regionsDoc = DocumentUtils.getVFSFileAsDocument("/regions/regions.xml", vfs);
//		if (regionsDoc != null) {
//			try {
//				ElementCollection col = ElementCollection.childElementsByTagName(regionsDoc.getDocumentElement(),
//						"region");
//				for (Element el : col) {
//					Row row = ec.getRow("RegionLookup");
//					String name = el.getElementsByTagName("name").item(0).getTextContent();
//					String desc = el.getElementsByTagName("description").item(0).getTextContent();
//
//					row.get("region_id").setString(el.getAttribute("id"));
//					row.get("region_name").setString(ec.formatLiteral(new StringLiteral(name)));
//					row.get("region_description").setString(ec.formatLiteral(new StringLiteral(desc)));
//
//					final InsertQuery iq = new InsertQuery("RegionLookup", row);
//					ec.doUpdate(iq);
//				}
//			} catch (DBException e) {
//				e.printStackTrace();
//				System.out.println("ERROR building RegionLookup table.");
//			}
//		}
//
//		final NodeCollection nodes = new NodeCollection(taxdoc.getDocumentElement().getChildNodes());
//		taxonCount = 0;
//		traverseNodes(null, nodes, 0, null);
//		System.out.println("Taxon count: " + taxonCount);
//		System.out.println("Overall exported in " + (System.currentTimeMillis() - time) + " ms");
//	}
//
//	private void populateDB(List<String> ids) {
//		final long time = System.currentTimeMillis();
//		System.out.println("Getting taxonomy doc");
//
//		final Document taxdoc = TaxonomyDocUtils.getTaxonomyDocByID();
//
//		System.out.println("Taxonomy doc fetched in " + (System.currentTimeMillis() - time) + " ms");
//
//		System.out.println("Populating User information.");
//		try {
//			getUserInfo();
//		} catch (NamingException e) {
//			e.printStackTrace();
//			System.out.println("Error fetching User information. Bailing.");
//			return;
//		}
//
//		System.out.println("Populating Regions lookup table.");
//		Document regionsDoc = DocumentUtils.getVFSFileAsDocument("/regions/regions.xml", vfs);
//		if (regionsDoc != null) {
//			try {
//				ElementCollection col = ElementCollection.childElementsByTagName(regionsDoc.getDocumentElement(),
//						"region");
//				for (Element el : col) {
//					Row row = ec.getRow("RegionLookup");
//					String name = el.getElementsByTagName("name").item(0).getTextContent();
//					String desc = el.getElementsByTagName("description").item(0).getTextContent();
//
//					row.get("region_id").setString(el.getAttribute("id"));
//					row.get("region_name").setString(ec.formatLiteral(new StringLiteral(name)));
//					row.get("region_description").setString(ec.formatLiteral(new StringLiteral(desc)));
//
//					final InsertQuery iq = new InsertQuery("RegionLookup", row);
//					ec.doUpdate(iq);
//				}
//			} catch (DBException e) {
//				e.printStackTrace();
//				System.out.println("ERROR building RegionLookup table.");
//			}
//		}
//
//		final NodeCollection nodes = new NodeCollection(taxdoc.getDocumentElement().getChildNodes());
//
//		taxonCount = 0;
//		traverseNodes(null, nodes, 0, ids);
//		System.out.println("Taxon count: " + taxonCount);
//		System.out.println("Overall exported in " + (System.currentTimeMillis() - time) + " ms");
//	}
//
//	public void run() {
//		if (!running.compareAndSet(false, true))
//			return;
//		try {
//			assessmentsToSkip = new HashMap<String, String>();
//			assessmentsToSkip.put("424080", "424080");
//			assessmentsToSkip.put("424081", "424081");
//			assessmentsToSkip.put("424079", "424079");
//
//			if (workingsetId == null)
//				export();
//			else
//				export(workingsetId);
//
//			lookupTables.clear();
//		} catch (final Throwable t) {
//			t.printStackTrace();
//		} finally {
//			DBSessionFactory.unregisterDataSource(DS);
//			running.set(false);
//		}
//	}
//
//	private void traverseNodes(final TrivialNode parent, final NodeCollection nodes, final int depth, List<String> ids) {
//		for (final Node node : nodes) {
//			if (!(node instanceof Element))
//				continue;
//			final Element element = (Element) node;
//			final String elementName = element.getNodeName();
//			if (!elementName.startsWith("node"))
//				continue;
//
//			final int nodeID = Integer.valueOf(elementName.substring(4));
//			final String taxonName = element.getAttribute("name");
//			taxonCount++;
//			if (taxonCount % 100 == 0) {
//				System.out.println(taxonCount);
//			}
//
//			if (ids == null) {
//				final TrivialNode tn = new TrivialNode(nodeID, taxonName, parent, depth, true);
//				if (node.hasChildNodes()) {
//					traverseNodes(tn, new NodeCollection(node.getChildNodes()), depth + 1, ids);
//				}
//			} else {
//				boolean export = ids.contains(String.valueOf(nodeID));
//				if (export)
//					System.out.println(nodeID);
//
//				final TrivialNode tn = new TrivialNode(nodeID, taxonName, parent, depth, export);
//				if (node.hasChildNodes()) {
//					traverseNodes(tn, new NodeCollection(node.getChildNodes()), depth + 1, ids);
//				}
//			}
//		}
//	}
//
//	private void zipResults() {
//		try {
//			FileZipper.zipper(new File(getWorking()), new File(getWorkingZip()));
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("Error zipping AccessExport at path " + getWorking());
//		}
//	}
//
//	private void zipResults(String workingsetId) {
//
//		try {
//			FileZipper.zipper(new File("/usr/data/export_" + workingsetId + ".mdb"), new File("/usr/data/export_"
//					+ workingsetId + ".zip"));
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("Error zipping AccessExport at path " + "/usr/data/export_" + workingsetId + ".mdb");
//		}
//	}
//
//}
