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
import org.iucn.sis.shared.api.models.fields.ThreatsSubfield;
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

public class StressesConverter extends FieldMigrationConverter {
	
	public StressesConverter() throws NamingException {
		super("sis_lookups", "sis1_lookups", CanonicalNames.Threats);
	}
	
	@Override
	protected Row getPrototype() {
		Row prototype = new Row();
		prototype.add(new CString("internal_id", null));
		prototype.add(new CString("taxon_id", null));
		prototype.add(new CString("status", null));
		prototype.add(new CString("code", null));
		prototype.add(new CInteger("index", null));
		prototype.add(new CString("stresses", null));
		
		return prototype;
	}
	
	@Override
	protected void correct(final String fieldName) throws DBException {
		synchronized (this) {
			final AtomicInteger count = new AtomicInteger(0);
			
			printf("Running corrections for %s", fieldName);
			
			SelectQuery query = new SelectQuery();
			query.select(getTableName(fieldName), "internal_id", "ASC");
			query.select(getTableName(fieldName), "*");
			if ("true".equals(parameters.getFirstValue("test"))) {
				query.constrain(new CanonicalColumnName(getTableName(fieldName), "taxon_id"), 
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
						update(fieldName, ref.get(), new HashMap<Integer, Row>(values));
						
						values.clear();
						
						ref.set(internal_id);
					}
					
					values.put(index, row);
						
					if (count.incrementAndGet() % 500 == 0)
						printf("%s...", count.get());
				}
			});
			
			update(fieldName, ref.get(), values);
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
			ThreatsSubfield proxy = new ThreatsSubfield(subfield);
			Integer value = proxy.getThreat();
			if (value == null) {
				printf("!! No lookup value for this field?!");
				continue;
			}
			
			Row row = data.get(value);
			if (row == null)
				printf("Value %s not found in %s", value, data.keySet());
			else {
				count++;
				
				List<Integer> stressValues = null;
				String stresses = row.get("stresses").toString();
				if (stresses != null && !"".equals(stresses)) {
					stressValues = new ArrayList<Integer>();
					for (String current : stresses.split(","))
						stressValues.add(Integer.valueOf(current));
				}
				proxy.setStresses(stressValues);
			}
		}
		
		session.update(field);
		
		commitAndStartTransaction();
		
		if (count != size)
			printf("Only updated %s/%s fields for T%sA%s", count, size, field.getAssessment().getTaxon().getId(), field.getAssessment().getId());
	}
	
	@SuppressWarnings("unchecked")
	protected void process(String fieldName, AssessmentData assessData, Object rawData) throws DBException {
		Map<String, List<String>> dataMap = (Map)rawData;
		for (Entry<String, List<String>> selected : dataMap.entrySet() ) {
			List<String> dataList = selected.getValue();
			dataList.add(0, selected.getKey()); //Add the threat ID back in
			
			if (dataList.size() > 6) {
				Integer numStresses = dataList.get(6).matches("\\d") ? Integer.valueOf(dataList.get(6)) : 0;
				if (numStresses.intValue() > 0) {
					Integer codingOption = getIndex(fieldName, fieldName+"Lookup", fieldName + "Lookup", selected.getKey());
					if (codingOption != null && codingOption.intValue() <= 0)
						codingOption = null;
					
					StringBuilder stressList = new StringBuilder();
					for (int i = 0; i < numStresses.intValue(); i++ )
						stressList.append(Integer.valueOf(dataList.get(7+i)) + ",");
			
					String stresses = stressList.toString();
					stresses = stresses.substring(0, stresses.length() - 1);
					
					Row row = getPrototype();
					row.get("internal_id").setObject(assessData.getAssessmentID());
					row.get("taxon_id").setObject(assessData.getSpeciesID());
					row.get("status").setObject(assessData.isPublished() ? "published" : "draft");
					row.get("code").setObject(selected.getKey());
					row.get("index").setObject(codingOption);
					row.get("stresses").setObject(stresses);
					
					InsertQuery query = new InsertQuery(getTableName(fieldName), row);
					
					print(query.getSQL(ec.getDBSession()));
					
					ec.doUpdate(query);
				}
			}
		}
	}

}
