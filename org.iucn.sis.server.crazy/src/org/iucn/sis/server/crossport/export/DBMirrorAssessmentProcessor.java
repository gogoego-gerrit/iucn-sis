package org.iucn.sis.server.crossport.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.iucn.sis.client.referenceui.ReferenceUI;
import org.iucn.sis.server.changes.AsmChanges;
import org.iucn.sis.server.integrity.IntegrityValidator;
import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.acl.User;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.structures.FormattingStripper;
import org.iucn.sis.shared.xml.XMLUtils;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.ConversionException;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.Row.Set;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.Query;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;
import com.solertium.util.TrivialExceptionHandler;

public class DBMirrorAssessmentProcessor extends DBMirrorCommodityProcessor<AssessmentData> {

	/**
	 * Specifies where to find the stresses count in a threat structure.
	 */
	private static final int STRESS_COUNT_OFFSET = 5;
	protected HashMap<String, User> users;

	public DBMirrorAssessmentProcessor(Queue<AssessmentData> queue, String threadID, HashMap<String, User> users) throws DBException, IOException, NamingException {
		super(queue, threadID);
		this.users = users;
	}

	@Override
	protected void process(AssessmentData data) {
		try {
			doWork(data);
		} catch (final Exception ix) {
			System.err.println("DB exception parsing assessment " + data.getAssessmentID());
			ix.printStackTrace();
		}
		
		revalidate(data);
	}

	private void revalidate(AssessmentData data) {
		if (data != null && data.isGlobal() && BaseAssessment.DRAFT_ASSESSMENT_STATUS.equals(data.getType())) {
			final DeleteQuery query = new DeleteQuery();
			query.setTable("assessment_integrity_status");
			query.constrain(new CanonicalColumnName("assessment_integrity_status", "asm_uid"), 
					QConstraint.CT_EQUALS, data.getAssessmentID() + "_" + data.getType());
			
			try {
				ec.doUpdate(query);
			} catch (DBException e) {
				TrivialExceptionHandler.ignore(this, e);
			}
			
			try {
				IntegrityValidator.validate_background(SISContainerApp.getStaticVFS(), ec, data.getAssessmentID(), data.getType());
			} catch (DBException e) {
				System.err.println("DB exception validating assessment " + data.getAssessmentID());
				e.printStackTrace();
			}
		}
	}
	
