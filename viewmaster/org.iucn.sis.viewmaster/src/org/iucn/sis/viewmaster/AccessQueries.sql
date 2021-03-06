------------------------------------------------------------------------------------------------------------
-- Base Views
------------------------------------------------------------------------------------------------------------
CREATE TABLE $schema.taxon AS 
 SELECT taxon.* FROM taxon
 JOIN $schema.vw_filter vf ON taxon.id = vf.taxonid;

-- BASE ASSESSMENTS -- FILTER TAXON STATUS, ASM STATUS
CREATE TABLE $schema.vw_assessments AS
 SELECT DISTINCT vf.taxonid, vf.assessmentid, a.assessment_typeid
 FROM $schema.vw_redlistcategoryandcriteria vf
 JOIN assessment a ON vf.assessmentid = a.id
 JOIN taxon t ON a.taxonid = t.id
 WHERE t.state = 0 and a.state = 0 
  AND t.taxon_statusid <> 2 AND t.taxon_statusid <> 3
  AND vf.rlcategory <> 'NE' AND vf.rlcategory <> 'NR';
   
-- DRAFT GLOBAL ASSESSMENTS
CREATE TABLE "$schema".vw_draft_global_assessment AS 
 SELECT DISTINCT f.taxonid, f.assessmentid as id
 FROM $schema.vw_assessments f
 LEFT JOIN $schema."REGIONINFORMATION" r ON r.assessmentid = f.assessmentid
 WHERE f.assessment_typeid <> 1  
  AND r.regions = 1;

------------------------------------------------------------------------------------------------------------          
-- DRAFT REGIONAL ASSESSMENTS
CREATE TABLE "$schema".vw_draft_regional_assessment AS 
 SELECT DISTINCT f.taxonid, f.assessmentid as id, reg.name as region_name, r.endemic as is_endemic
 FROM $schema.vw_assessments f
 JOIN $schema."REGIONINFORMATION" r ON r.assessmentid = f.assessmentid
 JOIN public.region reg ON reg.id = r.regions
 WHERE f.assessment_typeid <> 1
  AND r.regions <> 1;
   
------------------------------------------------------------------------------------------------------------
-- EXPORT VIEWS
------------------------------------------------------------------------------------------------------------
-- Base Views, exported.
CREATE OR REPLACE VIEW "$schema"."BASE_DRAFT_GLOBAL_ASSESSMENT" AS
 SELECT DISTINCT taxonid, id as assessmentid
   FROM $schema.vw_draft_global_assessment;
   
CREATE OR REPLACE VIEW "$schema"."BASE_DRAFT_REGIONAL_ASSESSMENT" AS
 SELECT DISTINCT taxonid, id as assessmentid
   FROM $schema.vw_draft_regional_assessment;

CREATE OR REPLACE VIEW "$schema".assessment AS
 SELECT a.*
   FROM $schema.vw_assessments f 
   JOIN public.assessment a ON f.assessmentid = a.id;
   
CREATE OR REPLACE VIEW "$schema".all_assessments AS
 SELECT a.*
   FROM $schema.vw_filter vf
   JOIN public.assessment a ON vf.assessmentid = a.id;
   
-- ALL_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_DRAFTS_GLOBAL" AS
SELECT a.taxonid, a.id AS assessmentid, ft.kingdom, ft.phylum, ft.class, ft."order", ft.family, ft.genus, ft.species, ft.infrarank, ft.infratype, ft.subpopulation, 
       taxon.friendly_name, ft.taxonomic_authority, rl.rlcategory AS category, rl.rlcriteria AS criteria, pe.possiblyextinct AS possibly_extinct, pe.possiblyextinctcandidate AS possibly_extinct_wild, 
       rld.value AS assessmentdate, ca.value as assessors, ce.value as evaluators, cc.value as contributors, cf.value as facilitators
  FROM $schema.vw_draft_global_assessment a
  JOIN $schema.vw_footprint ft ON ft.taxonid = a.taxonid
  JOIN $schema.taxon ON taxon.id = a.taxonid
  LEFT JOIN $schema.vw_redlistassessors_publication ca ON ca.assessmentid = a.id
  LEFT JOIN $schema.vw_redlistevaluators_publication ce ON ce.assessmentid = a.id
  LEFT JOIN $schema.vw_redlistcontributors_publication cc ON cc.assessmentid = a.id
  LEFT JOIN $schema.vw_redlistfacilitators_publication cf ON cf.assessmentid = a.id
  LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id
  LEFT JOIN $schema."REDLISTASSESSMENTDATE" rld ON rld.assessmentid = a.id
  LEFT JOIN $schema."REDLISTCRITERIA" pe ON pe.assessmentid = a.id;
   
