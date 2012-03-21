package org.iucn.sis.shared.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.NamingException;

import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.persistance.AssessmentCriteria;
import org.iucn.sis.server.api.persistance.FieldCriteria;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentChange;
import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.fields.ProxyField;
import org.iucn.sis.shared.helpers.AssessmentData;
import org.iucn.sis.shared.helpers.CanonicalNames;

import com.solertium.db.CBoolean;
import com.solertium.db.CDateTime;
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

public class RedListEvaluatedConverter extends FieldMigrationConverter {
		
	public RedListEvaluatedConverter() throws NamingException {
		this("sis_lookups", "sis1_lookups");
	}

	public RedListEvaluatedConverter(String dbSessionName, String sis1DBS) throws NamingException {
		super(dbSessionName, sis1DBS, CanonicalNames.RedListEvaluated);
	}
	
	protected void correct(final String name) throws DBException {
		synchronized (this) {
			final AtomicInteger count = new AtomicInteger(0);
			
			printf("Running corrections for %s", name);
			
			SelectQuery query = new SelectQuery();
			query.select(getTableName(name), "internal_id", "ASC");
			query.select(getTableName(name), "*");
			if ("true".equals(parameters.getFirstValue("test"))) {
				String taxonid = parameters.getFirstValue("taxon", "9");
				query.constrain(new CanonicalColumnName(getTableName(name), "taxon_id"), 
						QConstraint.CT_EQUALS, parameters.getFirstValue("taxon_id", taxonid));
			}
			
			ec.doQuery(query, new RowProcessor() {
				public void process(Row row) {
					String internal_id = row.get("internal_id").toString();
					
					update(name, internal_id, row);
					
						
					if (count.incrementAndGet() % 500 == 0)
						printf("%s...", count.get());
				}
			});
			
			printf("%s...", count.get());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void update(String name, String internal_id, Row row) {
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
		}
		
		if (field == null) {
			Assessment assessment = getAssessment(internal_id);
			if (assessment == null) {
				printf("No unique assessment found for %s", internal_id);
				return;
			}
			field = new Field(CanonicalNames.RedListEvaluated, assessment);
			assessment.getField().add(field);
		}
		
		ProxyField proxy = new ProxyField(field);
		proxy.setBooleanPrimitiveField("isEvaluated", "Y".equals(row.get("isEvaluated").getString()), Boolean.FALSE);
		proxy.setDatePrimitiveField("date", row.get("date").getDate());
		proxy.setForeignKeyPrimitiveField("status", row.get("status").getInteger(), name + "_statusLookup");
		proxy.setTextPrimitiveField("reasons", row.get("reasons").getString());
		proxy.setTextPrimitiveField("improvementsNeeded", row.get("improvementsNeeded").getString());
		
		if (field.hasData()) {
			if (field.getId() == 0)
				session.save(field);
			else
				session.update(field);
		}
		if ("true".equals(parameters.getFirstValue("test"))) {
			printf("Has data? %s -- %s", field.hasData(), field.toXML());
		}
		
		commitAndStartTransaction();
	}
	
	protected Row getPrototype() {
		Row prototype = new Row();
		prototype.add(new CString("internal_id", null));
		prototype.add(new CString("taxon_id", null));
		prototype.add(new CString("type", null));
		prototype.add(getTextColumn("array"));
		prototype.add(new CBoolean("isEvaluated", false));
		prototype.add(new CDateTime("date", null));
		prototype.add(new CInteger("status", -1));
		prototype.add(getTextColumn("reasons"));
		prototype.add(getTextColumn("improvementsNeeded"));
		
		return prototype;
	}
	
	@SuppressWarnings("unchecked")
	protected void process(String fieldName, AssessmentData assessData, Object rawData) throws DBException {
		Row row = getPrototype();
		row.get("internal_id").setObject(assessData.getAssessmentID());
		row.get("taxon_id").setObject(assessData.getSpeciesID());
		row.get("type").setObject(assessData.isPublished() ? "published" : "draft");
					
		ArrayList<String> data = new ArrayList<String>((List<String>)rawData);
		data.ensureCapacity(5);
					
		row.get("array").setObject(toStr(data.toString()));
		row.get("isEvaluated").setObject(toBool(get(data, 0)));
		row.get("date").setObject(toDate(get(data, 1)));
		row.get("status").setObject(toInt(get(data, 2)));
		row.get("reasons").setObject(toStr(get(data, 3)));
		row.get("improvementsNeeded").setObject(toStr(get(data, 4)));
		
		InsertQuery query = new InsertQuery(getTableName(fieldName), row);
					
		print(query.getSQL(ec.getDBSession()));
					
		ec.doUpdate(query);				
	}
	
	private String get(List<String> data, int index) {
		String value;
		try {
			value = data.get(index);
		} catch (Exception e) {
			value = null;
		}
		return value;
	}
	
}
