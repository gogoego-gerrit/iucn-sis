package org.iucn.sis.server.crossport.export;

import java.io.IOException;
import java.util.Queue;

import javax.naming.NamingException;

import org.iucn.sis.shared.taxonomyTree.CommonNameData;
import org.iucn.sis.shared.taxonomyTree.SynonymData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.CanonicalColumnName;
import com.solertium.db.DBException;
import com.solertium.db.Row;
import com.solertium.db.StringLiteral;
import com.solertium.db.Row.Set;
import com.solertium.db.query.DeleteQuery;
import com.solertium.db.query.InsertQuery;
import com.solertium.db.query.QComparisonConstraint;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.Query;
import com.solertium.db.query.SelectQuery;
import com.solertium.db.query.UpdateQuery;

public class DBMirrorTaxonProcessor extends DBMirrorCommodityProcessor<TaxonNode> {

	private TaxonNode taxon;

	public DBMirrorTaxonProcessor(Queue<TaxonNode> queue, String threadID) throws DBException, IOException, NamingException {
		super(queue, threadID);
	}

	@Override
	protected void process(TaxonNode data) {
		this.taxon = data;
		try {
			process();

			int parentID = taxon.getParentId().matches("\\d+") ? Integer.parseInt(taxon.getParentId()) : -1;

			Row r = new Row();
			r.add(new CInteger("id", taxon.getId()));
			r.add(new CInteger("parent_tax_id", parentID));
			r.add(new CString("name", taxon.getFullName()));
			r.add(new CString("taxonomic_authority", taxon.getTaxonomicAuthority()));
			r.add(new CString("status", taxon.getStatus()));
			r.add(new CString("level", taxon.getLevelString()));
			r.add(new CString("hybrid", taxon.isHybrid()+""));

			insertOrUpdate("taxonomy", "id", new QComparisonConstraint(new CanonicalColumnName(
					"taxonomy", "id"), QConstraint.CT_EQUALS, taxon.getId()), r);
		} catch (final DBException dbx) {
			dbx.printStackTrace();
		}
		this.taxon = null;
	}

