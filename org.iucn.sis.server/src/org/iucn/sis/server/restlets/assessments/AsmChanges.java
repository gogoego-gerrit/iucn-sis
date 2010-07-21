package org.iucn.sis.server.restlets.assessments;

import org.iucn.sis.shared.api.utils.CanonicalNames;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.InsertQuery;

/**
 * Creates the trigger functions for all assessment changes
 * 
 * @author liz.schwartz
 *
 */
public class AsmChanges {

	public final static String[] addedTables = new String[] {"assessment"};

	public final static String ASM_ID  = "asm_id";
	public final static String EDIT_TABLE_NAME = "asm_edits";
	public final static String DELETED_ASSESSMENTS_TABLE_NAME = "deleted_assessments";
	public final static String UID = "uid";
	public final static String TABLE_NAME_FIELD = "table_name";
	public final static String STRUCTURE_NAME = "name";
	public final static String VALUE = "value";
	public final static String TIMESTAMP = "date";
	public final static String USER = "user";
	public final static String LAST_USER_UPDATED = "last_user_updated";	//USED FOR OTHER TABLES BESIDES EDIT TABLE

	public final static void stopTriggering(ExecutionContext ec) throws DBException {
		try{
			ec.doUpdate("CREATE LANGUAGE pltcl");
		} catch (Exception e) {
			//IF EXCEPTION THROWN HERE, then Language already created.
		}

		for (String tableName : CanonicalNames.allCanonicalNames)
		{
			try{
				ec.doUpdate("DROP TRIGGER trigger_asm_edit ON \"" + tableName +"\"");
			} catch (Exception e) {
				//Probably okay, table doesn't exist yet
				System.out.println("ERROR -- " + e.getLocalizedMessage());
			}
		}
		for (String tableName : addedTables)
		{
			try{
				ec.doUpdate("DROP TRIGGER trigger_asm_edit ON \"" + tableName +"\"");				
			} catch (Exception e) {
				System.out.println("ERROR -- " + e.getLocalizedMessage());	
			}
		}		
	}

	public final static void startTriggering(ExecutionContext ec) throws DBException {
		try{
			ec.doUpdate("CREATE LANGUAGE pltcl");
		} catch (Exception e) {
			//IF EXCEPTION THROWN HERE, then Language already created.
		}

		ec.doUpdate(getTriggerFunction());
		for (String tableName : CanonicalNames.allCanonicalNames)
		{
			try{
				getTriggerCall(ec, tableName);	
			} catch (Exception e) {
				//Probably okay, table doesn't exist yet
				System.out.println("ERROR -- " + e.getLocalizedMessage());
			}
		}
		for (String tableName : addedTables)
		{
			try{
				getTriggerCall(ec, tableName);				
			} catch (Exception e) {
				System.out.println("ERROR -- " + e.getLocalizedMessage());	
			}
		}		
	}

	/**
	 * returns the tigger function for assessment changes
	 * 
	 * @return
	 */
	protected final static String getTriggerFunction() {
		String function = "CREATE OR REPLACE FUNCTION trigger_asm_edit() RETURNS trigger AS $$ \n" +
		"set tstamp \"\"\n" +
		"spi_exec \"SELECT extract(epoch FROM now()) as tstamp\"\n" +
		"switch $TG_op {\n" +
		"	INSERT {\n" +
		"		foreach field $TG_relatts {\n" +
		//		"			if {$field != 'last_user_updated'} {\n" +
		"				set previous_value [lindex [array get OLD $field] 1]\n" +
		"				set current_value [lindex [array get NEW $field] 1]\n" + 	
		"				set modified_field [lindex [array get NEW $field] 0]\n" + 
		"				set asm_id [lindex [array get NEW asm_id] 1] \n" + 
		"				set u_id [lindex [array get NEW uid] 1] \n" + 
		"				set user [lindex [array get NEW last_user_updated] 1] \n" +	
		"				if {! [string equal $previous_value $current_value]} { \n" +
		"				if {! [string equal $modified_field \"asm_id\"]} { \n" +
		"				if {! [string equal $modified_field \"uid\"]} { \n" +
		"				if {! [string equal $modified_field \"last_user_updated\"]} { \n" +
		"					spi_exec -array C \"INSERT INTO asm_edits VALUES ('[quote $asm_id]', '[quote $u_id]', '$1', '$modified_field', '[quote $current_value]', $tstamp, '[quote $user]')\" \n" +
		"				}\n" +
		"				}\n" +
		"				}\n" +
		"				}\n" +
		"		} \n" +
		"	} \n" +
		"	UPDATE {\n" +
		"		foreach field $TG_relatts { \n" +
		//		"			if {$field != 'last_user_updated' && $OLD($field) != $NEW($field)} {\n" +
		"				set previous_value [lindex [array get OLD $field] 1]\n" +
		"				set current_value [lindex [array get NEW $field] 1]\n" + 	
		"				set modified_field [lindex [array get NEW $field] 0]\n" + 
		"				set asm_id [lindex [array get NEW asm_id] 1] \n" + 
		"				set u_id [lindex [array get NEW uid] 1] \n" + 
		"				set user [lindex [array get NEW last_user_updated] 1] \n" +	
		"				if {! [string equal $previous_value $current_value]} { \n" +
		"				if {! [string equal $modified_field \"asm_id\"]} { \n" +
		"				if {! [string equal $modified_field \"uid\"]} { \n" +
		"				if {! [string equal $modified_field \"last_user_updated\"]} { \n" +
		"					spi_exec -array C \"INSERT INTO asm_edits VALUES ('[quote $asm_id]', '[quote $u_id]', '$1', '$modified_field', '[quote $current_value]', $tstamp, '[quote $user]')\" \n" +
		"				}\n" +
		"				}\n" +
		"				}\n" +
		"				}\n" +
		"		} \n" +
		"	} \n" +
		"} \n" +
		"return \"\" \n" +
		"$$ LANGUAGE 'pltcl'";
		System.out.println(function);
		return function;
	}



	/**
	 * Returns the call to the trigger function.  When calling the returned trigger function, the caller must supply
	 * $1 -- username
	 * $2 -- current timestamp
	 * 
	 * @param tableName
	 * @return
	 * @throws DBException 
	 */
	protected final static void getTriggerCall(ExecutionContext ec, String tableName) throws DBException {
		try{
			ec.doUpdate("DROP TRIGGER trigger_asm_edit ON \"" + tableName +"\"");
		} catch (Exception e) {
			System.out.println("ERROR -- CAN'T DROP TRIGGER ON TABLE " + tableName);
		}
		String call = "CREATE TRIGGER trigger_asm_edit AFTER INSERT or UPDATE ON \"" + tableName + "\" FOR EACH ROW EXECUTE PROCEDURE trigger_asm_edit('" + tableName + "');";		
		ec.doUpdate(call);
	}


	public final static InsertQuery getDeletedAssessmentSQL(ExecutionContext ec, String asm_id, String uid, long timestamp, String user, String taxaID) throws DBException {
		final Row strRow = ec.getRow(DELETED_ASSESSMENTS_TABLE_NAME);
		strRow.get(ASM_ID).setObject(asm_id);
		strRow.get(UID).setString(uid);
		strRow.get(TIMESTAMP).setObject(new Long(timestamp));
		strRow.get(USER).setString(user);
		strRow.get("taxa_id").setString(taxaID);
		final InsertQuery iqr = new InsertQuery(DELETED_ASSESSMENTS_TABLE_NAME, strRow);
		return iqr;
	}


}