------------------------------------------------------------------------------------------------------------ 
-- ALL_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_DRAFTS_REGIONAL" AS 
SELECT a.taxonid, a.id AS assessmentid, a.region_name, a.is_endemic, ft.kingdom, ft.phylum, ft.class, ft."order", ft.family, ft.genus, ft.species, ft.infrarank, ft.infratype, 
       ft.subpopulation, taxon.friendly_name, ft.taxonomic_authority, rl.rlcategory AS category, rl.rlcriteria AS criteria, 
       pe.possiblyextinct AS possibly_extinct, pe.possiblyextinctcandidate AS possibly_extinct_wild, 
       rld.value AS assessmentdate, ca.value as assessors, ce.value as evaluators, cc.value as contributors, cf.value as facilitators
  FROM $schema.vw_draft_regional_assessment a
  JOIN $schema.vw_footprint ft ON ft.taxonid = a.taxonid
  JOIN $schema.taxon ON taxon.id = a.taxonid
  LEFT JOIN $schema.vw_redlistassessors_publication ca ON ca.assessmentid = a.id
  LEFT JOIN $schema.vw_redlistevaluators_publication ce ON ce.assessmentid = a.id
  LEFT JOIN $schema.vw_redlistcontributors_publication cc ON cc.assessmentid = a.id
  LEFT JOIN $schema.vw_redlistfacilitators_publication cf ON cf.assessmentid = a.id
  LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id
  LEFT JOIN $schema."REDLISTASSESSMENTDATE" rld ON rld.assessmentid = a.id
  LEFT JOIN $schema."REDLISTCRITERIA" pe ON pe.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------ 
-- ALL_TAXA_CONSERVATION_ACTIONS_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_CONSERVATION_ACTIONS_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, 
    lv2."ID" AS ca_id, 
    lv0."REF" AS ca_ref0, lv0."DESCRIPTION" AS ca_desc0, 
    lv1."REF" AS ca_ref1, lv1."DESCRIPTION" AS ca_desc1, 
    lv2."REF" AS ca_ref2, lv2."DESCRIPTION" AS ca_desc2
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."CONSERVATIONACTIONSSUBFIELD" tbl ON a.id = tbl.assessmentid
   JOIN lookups."CONSERVATIONACTIONSLOOKUP" lv2 ON lv2."ID" = tbl.conservationactionslookup
   LEFT JOIN lookups."CONSERVATIONACTIONSLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."CONSERVATIONACTIONSLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;
   
