package org.iucn.sis.shared.conversions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;

public class CorrectTextFields extends Converter {
	
	private boolean sql = false;
	private boolean update = false;
	
	@Override
	protected void run() throws Exception {
		update = "true".equals(parameters.getFirstValue("update"));
		sql = "true".equals(parameters.getFirstValue("sql"));
		
		ExecutionContext ec = SIS.get().getExecutionContext();
		run(ec, CanonicalNames.ThreatsDocumentation, "SpcTxtThreats", "SpcTxtRecID");
		run(ec, CanonicalNames.PopulationDocumentation, "SpcRedJustPop", "SpcRedRecID");
		run(ec, CanonicalNames.RangeDocumentation, "SpcTxtRange", "SpcTxtRecID");
		run(ec, CanonicalNames.HabitatDocumentation, "SpcTxtEcology", "SpcTxtRecID");
		run(ec, CanonicalNames.ConservationActionsDocumentation, "SpcTxtAction", "SpcTxtRecID");
		run(ec, CanonicalNames.UseTradeDocumentation, "SpcTxtTargets", "SpcTxtRecID");
		run(ec, CanonicalNames.RedListRationale, "SpcRedJustShort", "SpcRedRecID");
		run(ec, CanonicalNames.TaxonomicNotes, "SpcTaxaNotes", "SpcRecID");
		run(ec, "Taxon"+CanonicalNames.TaxonomicNotes, "SpcTaxaNotes", "SpcRecID");
	}
	
	private void run(ExecutionContext ec, String table, String column, String idCol) throws DBException {
		run(ec, table, column, idCol, "text_primitive_field", "value", "id");
	}
	
	private void run(final ExecutionContext ec, final String table, final String column, final String idCol, 
			final String updateTbl, final String updateColumn, final String updateIdCol) throws DBException {
		final AtomicInteger unbalanced = new AtomicInteger(0);
		
		final SelectQuery query = new SelectQuery();
		query.select(table, column);
		query.select(table, idCol);
		
		synchronized(query) {
			ec.doQuery(query, new RowProcessor() {
				public void process(Row row) {
					String value = row.get(column).toString();
					if (!isBalanced(value)) {
						unbalanced.incrementAndGet();
						String fixed;
						try {
							fixed = resolve(value);
						} catch (Exception e) {
							System.out.println("Error on row " + row.get(idCol));
							e.printStackTrace();
							//throw new DBException(e);
							return;
						}
						println(" -- fixed -- \n" + fixed + "\n-------");
						
						//if (update) {
							final Row toUpdate = new Row();
							toUpdate.add(new CString(updateColumn, fixed));
							
							UpdateQuery fix = new UpdateQuery();
							fix.setTable(updateTbl);
							fix.setRow(toUpdate);
							fix.constrain(new CanonicalColumnName(updateTbl, updateIdCol), QConstraint.CT_EQUALS, row.get(idCol).getInteger());
							
							if (update) {
								try {
									ec.doUpdate(fix);
								} catch (Exception e) { 
									e.printStackTrace();
								}
							}
							if (sql) {
								print(fix.getSQL(ec.getDBSession())+";");
								
							}
						//}
					}
				}
			});
			
			print("Found " + unbalanced + " unbalanced rows for " + table + "." + column);
		}
	}
	
	private boolean isBalanced(String value) {
		int left = 0;
		int right = 0;
		
		for (Character c : value.toCharArray()) {
			switch (c) {
			case '<':
				left++;
				break;
			case '>':
				right++;
				break;
			default: 
				break;
			}
		}
		
		if (left != right)
			println("-Unbalanced-\n" + value + "\n------");
		
		return left == right;
	}
	
	public static String resolve(String value) {
		//First, scan for > tags ... if it's not yet open then it is a &gt;
		
		boolean open = false;
		int index = 0;
		StringBuilder pass1 = new StringBuilder();
		char[] chars = value.toCharArray();
		try {
			for (Character c : chars) {
				switch (c) {
				case '<': open = !Character.isDigit(chars[index+1]); pass1.append(c); index++; break;
				case '>': if (open) pass1.append(c); else pass1.append("&gt;"); open = false; index++; break;
				default: pass1.append(c); index++; break;
				}
			}
		} catch (IndexOutOfBoundsException e) {
			if (index+1<chars.length) {
				println("Len is " + chars.length + " and index + 1 is " + (index+1));
				throw e;
			}
		}
		
		//Now, scan for tags not closed...
		int lastUnclosedIndex = -1;
		ArrayList<Integer> problems = new ArrayList<Integer>();
		index = 0;
		for (Character c : pass1.toString().toCharArray()) {
			switch (c) {
			case '<': if (lastUnclosedIndex != -1) problems.add(lastUnclosedIndex); lastUnclosedIndex = index; index++; break;
			case '>': lastUnclosedIndex = -1; index++; break;
			default: index++;
			}
		}
		if (lastUnclosedIndex != -1)
			problems.add(lastUnclosedIndex);
		
		String finalValue = pass1.toString();
		Collections.reverse(problems);
		if (!problems.isEmpty())
			println("Found problems: " + problems);
		for (int problem : problems) {
			String pass = finalValue;
			pass = pass.substring(0, problem) + "&lt;";
			if (problem < finalValue.length() - 2)
				pass += finalValue.substring(problem + 1);
			
			finalValue = pass;
		}
		
		return finalValue;
	}
	
	private static void println(String out) {
		//System.out.println(out);
	}

}