	private String formatLiteral(String prop) {
		return ec.formatLiteral(new StringLiteral(prop == null ? "" : prop));
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

	private void populateAssessed() {
		Row r = new Row();
		r.add(new CInteger("tax_id", taxon.getId()));
		String[] tableNames = new String[] { "kingdom", "phylum", "class", "order", "family", "genus", "species",
				"infrarank", "infratype", "subpopulation", "friendly_name" };

		for (int i = 0; i <= 5; i++) {
			// Kingdom -> Species
			r.add(new CString(tableNames[i], taxon.getFootprint()[i] ));
		}
		
		if( taxon.getLevel() == TaxonNode.INFRARANK || taxon.getLevel() == TaxonNode.INFRARANK_SUBPOPULATION ) {
			r.add(new CString(tableNames[6], taxon.getFootprint()[6] ));
			r.add(new CString(tableNames[7], taxon.getInfrarankType() == TaxonNode.INFRARANK_TYPE_VARIETY ? "var." : "ssp."));
			
			if( taxon.getLevel() == TaxonNode.INFRARANK_SUBPOPULATION ) {
				r.add(new CString(tableNames[8], taxon.getFootprint()[7]));
				r.add(new CString(tableNames[9], taxon.getName()));
			} else {
				r.add(new CString(tableNames[8], taxon.getName()));
				r.add(new CString(tableNames[9], null));
			}
			
		} else if( taxon.getLevel() == TaxonNode.SUBPOPULATION ) {
			r.add(new CString(tableNames[6], taxon.getFootprint()[6] ));
			r.add(new CString(tableNames[7], null));
			r.add(new CString(tableNames[8], null));
			r.add(new CString(tableNames[9], taxon.getName()));
		} else {
			r.add(new CString(tableNames[6], taxon.getName() ));
			r.add(new CString(tableNames[7], null)); // Infrarank == null
			r.add(new CString(tableNames[8], null)); // Infratype == null
			r.add(new CString(tableNames[9], null)); // Subpop == null
		}

		r.add(new CString("friendly_name", taxon.getFullName()));
		try {
			insertOrUpdate("assessed", "tax_id", new QComparisonConstraint(new CanonicalColumnName(
					"assessed", "tax_id"), QConstraint.CT_EQUALS, taxon.getId()), r);
			//				InsertQuery i = new InsertQuery();
			//				i.setTable("assessed");
			//				i.setRow(r);
			//				ec.doUpdate(i);
			// ec.doUpdate(s);
		} catch (final DBException dbx) {
			// System.out.println(s);
			dbx.printStackTrace();
		}
	}

	public void process() throws DBException {
		if( taxon.getAssessments().size() > 0 )
			populateAssessed();

		DeleteQuery del = new DeleteQuery("synonyms", "tax_id", taxon.getId());
		ec.doUpdate(del);

		for( SynonymData curSyn : taxon.getSynonyms() ) {
			// Process synonyms
			String name = curSyn.getName();

			String synlevel;
			if (curSyn.getLevel() == -1)
				synlevel = null;
			else
				synlevel = TaxonNode.getDisplayableLevel(curSyn.getLevel(), curSyn.getInfrarankType());

			String notes = curSyn.getNotes();
			String status = curSyn.getStatus();
			String rlCat = curSyn.getRedListCategory();
			String rlCrit = curSyn.getRedListCriteria();
			String rlDate = curSyn.getRedListDate();
			String genusAuth = curSyn.getAuthority(TaxonNode.GENUS);
			String spcAuth = curSyn.getAuthority(TaxonNode.SPECIES);
			String infraAuth = curSyn.getAuthority(TaxonNode.INFRARANK);

			String[] brokenName = name.split("\\s");

			String genusName = curSyn.getGenus();
			String spcName = curSyn.getSpecie();
			String infraType = curSyn.getInfrarankType()+"";
			String infraName = curSyn.getInfrarank();
			String subpopName = curSyn.getStockName();
			String upperLevelName = curSyn.getUpperLevelName();

			if (synlevel == null || synlevel.equals("") || synlevel.equals("-1")) {
				synlevel = "N/A";

				if (taxon.getLevel() >= TaxonNode.GENUS) {
					genusName = brokenName[0];
					synlevel = TaxonNode.getDisplayableLevel(TaxonNode.GENUS);

					if (brokenName.length > 1) {
						if (brokenName[1].endsWith("ssp.")) {
							name = new StringBuilder(name).insert(name.indexOf("ssp."), " ").toString();

							System.out.println(brokenName[1] + " ends with ssp. New name is " + name);
							brokenName = name.split("\\s");
						} else if (brokenName[1].endsWith("var.")) {
							name = new StringBuilder(name).insert(name.indexOf("var."), " ").toString();

							System.out.println(brokenName[1] + " ends with var. New name is " + name);
							brokenName = name.split("\\s");
						} else if (brokenName[1].endsWith("fma.")) {
							name = new StringBuilder(name).insert(name.indexOf("fma."), " ").toString();

							System.out.println(brokenName[1] + " ends with fma. New name is " + name);
							brokenName = name.split("\\s");
						}

						spcName = brokenName[1];
						synlevel = TaxonNode.getDisplayableLevel(TaxonNode.SPECIES);
					}
					if (brokenName.length > 2) {
						if (brokenName[2].matches("^ssp\\.?$") || brokenName[2].matches("^var\\.?$")
								|| brokenName[2].equals("fma.")) {
							infraType = brokenName[2];
							infraName = brokenName[3];
							for (int i = 4; i < brokenName.length; i++)
								infraName += " " + brokenName[i];

							synlevel = TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK);
						} else if (brokenName.length > 3
								&& (brokenName[3].matches("^ssp\\.?$") || brokenName[3].matches("^var\\.?$") || brokenName[3]
								                                                                                           .equals("fma."))) {
							spcName += " " + brokenName[2];
							infraType = brokenName[3];
							infraName = brokenName[4];
							for (int i = 5; i < brokenName.length; i++)
								infraName += " " + brokenName[i];

							synlevel = TaxonNode.getDisplayableLevel(TaxonNode.INFRARANK);
						} else {
							String whatsLeft = brokenName[2];
							for (int i = 3; i < brokenName.length; i++)
								whatsLeft += " " + brokenName[i];

							if (whatsLeft.toLowerCase().contains("stock")
									|| whatsLeft.toLowerCase().contains("subpopulation")) {
								subpopName = whatsLeft;
								synlevel = TaxonNode.getDisplayableLevel(TaxonNode.SUBPOPULATION);
							} else
								spcName += " " + whatsLeft;
						}
					}
				}
			} else if (synlevel.matches("\\d+")) {
				int intLevel = Integer.valueOf(synlevel);
				synlevel = TaxonNode.getDisplayableLevel(intLevel);
			}

			if (spcName == null)
				spcName = "";
			if (genusName == null)
				genusName = "";
			if (infraName == null)
				infraName = "";
			if (upperLevelName == null)
				upperLevelName = "";
			if (subpopName == null)
				subpopName = "";

			if (infraType == null)
				infraType = "";
			else if (infraType.equals("-1"))
				infraType = "";

			final String query = "insert into synonyms (tax_id,name,level,"
				+ "genus_name,species_name,infra_type,infra_name,stock_name,"
				+ "genus_author,species_author,infrarank_author," + "status,notes,rl_category,"
				+ "rl_criteria,rl_date) values ("
				+ taxon.getId()
				+ ","
				+ formatLiteral(name)
				+ ","
				+ formatLiteral(synlevel)
				+ ","
				+ formatLiteral(genusName)
				+ ","
				+ formatLiteral(spcName)
				+ ","
				+ formatLiteral(infraType)
				+ ","
				+ formatLiteral(infraName)
				+ ","
				+ formatLiteral(subpopName)
				+ ","
				+ formatLiteral(genusAuth)
				+ ","
				+ formatLiteral(spcAuth)
				+ ","
				+ formatLiteral(infraAuth)
				+ ","
				+ formatLiteral(status)
				+ ","
				+ formatLiteral(notes)
				+ ","
				+ formatLiteral(rlCat)
				+ "," + formatLiteral(rlCrit) + "," + formatLiteral(rlDate) + ")";
			try {
				ec.doUpdate(query);
			} catch (final DBException dbx) {
				dbx.printStackTrace();
			}
		}

		del = new DeleteQuery("common_name", "tax_id", taxon.getId());
		ec.doUpdate(del);

		for( CommonNameData curCommonName : taxon.getCommonNames() ) {
			final String iso = curCommonName.getIsoCode();
			final String name = curCommonName.getName();
			// switch "primary" for "principal" because easier to query
			// in SQL
			String primary = null;
			if ("true".equals(curCommonName.isPrimary()))
				primary = "Y";
			else
				primary = "N";
			String validated = null;
			if ("true".equals(curCommonName.isValidated()))
				validated = "Y";
			else
				validated = "N";
			final String s = "insert into common_name (tax_id,common_name,iso_language,principal,validated)"
				+ " values (" + taxon.getId() + "," + ec.formatLiteral(new StringLiteral(name)) + ","
				+ ec.formatLiteral(new StringLiteral(iso)) + ",'" + primary + "','" + validated + "')";
			// System.out.println(s);
			try {
				ec.doUpdate(s);
			} catch (final DBException dbx) {
				dbx.printStackTrace();
			}
		}
	}
}
