package org.iucn.sis.server.utils.scripts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.iucn.sis.server.crossport.export.DBMirrorManager;
import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.data.assessments.AssessmentParser;
import org.iucn.sis.shared.data.assessments.CanonicalNames;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.Row.Set;
import com.solertium.lwxml.factory.NativeDocumentFactory;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VersionedVFS;

public class SetDataRepairer {

	private static Long checkFromDate = Long.valueOf(1264507200);
	
	public static void doRepair(VFS vfs, boolean writeback) throws NamingException, DBException {
		ExecutionContext ec = new SystemExecutionContext(DBMirrorManager.DS);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);

		List<String> manualCheck =  new ArrayList<String>();
		List<String> questionable =  new ArrayList<String>();
		String [] checkTables = new String [] { "UseTradeDetails", "Livelihoods" };

		String selectUsers = "SELECT DISTINCT \"user\" FROM asm_edits WHERE date > " + checkFromDate;

		System.out.println("Writeback is " + writeback + ": hurry up and kill the server if you did not " 
				+ "mean to supply that argument!");
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO: handle exception
		}

		Set set = new Set();
		ec.doQuery(selectUsers, set);

		for( Row curUserRow : set.getSet() ) {
			String user = curUserRow.get("user").getString();

			for(String curTable : checkTables) {
				String selectAsms = "SELECT DISTINCT uid, date FROM asm_edits WHERE date > " + checkFromDate
				+ " AND \"user\"='" + user + "' AND table_name='" + curTable + "' ORDER BY date ASC";
				Set asmSet = new Set();
				ec.doQuery(selectAsms, asmSet);

				List<String> asmList = new ArrayList<String>();

				for(Row curAsmRow : asmSet.getSet())
					if( !asmList.contains(curAsmRow.get("uid").getString()) )
						asmList.add(curAsmRow.get("uid").getString());

				if( asmList.size() == 0 )
					continue;

				if( asmList.size() > 0 )
					manualCheck.add(asmList.get(0) + "-" + curTable);

				HashMap<AssessmentData, String> toRepair = new HashMap<AssessmentData, String>();
				List<String> empty = new ArrayList<String>();
				if( curTable.equalsIgnoreCase(CanonicalNames.Livelihoods))
					empty.add("false");
				empty.add("0");
				
				System.out.println("--------------\nFor user's " + curTable + " data: " + user);
				for(int i = 1; i < asmList.size(); i++) {
					String [] split = asmList.get(i-1).split("_");
					AssessmentData one = AssessmentIO.readAssessment(vfs, split.length == 3 ? split[0] : split[0]+"_"+split[1], 
							split.length == 3 ? split[1]+"_"+split[2] : split[2]+"_"+split[3], "");

					split = asmList.get(i).split("_");
					AssessmentData two = AssessmentIO.readAssessment(vfs, split.length == 3 ? split[0] : split[0]+"_"+split[1], 
							split.length == 3 ? split[1]+"_"+split[2] : split[2]+"_"+split[3], "");

					if( one != null && two != null ) {
						List<String> oneData = (List<String>)one.getDataMap().get(curTable);
						List<String> twoData = (List<String>)two.getDataMap().get(curTable);

						if( oneData != null && twoData != null && oneData.equals(twoData) && !oneData.equals(empty) ) {
							if( hadDataInHistory((VersionedVFS)vfs, curTable, two) ) {
								System.out.println("LOOKS LIKE " + two.getAssessmentID() + " was an original.");
								questionable.add(one.getAssessmentID());
							} else {
								System.out.println(curTable + " for " + two.getAssessmentID() + " dupe of " + one.getAssessmentID());
								toRepair.put(two, curTable);
							}
						}
					}
				}

				//				System.out.println("--------------\nFor user: " + user + 
				//						"\nFull List of " + curTable + " entries to repair: " + Arrays.toString(toRepair.keySet().toArray()));

				for( Entry<AssessmentData, String> repair : toRepair.entrySet() ) {
					repair.getKey().getDataMap().put(repair.getValue(), empty);
					if( writeback )
						AssessmentIO.writeAssessment(repair.getKey(), "repairScript", vfs, false);
				}
			}

			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		System.out.println("\n===========\nTo manual check: " + Arrays.toString(manualCheck.toArray()));
		System.out.println("\n===========\nQuestionable originals: " + Arrays.toString(questionable.toArray()));
	}

	private static boolean hadDataInHistory(VersionedVFS vfs, String canonicalName, AssessmentData duplicate) {
		VFSPath path = new VFSPath(ServerPaths.getPathForAssessment(duplicate, null));
		
		try {
			List<String> duplicateRevIDs = vfs.getRevisionIDsBefore(path, null, -1);
			int i = 0;
			while( i < duplicateRevIDs.size() && ( (vfs.getLastModified(path, duplicateRevIDs.get(i))/1000)  > checkFromDate.longValue()))
				i++;

			if( i < duplicateRevIDs.size() ) {
				NativeDocument ndoc = NativeDocumentFactory.newNativeDocument();
				BufferedReader reader = new BufferedReader(new InputStreamReader(vfs.getInputStream(path, duplicateRevIDs.get(i))));
				StringBuilder xml = new StringBuilder();
				String temp;
				while( (temp = reader.readLine()) != null )
					xml.append(temp + "\n");

				ndoc.parse(xml.toString());
				AssessmentData oldDuplicate = new AssessmentParser(ndoc).getAssessment(); 
				if( oldDuplicate.getDataMap().containsKey(canonicalName) )
					return oldDuplicate.getDataMap().get(canonicalName).equals(duplicate.getDataMap().get(canonicalName));
			}
			
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