------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_CONSERVATION_ACTIONS_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_CONSERVATION_ACTIONS_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, 
    lv2."ID" AS ca_id, 
    lv0."REF" AS ca_ref0, lv0."DESCRIPTION" AS ca_desc0, 
    lv1."REF" AS ca_ref1, lv1."DESCRIPTION" AS ca_desc1, 
    lv2."REF" AS ca_ref2, lv2."DESCRIPTION" AS ca_desc2
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."CONSERVATIONACTIONSSUBFIELD" tbl ON a.id = tbl.assessmentid
   JOIN lookups."CONSERVATIONACTIONSLOOKUP" lv2 ON lv2."ID" = tbl.conservationactionslookup
   LEFT JOIN lookups."CONSERVATIONACTIONSLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."CONSERVATIONACTIONSLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_COO_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_COO_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, 
   col."CODE" AS country_code, col."DESCRIPTION" AS country_name, 
   tbl.presence AS presence_id, copl."LABEL" AS presence_description, 
   tbl.origin AS origin_id, cool."LABEL" AS origin_description
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."COUNTRYOCCURRENCESUBFIELD" tbl ON a.id = tbl.assessmentid
   JOIN lookups."COUNTRYOCCURRENCELOOKUP" col ON col."ID" = tbl.countryoccurrencelookup
   LEFT JOIN lookups."COUNTRYOCCURRENCE_ORIGINLOOKUP" cool ON cool."ID" = tbl.origin
   LEFT JOIN lookups."COUNTRYOCCURRENCE_PRESENCELOOKUP" copl ON copl."ID" = tbl.presence
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_COO_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_COO_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, 
   col."CODE" AS country_code, col."DESCRIPTION" AS country_name, 
   tbl.presence AS presence_id, copl."LABEL" AS presence_description, 
   tbl.origin AS origin_id, cool."LABEL" AS origin_description
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."COUNTRYOCCURRENCESUBFIELD" tbl ON a.id = tbl.assessmentid
   JOIN lookups."COUNTRYOCCURRENCELOOKUP" col ON col."ID" = tbl.countryoccurrencelookup
   LEFT JOIN lookups."COUNTRYOCCURRENCE_ORIGINLOOKUP" cool ON cool."ID" = tbl.origin
   LEFT JOIN lookups."COUNTRYOCCURRENCE_PRESENCELOOKUP" copl ON copl."ID" = tbl.presence
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_FAO_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_FAO_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, 
 	col."DESCRIPTION" AS fao_name, tbl.presence AS presence_id, copl."LABEL" AS presence_description, 
 	tbl.origin AS origin_id, cool."LABEL" AS origin_description
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."FAOOCCURRENCESUBFIELD" tbl ON a.id = tbl.assessmentid
   JOIN lookups."FAOOCCURRENCELOOKUP" col ON col."ID" = tbl.faooccurrencelookup
   LEFT JOIN lookups."FAOOCCURRENCE_ORIGINLOOKUP" cool ON cool."ID" = tbl.origin
   LEFT JOIN lookups."FAOOCCURRENCE_PRESENCELOOKUP" copl ON copl."ID" = tbl.presence
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_FAO_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_FAO_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, a.region_name, a.is_endemic, taxon.friendly_name, rl.rlcategory AS category, 
 	col."DESCRIPTION" AS fao_name, tbl.presence AS presence_id, copl."LABEL" AS presence_description, 
 	tbl.origin AS origin_id, cool."LABEL" AS origin_description
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."FAOOCCURRENCESUBFIELD" tbl ON a.id = tbl.assessmentid
   JOIN lookups."FAOOCCURRENCELOOKUP" col ON col."ID" = tbl.faooccurrencelookup
   LEFT JOIN lookups."FAOOCCURRENCE_ORIGINLOOKUP" cool ON cool."ID" = tbl.origin
   LEFT JOIN lookups."FAOOCCURRENCE_PRESENCELOOKUP" copl ON copl."ID" = tbl.presence
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_HABITAT_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_HABITAT_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, lv2."ID" AS habitat_id, 
	lv0."REF" AS habitat_ref0, lv0."DESCRIPTION" AS habitat_desc0, 
	lv1."REF" AS habitat_ref1, lv1."DESCRIPTION" AS habitat_desc1, 
	lv2."REF" AS habitat_ref2, lv2."DESCRIPTION" AS habitat_desc2, 
	su."LABEL" AS suitability, mi."LABEL" AS majorimportance
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."GENERALHABITATSSUBFIELD" tbl ON a.id = tbl.assessmentid	
   JOIN lookups."GENERALHABITATSLOOKUP" lv2 ON lv2."ID" = tbl.generalhabitatslookup
   LEFT JOIN lookups."GENERALHABITATSLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."GENERALHABITATSLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN lookups."GENERALHABITATS_SUITABILITYLOOKUP" su ON su."ID" = tbl.suitability
   LEFT JOIN lookups."GENERALHABITATS_MAJORIMPORTANCELOOKUP" mi ON mi."ID" = tbl.majorimportance
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id; 

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_HABITAT_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_HABITAT_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, a.region_name, a.is_endemic, taxon.friendly_name, rl.rlcategory AS category, lv2."ID" AS habitat_id, 
	lv0."REF" AS habitat_ref0, lv0."DESCRIPTION" AS habitat_desc0, 
	lv1."REF" AS habitat_ref1, lv1."DESCRIPTION" AS habitat_desc1, 
	lv2."REF" AS habitat_ref2, lv2."DESCRIPTION" AS habitat_desc2, 
	su."LABEL" AS suitability, mi."LABEL" AS majorimportance
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."GENERALHABITATSSUBFIELD" tbl ON a.id = tbl.assessmentid   
   JOIN lookups."GENERALHABITATSLOOKUP" lv2 ON lv2."ID" = tbl.generalhabitatslookup
   LEFT JOIN lookups."GENERALHABITATSLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."GENERALHABITATSLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN lookups."GENERALHABITATS_SUITABILITYLOOKUP" su ON su."ID" = tbl.suitability
   LEFT JOIN lookups."GENERALHABITATS_MAJORIMPORTANCELOOKUP" mi ON mi."ID" = tbl.majorimportance
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_LME_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_LME_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, col."DESCRIPTION" AS lme_name, 
	tbl.presence AS presence_id, copl."LABEL" AS presence_description, tbl.origin AS origin_id, cool."LABEL" AS origin_description
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."LARGEMARINEECOSYSTEMSSUBFIELD" tbl ON a.id = tbl.assessmentid	   
   JOIN lookups."LARGEMARINEECOSYSTEMSLOOKUP" col ON col."ID" = tbl.largemarineecosystemslookup
   LEFT JOIN lookups."LARGEMARINEECOSYSTEMS_ORIGINLOOKUP" cool ON cool."ID" = tbl.origin
   LEFT JOIN lookups."LARGEMARINEECOSYSTEMS_PRESENCELOOKUP" copl ON copl."ID" = tbl.presence
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id; 

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_LME_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_LME_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, a.region_name, a.is_endemic, taxon.friendly_name, rl.rlcategory AS category, 
	col."DESCRIPTION" AS lme_name, tbl.presence AS presence_id, copl."LABEL" AS presence_description, tbl.origin AS origin_id, cool."LABEL" AS origin_description
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."LARGEMARINEECOSYSTEMSSUBFIELD" tbl ON a.id = tbl.assessmentid	      
   JOIN lookups."LARGEMARINEECOSYSTEMSLOOKUP" col ON col."ID" = tbl.largemarineecosystemslookup
   LEFT JOIN lookups."LARGEMARINEECOSYSTEMS_ORIGINLOOKUP" cool ON cool."ID" = tbl.origin
   LEFT JOIN lookups."LARGEMARINEECOSYSTEMS_PRESENCELOOKUP" copl ON copl."ID" = tbl.presence
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_RESEARCH_ACTIONS_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_RESEARCH_ACTIONS_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, lv2."ID" AS res_id, 
	lv0."REF" AS res_ref0, lv0."DESCRIPTION" AS res_desc0, 
	lv1."REF" AS res_ref1, lv1."DESCRIPTION" AS res_desc1, 
	lv2."REF" AS res_ref2, lv2."DESCRIPTION" AS res_desc2
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."RESEARCHSUBFIELD" tbl ON a.id = tbl.assessmentid	   
   JOIN lookups."RESEARCHLOOKUP" lv2 ON lv2."ID" = tbl.researchlookup
   LEFT JOIN lookups."RESEARCHLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."RESEARCHLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id; 

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_RESEARCH_ACTIONS_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_RESEARCH_ACTIONS_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, a.region_name, a.is_endemic, taxon.friendly_name, rl.rlcategory AS category, lv2."ID" AS res_id, 
	lv0."REF" AS res_ref0, lv0."DESCRIPTION" AS res_desc0, 
	lv1."REF" AS res_ref1, lv1."DESCRIPTION" AS res_desc1, 
	lv2."REF" AS res_ref2, lv2."DESCRIPTION" AS res_desc2
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."RESEARCHSUBFIELD" tbl ON a.id = tbl.assessmentid	   
   JOIN lookups."RESEARCHLOOKUP" lv2 ON lv2."ID" = tbl.researchlookup
   LEFT JOIN lookups."RESEARCHLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."RESEARCHLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_STRESSES_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_STRESSES_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, thr."ID" AS threat_id, lv2."ID" AS stresses_id, 
	lv0."REF" AS stresses_ref0, lv0."DESCRIPTION" AS stresses_desc0, 
	lv1."REF" AS stresses_ref1, lv1."DESCRIPTION" AS stresses_desc1, 
	lv2."REF" AS stresses_ref2, lv2."DESCRIPTION" AS stresses_desc2
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."STRESSESSUBFIELD" tbl ON a.id = tbl.assessmentid	   
   LEFT JOIN lookups."THREATSLOOKUP" thr ON thr."ID" = tbl.recordid
   LEFT JOIN lookups."STRESSESLOOKUP" lv2 ON lv2."ID" = tbl.stress
   LEFT JOIN lookups."STRESSESLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."STRESSESLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_STRESSES_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_STRESSES_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, a.region_name, a.is_endemic, rl.rlcategory AS category, 
	thr."ID" AS threat_id, lv2."ID" AS stresses_id, 
	lv0."REF" AS stresses_ref0, lv0."DESCRIPTION" AS stresses_desc0, 
	lv1."REF" AS stresses_ref1, lv1."DESCRIPTION" AS stresses_desc1, 
	lv2."REF" AS stresses_ref2, lv2."DESCRIPTION" AS stresses_desc2
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."STRESSESSUBFIELD" tbl ON a.id = tbl.assessmentid	   
   LEFT JOIN lookups."THREATSLOOKUP" thr ON thr."ID" = tbl.recordid
   LEFT JOIN lookups."STRESSESLOOKUP" lv2 ON lv2."ID" = tbl.stress
   LEFT JOIN lookups."STRESSESLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."STRESSESLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_THREATS_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_THREATS_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, lv2."ID" AS threat_id, 
	lv0."REF" AS threat_ref0, lv0."DESCRIPTION" AS threat_desc0, 
	lv1."REF" AS threat_ref1, lv1."DESCRIPTION" AS threat_desc1, 
	lv2."REF" AS threat_ref2, lv2."DESCRIPTION" AS threat_desc2, 
	tbl.scope AS scope_id, thrsc."LABEL" AS scope_description, tbl.severity AS severity_id, thrse."LABEL" AS severity_description, 
	tbl.timing AS timing_id, thrt."LABEL" AS timing_description
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."THREATSSUBFIELD" tbl ON a.id = tbl.assessmentid	      
   JOIN lookups."THREATSLOOKUP" lv2 ON lv2."ID" = tbl.threatslookup
   LEFT JOIN lookups."THREATSLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."THREATSLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN lookups."THREATS_SCOPELOOKUP" thrsc ON thrsc."ID" = tbl.scope
   LEFT JOIN lookups."THREATS_SEVERITYLOOKUP" thrse ON thrse."ID" = tbl.severity
   LEFT JOIN lookups."THREATS_TIMINGLOOKUP" thrt ON thrt."ID" = tbl.timing
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id; 

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_THREATS_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_THREATS_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, a.region_name, a.is_endemic, taxon.friendly_name, rl.rlcategory AS category, lv2."ID" AS threat_id, 
	lv0."REF" AS threat_ref0, lv0."DESCRIPTION" AS threat_desc0, 
	lv1."REF" AS threat_ref1, lv1."DESCRIPTION" AS threat_desc1, 
	lv2."REF" AS threat_ref2, lv2."DESCRIPTION" AS threat_desc2, 
	tbl.scope AS scope_id, thrsc."LABEL" AS scope_description, tbl.severity AS severity_id, thrse."LABEL" AS severity_description, 
	tbl.timing AS timing_id, thrt."LABEL" AS timing_description
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."THREATSSUBFIELD" tbl ON a.id = tbl.assessmentid
   LEFT JOIN lookups."THREATSLOOKUP" lv2 ON lv2."ID" = tbl.threatslookup
   LEFT JOIN lookups."THREATSLOOKUP" lv1 ON lv1."CODE"::text = lv2."PARENTID"::text
   LEFT JOIN lookups."THREATSLOOKUP" lv0 ON lv0."CODE"::text = lv1."PARENTID"::text
   LEFT JOIN lookups."THREATS_SCOPELOOKUP" thrsc ON thrsc."ID" = tbl.scope
   LEFT JOIN lookups."THREATS_SEVERITYLOOKUP" thrse ON thrse."ID" = tbl.severity
   LEFT JOIN lookups."THREATS_TIMINGLOOKUP" thrt ON thrt."ID" = tbl.timing
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_USE_TRADE_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_USE_TRADE_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, pu."LABEL" AS purpose, so."LABEL" AS source, fr."LABEL" AS form_removed, 
	tbl.subsistence, tbl."national", tbl.international, tbl.possiblethreat
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."USETRADEDETAILSSUBFIELD" tbl ON a.id = tbl.assessmentid
   LEFT JOIN lookups."USETRADEDETAILS_PURPOSELOOKUP" pu ON pu."ID" = tbl.purpose
   LEFT JOIN lookups."USETRADEDETAILS_SOURCELOOKUP" so ON so."ID" = tbl.source
   LEFT JOIN lookups."USETRADEDETAILS_FORMREMOVEDLOOKUP" fr ON fr."ID" = tbl.formremoved
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_USE_TRADE_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_USE_TRADE_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, a.region_name, a.is_endemic, taxon.friendly_name, rl.rlcategory AS category, 
	pu."LABEL" AS purpose, so."LABEL" AS source, fr."LABEL" AS form_removed, tbl.subsistence, tbl."national", tbl.international, tbl.possiblethreat
   FROM $schema.vw_published_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."USETRADEDETAILSSUBFIELD" tbl ON a.id = tbl.assessmentid
   LEFT JOIN lookups."USETRADEDETAILS_PURPOSELOOKUP" pu ON pu."ID" = tbl.purpose
   LEFT JOIN lookups."USETRADEDETAILS_SOURCELOOKUP" so ON so."ID" = tbl.source
   LEFT JOIN lookups."USETRADEDETAILS_FORMREMOVEDLOOKUP" fr ON fr."ID" = tbl.formremoved
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_LIVELIHOOD_DRAFTS_GLOBAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_LIVELIHOOD_DRAFTS_GLOBAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, taxon.friendly_name, rl.rlcategory AS category, l1."LABEL" AS scale, tbl.nameoflocality, tbl.date, 
	tbl.productdescription, tbl.annualharvest, l2."LABEL" AS units_annualharvest, tbl.annualmultispeciesharvest, l3."LABEL" AS units_annualmultispeciesharvest, 
	l4."LABEL" AS user_reliance, l5."LABEL" AS user_age, l6."LABEL" AS socio_economics, l7."LABEL" AS total_popbenifit, l8."LABEL" AS household_consumption, 
	l9."LABEL" AS household_income
   FROM $schema.vw_draft_global_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."LIVELIHOODSSUBFIELD" tbl ON a.id = tbl.assessmentid
   LEFT JOIN lookups."LIVELIHOODS_SCALELOOKUP" l1 ON l1."ID" = tbl.scale
   LEFT JOIN lookups."LIVELIHOODS_UNITSANNUALHARVESTLOOKUP" l2 ON l2."ID" = tbl.unitsannualharvest
   LEFT JOIN lookups."LIVELIHOODS_UNITSANNUALMULTISPECIESHARVESTLOOKUP" l3 ON l3."ID" = tbl.unitsannualmultispeciesharvest
   LEFT JOIN lookups."LIVELIHOODS_HUMANRELIANCELOOKUP" l4 ON l4."ID" = tbl.humanreliance
   LEFT JOIN lookups."LIVELIHOODS_GENDERAGELOOKUP" l5 ON l5."ID" = tbl.genderage
   LEFT JOIN lookups."LIVELIHOODS_SOCIOECONOMICLOOKUP" l6 ON l6."ID" = tbl.socioeconomic
   LEFT JOIN lookups."LIVELIHOODS_TOTALPOPBENEFITLOOKUP" l7 ON l7."ID" = tbl.totalpopbenefit
   LEFT JOIN lookups."LIVELIHOODS_HOUSEHOLDCONSUMPTIONLOOKUP" l8 ON l8."ID" = tbl.householdconsumption
   LEFT JOIN lookups."LIVELIHOODS_HOUSEHOLDINCOMELOOKUP" l9 ON l9."ID" = tbl.householdincome
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;

