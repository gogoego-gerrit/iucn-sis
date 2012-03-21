package org.iucn.sis.shared.migration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.iucn.sis.server.api.persistance.FieldDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.api.utils.CanonicalNames;
import org.iucn.sis.shared.helpers.AssessmentData;

import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.RowProcessor;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.TrivialExceptionHandler;

public class MovementPatternsConverter extends FieldMigrationConverter {
	
	public MovementPatternsConverter() throws NamingException {
		this("sis_lookups", "sis1_lookups");
	}
	
	public MovementPatternsConverter(String dbSessionName, String sis1DBS) throws NamingException {
		super(dbSessionName, sis1DBS, CanonicalNames.MovementPatterns);
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
				String taxonid = parameters.getFirstValue("taxon", "181329");
				query.constrain(new CanonicalColumnName(getTableName(fieldName), "taxon_id"), 
						QConstraint.CT_EQUALS, parameters.getFirstValue("taxon_id", taxonid));
			}
			
			ec.doQuery(query, new RowProcessor() {
				public void process(Row row) {
					String internal_id = row.get("internal_id").toString();
					
					update(fieldName, internal_id, row);
						
					if (count.incrementAndGet() % 500 == 0)
						printf("%s...", count.get());
				}
			});
			
			printf("%s...", count.get());
		}
	}
	
	private void update(String name, String internal_id, Row row) {
		Assessment assessment = getAssessment(internal_id);
		if (assessment == null) {
			printf("No unique assessment found for %s", internal_id);
			return;
		}
		
		for (String toDelete : new String[] { CanonicalNames.MovementPatterns, CanonicalNames.Congregatory }) {
			Field field = assessment.getField(toDelete);
			if (field != null) {
				try {
					FieldDAO.deleteAndDissociate(field, session);
				} catch (PersistentException e) {
					TrivialExceptionHandler.ignore(this, e);
				}
			}
		}
		
		/*
		if (field != null && !"true".equals(parameters.getFirstValue("ignoreChanges", "false"))) {
			List<AssessmentChange> changes = session.createCriteria(AssessmentChange.class)
				.add(Restrictions.eq("assessment", field.getAssessment()))
				.add(Restrictions.eq("fieldName", name))
				.list();
			if (!changes.isEmpty()) {
				String username = changes.get(0).getEdit().getUser().getDisplayableName();
				printf("Not updating field for T%sA%s; has updates from %s", 
					field.getAssessment().getTaxon().getId(), field.getAssessment().getId(), username);
				return;
			}
		}*/
		
		String pattern = row.get("pattern").toString();
		if (pattern != null) {
			Integer value = null;
			boolean isCongregatory = false;
			
			for (String key : pattern.split(",")) {
				if ("1".equals(key))
					value = 4;
				else if ("2".equals(key))
					isCongregatory = true;
				else if ("3".equals(key))
					value = 1;
				else if ("4".equals(key))
					value = 2;
			}
			
			if (value != null) {
				Field field = new Field(CanonicalNames.MovementPatterns, assessment);
				ProxyField proxy = new ProxyField(field);
				proxy.setForeignKeyPrimitiveField("pattern", value, field.getName() + "_patternLookup");
				assessment.setField(field);
				
				session.save(field);
			}
			
			if (isCongregatory) {
				Field field = new Field(CanonicalNames.Congregatory, assessment);
				ProxyField proxy = new ProxyField(field);
				proxy.setForeignKeyPrimitiveField("value", 1, field.getName() + "_valueLookup");
				assessment.setField(field);
				
				session.save(field);
			}
		}
		commitAndStartTransaction();
	}

	@Override
	protected Row getPrototype() {
		Row prototype = new Row();
		prototype.add(new CString("internal_id", null));
		prototype.add(new CString("taxon_id", null));
		prototype.add(new CString("type", null));
		prototype.add(getTextColumn("pattern"));
		
		return prototype;
	}

	@SuppressWarnings("unchecked")
	protected void process(String fieldName, AssessmentData assessData, Object rawData) throws DBException {
		Row row = getPrototype();
		row.get("internal_id").setObject(assessData.getAssessmentID());
		row.get("taxon_id").setObject(assessData.getSpeciesID());
		row.get("type").setObject(assessData.isPublished() ? "published" : "draft");
		
		ArrayList<String> data = new ArrayList<String>((List<String>)rawData);
		StringBuilder csv = new StringBuilder();
		for (Iterator<String> iter = data.listIterator(); iter.hasNext();) {
			csv.append(iter.next());
			csv.append(iter.hasNext() ? "," : "");
		}
		
		String out = csv.toString();
		if (!"".equals(out) && !"0".equals(out)) {
			row.get("pattern").setObject(out);
			InsertQuery query = new InsertQuery(getTableName(fieldName), row);
			
			print(query.getSQL(ec.getDBSession()));
						
			ec.doUpdate(query);
		}
	}

}