	private String generateTextFromUsers(List<User> userList) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < userList.size(); i++) {
			text.append(userList.get(i).getCitationName());
			
			if (i + 1 < userList.size() - 1)
				text.append(", ");

			else if (i + 1 == userList.size() - 1)
				text.append(" & ");
		}
		
		return text.toString();
	}

	
	private void addClassification(final String id, String uid, final String user, final String scheme, final List<String> selected, 
			final String selectedID, final Row extantData) throws DBException {
		// populate id and lookup column

		final Row clsRow;
		try {
			clsRow = ec.getRow(scheme);

			if (clsRow == null) {
				System.out.println("No table found for classification scheme " + scheme);
				return;
			}
		} catch (Exception e) {
			System.out.println("No table found for classification scheme " + scheme);
			return;
		}
		
		final Column lkup = clsRow.get(2);
		// System.out.println("Lookup column for "+scheme+" is
		// "+lkup.getLocalName());
		lkup.setString(selectedID);
		// populate remainder
		int col = 3;
		boolean changed = (extantData == null);
		
		for (String s : selected) {
			try {
				final Column currentColumn = clsRow.get(col);
				if (s != null && !"".equals(s)) {
					currentColumn.setString(s);
				}
				col++;
				
				try {
					if( !changed && extantData.size() < col && !extantData.get(col).getString(Column.NEVER_NULL).equals(s) )
						changed = true;
				} catch (Exception e) {
					System.out.println("Exception trying to determine whether the data changed for scheme " + scheme);
					e.printStackTrace();
				}
				
				try {
					if (clsRow.get(col).getLocalName().equalsIgnoreCase(AsmChanges.LAST_USER_UPDATED))
						if ("Threats".equals(scheme))
							addStresses(id, uid, selected, selectedID, user);
				} catch (IndexOutOfBoundsException e) {
					if ("Threats".equals(scheme))
						addStresses(id, uid, selected, selectedID, user);
					
					break;
				}
			} catch (final NumberFormatException badData) {
				badData.printStackTrace();
				System.out.println("ERROR parsing int from " + selectedID 
						+ " for class scheme " + scheme);
			}
		}

		// commit to database
		try {
			if( changed ) { 
				addConstantRowInfo(clsRow, id, uid, user);
				insertOrUpdate(scheme, lkup.getLocalName(), new QComparisonConstraint(new CanonicalColumnName(
						scheme, lkup.getLocalName()), QConstraint.CT_EQUALS, selectedID), clsRow);
//				final InsertQuery iqr = new InsertQuery(scheme, clsRow);
//				ec.doUpdate(iqr);
			}
		} catch (final DBException dbx) {
			dbx.printStackTrace();
		}
	}
	
	private void addConstantRowInfo(Row row, String asm_id, String uid, String user) {
		row.get(AsmChanges.ASM_ID).setObject(asm_id);
		row.get(AsmChanges.UID).setObject(uid);
		row.get(AsmChanges.LAST_USER_UPDATED).setObject(user);
	}

	private void addReference(final AssessmentData assessment, String uid) throws DBException {
		DeleteQuery del = new DeleteQuery("assessment_reference", "uid", uid);
		ec.doUpdate(del);

		for(final Entry<String, ArrayList<ReferenceUI>> refEntry : assessment.getReferences().entrySet()) {
			final String fieldName = refEntry.getKey();

			for (final ReferenceUI ref : refEntry.getValue()) {
				final String referenceHash = ref.getReferenceID();
				final Row asmRefRow = ec.getRow("assessment_reference");
				asmRefRow.get("asm_id").setObject(assessment.getAssessmentID());
				asmRefRow.get("uid").setString(uid);
				asmRefRow.get("field").setString(fieldName);
				asmRefRow.get("ref_id").setString(referenceHash);
				try {
					final InsertQuery iqr = new InsertQuery("assessment_reference", asmRefRow);
					ec.doUpdate(iqr);
				} catch (final DBException dbx) {
					dbx.printStackTrace();
				}
				
				final Row refRow = ec.getRow("reference");
				refRow.get("id").setString(ref.getReferenceID());
				refRow.get("type").setString(ref.getReferenceType());
				for (Entry<String, String> curEntry : ref.entrySet()) {
					final Column c = refRow.get(curEntry.getKey());
					if (c != null) {
						final String s = curEntry.getValue();
						if (s != null && !"".equals(s)) {
							c.setString(s);
						}
					}
				}

				final Column c = refRow.get("citation");
				if (c.getString() == null || c.getString().equals("")) {
					if (ref.getReferenceType().equalsIgnoreCase("rldb"))
						ref.setReferenceType("Book");

					ref.generateCitation();
					c.setString(ref.getCitation());
				}

				try {
					insertOrUpdate("reference", "id", new QComparisonConstraint(new CanonicalColumnName(
							"reference", "id"), QConstraint.CT_EQUALS, ref.getReferenceID()), refRow);
//					final InsertQuery iq = new InsertQuery("reference", refRow);
//					ec.doUpdate(iq);
				} catch (final DBException dbx) {
					dbx.printStackTrace();
				}
			}
		}
	}

	private void addStresses(final String id, String uid, final List<String> selected, final String threatID, 
			final String user) throws DBException {
		try {
			HashMap<String, String> existingStresses = new HashMap<String, String>();
			
			SelectQuery sel = new SelectQuery();
			sel.select("Stresses", "*");
			sel.constrain(new CanonicalColumnName("Stresses", "uid"), QConstraint.CT_EQUALS, uid);
			sel.constrain(new CanonicalColumnName("Stresses", "threat_id"), QConstraint.CT_EQUALS, threatID);
			
			Set set = new Set();
			ec.doQuery(sel, set);
			
			for( Row curRow : set.getSet() ) {
				 String stress = curRow.get("stress_id").getString();
				 int index = selected.indexOf(stress);
				 if( index == -1 || index <= STRESS_COUNT_OFFSET ) {
					DeleteQuery dRow = new DeleteQuery();
					dRow.constrain(new QComparisonConstraint(new CanonicalColumnName(
							"Stresses", "stress_id"), QConstraint.CT_EQUALS, stress));
					dRow.constrain(new QComparisonConstraint(new CanonicalColumnName(
							"Stresses", "threat_id"), QConstraint.CT_EQUALS, threatID));
					ec.doUpdate(dRow);
				 } else
					 existingStresses.put(stress, index+"");
			}

			final int stressCount = Integer.parseInt(selected.get(STRESS_COUNT_OFFSET));
			final int threat = Integer.parseInt(threatID);
			if (stressCount > 1) {
				for (int i = STRESS_COUNT_OFFSET + 1; i <= STRESS_COUNT_OFFSET + stressCount; i++) {
					if( existingStresses.containsKey(selected.get(i)) ) {
						final Row strRow = ec.getRow("Stresses");
						addConstantRowInfo(strRow, id, uid, user);
						strRow.get("threat_id").setObject(threat);
						strRow.get("stress_id").setString(selected.get(i));
						final InsertQuery iqr = new InsertQuery("Stresses", strRow);
						ec.doUpdate(iqr);
					}
				}
			}
		} catch (final Exception notVeryUnexpected) {
			notVeryUnexpected.printStackTrace();
		}
	}



	private void captureBasic(final Row row, final AssessmentData assessment) {
		captureBasicString(assessment.getDateModified()+"", row, "dateModified");
		captureBasicString(assessment.getDateAdded(), row, "dateAdded");
		captureBasicString(assessment.getDateFinalized(), row, "dateFinalized");
		captureBasicString(assessment.isDone()+"", row, "isDone");
		captureBasicString(assessment.isHistorical()+"", row, "isHistorical");
		captureBasicString(assessment.getSource(), row, "source");
		captureBasicString(assessment.getSourceDate(), row, "sourceDate");
		captureBasicString(assessment.getType(), row, "validationStatus");
		
	}

	private void captureBasicString(final String string, Row row, String cname) {
		if( string != null && !string.equals("") )
			row.get(cname).setString(string);
		else
			row.get(cname).setObject(null);
	}





	private void insertOrUpdate(String table, String field, QConstraint constraint, Row row) throws DBException {
		Query query;

		try {
			SelectQuery sel = new SelectQuery();
			sel.select(new CanonicalColumnName(table, field));
			sel.constrain(constraint);
			Set set = new Set();
			ec.doQuery(sel, set);

			if( set.getSet().size() == 0 )
				query = new InsertQuery(table, row);
			else
				query = new UpdateQuery(table, row, constraint);
		} catch (DBException e) {
			query = new InsertQuery(table, row);
		}

		ec.doUpdate(query);
	}


	protected void doWork(AssessmentData assessment) throws Exception {
		final String validationStatus = assessment.getType();
		final String uid = assessment.getAssessmentID() + "_" + validationStatus;

		final Row asmRow = ec.getRow("assessment");
		addConstantRowInfo(asmRow, assessment.getAssessmentID(), uid, assessment.getUserLastUpdated());
		asmRow.get("tax_id").setObject(Integer.parseInt(assessment.getSpeciesID()));
		asmRow.get("validationStatus").setString(validationStatus);

		captureBasic(asmRow, assessment);

		//look for references
		addReference(assessment, uid);

		for(Entry<String, Object> fieldData : assessment.getDataMap().entrySet()) {

			if (fieldData.getValue() instanceof List) {
				// fields
				final String fieldName = fieldData.getKey();
				final List<String> curFieldData = (List<String>)fieldData.getValue();
				final Column c = asmRow.get(fieldName);

				if ("RegionInformation".equals(fieldName)) {
					String isRegional = Boolean.valueOf(curFieldData.get(0).contains("-1")).toString();
					String regions = curFieldData.get(0);
					String endemic = curFieldData.get(1);

					asmRow.get("is_regional").setString(isRegional);
					asmRow.get("is_endemic").setString(endemic);

					String[] ids;
					if (regions.contains(","))
						ids = regions.split(",");
					else
						ids = new String[] { regions };

					for (String curID : ids) {
						Row regRow = ec.getRow("RegionInformation");
						regRow.get("region_id").setObject(curID);
						addConstantRowInfo(regRow, assessment.getAssessmentID(), uid, assessment.getUserLastUpdated());

						insertOrUpdate("RegionInformation", "uid", new QComparisonConstraint(new CanonicalColumnName(
								"RegionInformation", "uid"), QConstraint.CT_EQUALS, uid), regRow);
						//								final InsertQuery iq = new InsertQuery("RegionInformation", regRow);
						//								ec.doUpdate(iq);
					}
				} else if (c != null) { // just put it in the column
					boolean narrative = fieldName.endsWith("Documentation")
					|| fieldName.equalsIgnoreCase("RedListRationale");
					
					if( "RedListEvaluators".equals(fieldName) || "RedListContributors".equals(fieldName) || 
							"RedListAssessors".equals(fieldName)) {
						if( curFieldData.size() > 0 ) {
							String s = curFieldData.get(0);
							if( s == null || s.equals("") ) {
								List<User> userList = new ArrayList<User>();
								for (int i = 2; i < curFieldData.size(); i++) {
									String curID = curFieldData.get(i);
									if( !curID.equals("0") ) {
										if( users.containsKey(curID) )
											userList.add(users.get(curID));
										else
											System.out.println("Could not find user with ID " + curID);
									}
								}
								
								s = generateTextFromUsers(userList);
							}
							
							c.setString(s);
						}
					} else {
						for (final String s : curFieldData) {
							if (s != null && !"".equals(s)) {
								if (narrative)// TODO: TEST THIS!
									c.setString(FormattingStripper.stripText(XMLUtils.cleanFromXML(s)));
								else
									c.setString(s);
								break;
							}
						}
					}
				} else if ("UseTradeDetails".equals(fieldName) || "Livelihoods".equals(fieldName)) { 
					int countOffset = "Livelihoods".equals(fieldName) ? 1 : 0;
					int count = Integer.valueOf(curFieldData.get(countOffset));
					if( count > 0 ) {
						int total = (curFieldData.size()-countOffset-1) / count;

						for( int i = 0; i < count; i++ ) {
							if( ec.getMatchingTables(fieldName).size() > 0 ) {
								Row row = ec.getRow(fieldName);
								addConstantRowInfo(row, assessment.getAssessmentID(), uid, assessment.getUserLastUpdated());

								for( int j = 0; j < total; j++ ) {
									try {
										row.get(j+2).setObject(curFieldData.get( (j+1)+(total*i)+countOffset ));
									} catch (ConversionException e1) {
										try {
											if( curFieldData.get( (j+1)+(total*i)+countOffset ).equals("") )
												row.get(j+2).setObject(0);
											else
												row.get(j+2).setObject(Integer.valueOf(curFieldData.get( (j+1)+(total*i)+countOffset )));
										} catch (NumberFormatException e2) {
											try {
												row.get(j+2).setObject(Boolean.valueOf(curFieldData.get( (j+1)+(total*i)+countOffset )));
											} catch (ConversionException e3) {
												if( curFieldData.get( (j+1)+(total*i)+countOffset ).equals("false") ) {
													countOffset++; //Wayward false. Gr. Increment the offset and try again.
													j = -1;
												} else
													System.out.println("COULD NOT SET DATA for field " + fieldName + " at index " + ((j+1)+(total*i)+countOffset));
											}
										}
									}
								}

								try {
									insertOrUpdate(fieldName, "uid", new QComparisonConstraint(new CanonicalColumnName(
											fieldName, "uid"), QConstraint.CT_EQUALS, uid), row);
									//								final InsertQuery iq = new InsertQuery(fieldName, row);
									//								ec.doUpdate(iq);
								} catch (final DBException dbx) {
									dbx.printStackTrace();
								}
							}else
								System.out.println("COULD NOT FIND TABLE FOR FIELD " + fieldName);
						}
					}
				} else { // complex structure like RedListCriteria
					// System.out.println("Trying to find table for " +
					// fieldName);
					try {
						final Row externRow = ec.getRow(fieldName);
						addConstantRowInfo(externRow, assessment.getAssessmentID(), uid, assessment.getUserLastUpdated());
						int count = 2;
						for (final String s : curFieldData) {
							final Column externCol = externRow.get(count++);
							if (s != null && !"".equals(s)) {
								externCol.setString(s);
							}
						}
						try {
							insertOrUpdate(fieldName, "uid", new QComparisonConstraint(new CanonicalColumnName(
									fieldName, "uid"), QConstraint.CT_EQUALS, uid), externRow);
							//								final InsertQuery iq = new InsertQuery(fieldName, externRow);
							//								ec.doUpdate(iq);
						} catch (final DBException dbx) {
							dbx.printStackTrace();
						}
					} catch (Exception e2) {
						// System.out.println("Failed finding table for " +
						// fieldName);
						// No table found. It's cool.
					}
				}
			} else {
				final String scheme = fieldData.getKey();

				if( !scheme.equalsIgnoreCase("lakes") && !scheme.equalsIgnoreCase("rivers") ) {
					try {
						SelectQuery del = new SelectQuery();
						del.select(scheme, "*");
						del.constrain(new CanonicalColumnName(scheme, "uid"), QConstraint.CT_EQUALS, uid);
						
						Set set = new Set();
						ec.doQuery(del, set);
						
						HashMap<String, List<String>> schemeData = (HashMap<String, List<String>>)fieldData.getValue();
						
						for( Row curRow : set.getSet() ) {
							 String selectedID = curRow.get(2).getString();
							 if( !schemeData.containsKey(selectedID) ) {
								DeleteQuery dRow = new DeleteQuery(scheme, curRow.getColumns().get(2).getLocalName(), selectedID);
								ec.doUpdate(dRow);
							 } else
								 addClassification(assessment.getAssessmentID(), uid, assessment.getUserLastUpdated(), 
										 scheme, schemeData.get(selectedID), selectedID, curRow);
						}
						
						for (Entry<String, List<String>> curSchemeData : schemeData.entrySet())
							addClassification(assessment.getAssessmentID(), uid, assessment.getUserLastUpdated(), scheme, curSchemeData.getValue(), curSchemeData.getKey(), null);
						
					} catch (DBException e) {
						e.printStackTrace();
					}
				}
			} 
		}
		try {
			if( asmRow.get("is_regional") == null )
				asmRow.get("is_regional").setString("true");

			insertOrUpdate("assessment", "uid", new QComparisonConstraint(new CanonicalColumnName(
					"assessment", "uid"), QConstraint.CT_EQUALS, uid), asmRow);
			//				final InsertQuery iq = new InsertQuery("assessment", asmRow);
			// System.out.println("ASSESSMENT insert SQL: " +
			// iq.getSQL(ec.getDBSession()));
			//				ec.doUpdate(iq);
		} catch (final DBException dbx) {
			final InsertQuery iq = new InsertQuery("assessment", asmRow);
			System.out.println("ASSESSMENT insert SQL: " + iq.getSQL(ec.getDBSession()));
//			((SQLException) dbx.getCause()).getNextException().printStackTrace();
			dbx.printStackTrace();
		}
	}

}