------------------------------------------------------------------------------------------------------------
-- ALL_TAXA_LIVELIHOOD_DRAFTS_REGIONAL
CREATE OR REPLACE VIEW "$schema"."ALL_TAXA_LIVELIHOOD_DRAFTS_REGIONAL" AS 
 SELECT a.taxonid, a.id AS assessmentid, a.region_name, a.is_endemic, taxon.friendly_name, rl.rlcategory AS category, 
	l1."LABEL" AS scale, tbl.nameoflocality, tbl.date,tbl.productdescription, tbl.annualharvest, l2."LABEL" AS units_annualharvest, tbl.annualmultispeciesharvest, 
	l3."LABEL" AS units_annualmultispeciesharvest, l4."LABEL" AS user_reliance, l5."LABEL" AS user_age, l6."LABEL" AS socio_economics, l7."LABEL" AS total_popbenifit, 
	l8."LABEL" AS household_consumption, l9."LABEL" AS household_income
   FROM $schema.vw_draft_regional_assessment a
   JOIN taxon ON taxon.id = a.taxonid
   JOIN $schema."LIVELIHOODSSUBFIELD" tbl ON a.id = tbl.assessmentid
   LEFT JOIN lookups."LIVELIHOODS_SCALELOOKUP" l1 ON l1."ID" = tbl.scale
   LEFT JOIN lookups."LIVELIHOODS_UNITSANNUALHARVESTLOOKUP" l2 ON l2."ID" = tbl.unitsannualharvest
   LEFT JOIN lookups."LIVELIHOODS_UNITSANNUALMULTISPECIESHARVESTLOOKUP" l3 ON l3."ID" = tbl.unitsannualmultispeciesharvest
   LEFT JOIN lookups."LIVELIHOODS_HUMANRELIANCELOOKUP" l4 ON l4."ID" = tbl.humanreliance
   LEFT JOIN lookups."LIVELIHOODS_GENDERAGELOOKUP" l5 ON l5."ID" = tbl.genderage
   LEFT JOIN lookups."LIVELIHOODS_SOCIOECONOMICLOOKUP" l6 ON l6."ID" = tbl.socioeconomic
   LEFT JOIN lookups."LIVELIHOODS_TOTALPOPBENEFITLOOKUP" l7 ON l7."ID" = tbl.totalpopbenefit
   LEFT JOIN lookups."LIVELIHOODS_HOUSEHOLDCONSUMPTIONLOOKUP" l8 ON l8."ID" = tbl.householdconsumption
   LEFT JOIN lookups."LIVELIHOODS_HOUSEHOLDINCOMELOOKUP" l9 ON l9."ID" = tbl.householdincome
   LEFT JOIN $schema.vw_redlistcategoryandcriteria rl ON rl.assessmentid = a.id;
