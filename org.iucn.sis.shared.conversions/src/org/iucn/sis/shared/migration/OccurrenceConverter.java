package org.iucn.sis.shared.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.NamingException;

import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.persistance.AssessmentCriteria;
import org.iucn.sis.server.api.persistance.FieldCriteria;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.helpers.AssessmentData;
import org.iucn.sis.shared.helpers.CanonicalNames;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.TrivialExceptionHandler;

public class OccurrenceConverter extends FieldMigrationConverter {
	
	public OccurrenceConverter() throws NamingException {
		super("sis_lookups", "sis1_lookups", CanonicalNames.CountryOccurrence, CanonicalNames.FAOOccurrence, CanonicalNames.LargeMarineEcosystems);
	}
	
	protected void correct(final String name) throws DBException {
		synchronized (this) {
			final AtomicInteger count = new AtomicInteger(0);
			
			printf("Running corrections for %s", name);
			
			SelectQuery query = new SelectQuery();
			query.select(getTableName(name), "internal_id", "ASC");
			query.select(getTableName(name), "*");
			if ("true".equals(parameters.getFirstValue("test"))) {
				query.constrain(new CanonicalColumnName(getTableName(name), "taxon_id"), 
						QConstraint.CT_EQUALS, parameters.getFirstValue("taxon_id", "171912"));
			}
			
			final AtomicReference<String> ref = new AtomicReference<String>("");
			final Map<Integer, Row> values = new ConcurrentHashMap<Integer, Row>();
			
			ec.doQuery(query, new RowProcessor() {
				public void process(Row row) {
					String current = ref.get();
					String internal_id = row.get("internal_id").toString();
					Integer index = row.get("index").getInteger();
					
					if ("".equals(current)) {
						ref.set(internal_id);
					}
					else if (!internal_id.equals(current)) {
						update(name, ref.get(), new HashMap<Integer, Row>(values));
						
						values.clear();
						
						ref.set(internal_id);
					}
					
					values.put(index, row);
						
					if (count.incrementAndGet() % 500 == 0)
						printf("%s...", count.get());
				}
			});
			
			update(name, ref.get(), values);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void update(String name, String internal_id, Map<Integer, Row> data) {
		FieldCriteria criteria = new FieldCriteria(session);
		criteria.name.eq(name);
		
		AssessmentCriteria asmCrit = criteria.createAssessmentCriteria();
		asmCrit.internalId.eq(internal_id);
		
		Field field = null;
		try  {
			field = criteria.uniqueField();
		} catch (Exception e) {
			TrivialExceptionHandler.ignore(this, e);
		}
		
		if (field == null) {
			printf("No unique field found for Internal ID %s", internal_id);
			return;
		}
		
		List<AssessmentChange> changes = session.createCriteria(AssessmentChange.class)
			.add(Restrictions.eq("assessment", field.getAssessment()))
			.add(Restrictions.eq("fieldName", name))
			.list();
		if (!changes.isEmpty()) {
			printf("Not updating field for T%sA%s; has updates", 
				field.getAssessment().getTaxon().getId(), field.getAssessment().getId());
			return;
		}
		
		int count = 0, size = field.getFields().size();
		for (Field subfield : field.getFields()) {
			ProxyField proxy = new ProxyField(subfield);
			Integer value = proxy.getForeignKeyPrimitiveField(name + "Lookup");
			if (value == null) {
				printf("!! No lookup value for this field?!");
				continue;
			}
			
			Row row = data.get(value);
			if (row == null)
				printf("Value %s not found in %s", value, data.keySet());
			else {
				count++;
				
				proxy.setForeignKeyPrimitiveField("presence", row.get("presence").getInteger());
				proxy.setBooleanUnknownPrimitiveField("formerlyBred", row.get("formerlybred").getInteger());
				proxy.setForeignKeyPrimitiveField("origin", row.get("origin").getInteger());
				
				List<Integer> seasonalityValues = null;
				String seasonality = row.get("seasonality").toString();
				if (seasonality != null && !"".equals(seasonality)) {
					seasonalityValues = new ArrayList<Integer>();
					for (String current : seasonality.split(","))
						seasonalityValues.add(Integer.valueOf(current));
				}
				proxy.setForeignKeyListPrimitiveField("seasonality", seasonalityValues);
			}
		}
		
		session.update(field);
		
		commitAndStartTransaction();
		
		if (count != size)
			printf("Only updated %s/%s fields for T%sA%s", count, size, field.getAssessment().getTaxon().getId(), field.getAssessment().getId());
	}
	
	protected Row getPrototype() {
		Row prototype = new Row();
		prototype.add(new CString("internal_id", null));
		prototype.add(new CString("taxon_id", null));
		prototype.add(new CString("status", null));
		prototype.add(new CString("code", null));
		prototype.add(new CInteger("index", null));
		prototype.add(new CInteger("presence", 0));
		prototype.add(new CInteger("formerlybred", 0));
		prototype.add(new CInteger("origin", 0));
		prototype.add(new CString("seasonality", null));
		
		return prototype;
	}
	
	@SuppressWarnings("unchecked")
	protected void process(String name, AssessmentData assessData, Object rawData) throws DBException {
		HashMap<String, ArrayList<String>> map = operateOn(assessData, (HashMap) rawData);
		if (map != null) {
			for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
				Integer codingOption = getIndex(name, name + "Lookup", name + "Lookup", entry.getKey());
				if (codingOption != null && codingOption.intValue() <= 0)
					codingOption = null;
						
				Row row = getPrototype();
				row.get("internal_id").setObject(assessData.getAssessmentID());
				row.get("taxon_id").setObject(assessData.getSpeciesID());
				row.get("status").setObject(assessData.isPublished() ? "published" : "draft");
				row.get("code").setObject(entry.getKey());
				row.get("index").setObject(codingOption);
				row.get("presence").setObject(toInt(entry.getValue().get(0)));
				row.get("formerlybred").setObject(toInt(entry.getValue().get(1)));
				row.get("origin").setObject(toInt(entry.getValue().get(2)));
				row.get("seasonality").setObject(toStr(entry.getValue().get(3)));
				
				InsertQuery query = new InsertQuery(getTableName(name), row);
				
				print(query.getSQL(ec.getDBSession()));
				
				ec.doUpdate(query);
			}
		}
	}
	
	/**
	 * This will migrate occurrence data from the old format (included passage
	 * migrant as its own check box) to the new format, with three select boxes.
	 * 
	 * @param data
	 *            Assessment to work on
	 * @return true/false, if changes were made to the assessment
	 */
	private HashMap<String, ArrayList<String>> operateOn(AssessmentData data, HashMap<String, ArrayList<String>> rawData) {
		for (Entry<String, ArrayList<String>> curSelected : rawData.entrySet()) {
			if (curSelected.getValue().size() == 4) { // This is already modded
				//Continue to check origin since it can't hurt...
				String origin = curSelected.getValue().get(2);
				if (origin.equals("9"))
					curSelected.getValue().set(2, "6");
			} else {
				modifyOccurrenceEntry(curSelected);
			}
		}
		
		return rawData;
	}

	private void modifyOccurrenceEntry(Entry<String, ArrayList<String>> curSelected) {
		curSelected.getValue().ensureCapacity(4);

		String presenceCode = curSelected.getValue().get(0);
		String passageMigrant = curSelected.getValue().get(1);
		String origin = curSelected.getValue().get(2);

		String seasonality = "";

		if (!presenceCode.equals("") && !presenceCode.equals("0")) {
			int pCode = Integer.valueOf(presenceCode);
			if (pCode <= 3) {
				curSelected.getValue().set(0, "1");

				if (pCode == 1)
					seasonality += "1,";
				else if (pCode == 2)
					seasonality += "2,";
				else if (pCode == 3)
					seasonality += "3,";
			} else if (pCode == 4)
				curSelected.getValue().set(0, "2");
			else if (pCode == 5)
				curSelected.getValue().set(0, "3");
			else if (pCode == 6)
				curSelected.getValue().set(0, "4");
		} else {
			curSelected.getValue().set(0, "0");
		}

		//Formerly bred is unseleted
		curSelected.getValue().set(1, "0");

		if (passageMigrant.equals("true"))
			seasonality += "4";

		if (!origin.equals("") && !origin.equals("0")) {
			int oCode = Integer.valueOf(origin);

			if (oCode == 1)
				curSelected.getValue().set(2, "1");
			else if (oCode == 2)
				curSelected.getValue().set(2, "3");
			else if (oCode == 3)
				curSelected.getValue().set(2, "2");
			else if (oCode == 4)
				curSelected.getValue().set(2, "5");
			else if (oCode == 5)
				curSelected.getValue().set(2, "6");
			else if (oCode == 9) // This shouldn't be in there, but somehow a
				// few are...
				curSelected.getValue().set(2, "6");
		} else
			curSelected.getValue().set(2, "0");

		if (seasonality.endsWith(","))
			seasonality = seasonality.substring(0, seasonality.length() - 1);

		curSelected.getValue().add(seasonality);
	}

}
