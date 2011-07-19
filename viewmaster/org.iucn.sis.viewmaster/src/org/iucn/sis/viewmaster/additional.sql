DROP VIEW IF EXISTS $schema.vw_reference CASCADE;
CREATE VIEW $schema.vw_reference AS
  SELECT $schema.vw_filter.taxonid, $schema.vw_filter.assessmentid, f.id as fieldid, f.name as fieldname, r.*
  FROM $schema.vw_filter
	JOIN public.field f ON f.assessmentid = $schema.vw_filter.assessmentid
	JOIN public.field_reference ON public.field_reference.fieldid = f.id 
	JOIN public.reference r ON r.id = public.field_reference.referenceid;
GRANT SELECT ON $schema.vw_reference TO $user;

DROP VIEW IF EXISTS lookups.REGIONINFORMATION_REGIONSLOOKUP CASCADE;
CREATE VIEW lookups.REGIONINFORMATION_REGIONSLOOKUP AS 
  SELECT public.region.id AS "ID", CAST (public.region.id AS varchar(255)) AS "NAME", 
  public.region.name AS "LABEL" FROM public.region;
GRANT SELECT ON lookups.REGIONINFORMATION_REGIONSLOOKUP TO PUBLIC;

DROP VIEW IF EXISTS lookups.REDLISTASSESSORS_VALUELOOKUP CASCADE;
CREATE VIEW lookups.REDLISTASSESSORS_VALUELOOKUP AS 
  SELECT public."user".id AS "ID", CAST (public."user".id AS varchar(255)) AS "NAME", 
  public."user".username AS "LABEL" FROM public."user";
GRANT SELECT ON lookups.REDLISTASSESSORS_VALUELOOKUP TO PUBLIC;

DROP VIEW IF EXISTS lookups.REDLISTEVALUATORS_VALUELOOKUP CASCADE;
CREATE VIEW lookups.REDLISTEVALUATORS_VALUELOOKUP AS 
  SELECT public."user".id AS "ID", CAST (public."user".id AS varchar(255)) AS "NAME", 
  public."user".username AS "LABEL" FROM public."user";
GRANT SELECT ON lookups.REDLISTEVALUATORS_VALUELOOKUP TO PUBLIC;

DROP VIEW IF EXISTS lookups.REDLISTCONTRIBUTORS_VALUELOOKUP CASCADE;
CREATE VIEW lookups.REDLISTCONTRIBUTORS_VALUELOOKUP AS 
  SELECT public."user".id AS "ID", CAST (public."user".id AS varchar(255)) AS "NAME", 
  public."user".username AS "LABEL" FROM public."user";
GRANT SELECT ON lookups.REDLISTCONTRIBUTORS_VALUELOOKUP TO PUBLIC;

DROP VIEW IF EXISTS lookups.REDLISTFACILITATORS_VALUELOOKUP CASCADE;
CREATE VIEW lookups.REDLISTFACILITATORS_VALUELOOKUP AS 
  SELECT public."user".id AS "ID", CAST (public."user".id AS varchar(255)) AS "NAME", 
  public."user".username AS "LABEL" FROM public."user";
GRANT SELECT ON lookups.REDLISTFACILITATORS_VALUELOOKUP TO PUBLIC;

DROP VIEW IF EXISTS lookups.THREATS_VIRUSLOOKUP CASCADE;
CREATE VIEW lookups.THREATS_VIRUSLOOKUP AS 
  SELECT public.virus.id AS "ID", CAST(public.virus.id AS varchar(255)) AS "NAME", 
  public.virus.name AS "LABEL" FROM public.virus;
GRANT SELECT ON lookups.THREATS_VIRUSLOOKUP TO PUBLIC;

DROP VIEW IF EXISTS lookups.THREATS_IASLOOKUP CASCADE;
CREATE VIEW lookups.THREATS_IASLOOKUP AS 
  SELECT public.taxon.id AS "ID", public.taxon.name AS "NAME", 
  public.taxon.friendly_name AS "LABEL" FROM public.taxon 
  WHERE taxon.taxon_levelid = 7 AND taxon.state = 0;
GRANT SELECT ON lookups.THREATS_IASLOOKUP TO PUBLIC;

DROP VIEW IF EXISTS $schema.vw_redlistcategoryandcriteria CASCADE;
CREATE VIEW $schema.vw_redlistcategoryandcriteria AS
 SELECT vw.taxonid, vw.assessmentid,
        CASE
            WHEN s1.ismanual = true THEN s1.manualcategory
            ELSE s1.autocategory
        END AS rlcategory,
        CASE
            WHEN s1.ismanual = true THEN s1.manualcriteria
            ELSE s1.autocriteria
        END AS rlcriteria, s1.critversion, s1.ismanual
   FROM $schema.vw_filter vw
   LEFT JOIN ( 
      SELECT v.assessmentid, ff1.value AS autocategory, ff2.value AS autocriteria, 
        ff4.value AS critversion, ff7.value AS ismanual, ff8.value AS manualcategory, 
        ff9.value AS manualcriteria
        FROM $schema.vw_filter v
      JOIN public.field ON public.field.assessmentid = v.assessmentid AND public.field.name::text = 'RedListCriteria'::text
   LEFT JOIN public.primitive_field pf1 ON pf1.fieldid = public.field.id AND pf1.name::text = 'autoCategory'::text
   LEFT JOIN public.string_primitive_field ff1 ON ff1.id = pf1.id
   LEFT JOIN public.primitive_field pf2 ON pf2.fieldid = public.field.id AND pf2.name::text = 'autoCriteria'::text
   LEFT JOIN public.string_primitive_field ff2 ON ff2.id = pf2.id
   LEFT JOIN public.primitive_field pf4 ON pf4.fieldid = public.field.id AND pf4.name::text = 'critVersion'::text
   LEFT JOIN public.foreign_key_primitive_field ff4 ON ff4.id = pf4.id
   LEFT JOIN public.primitive_field pf7 ON pf7.fieldid = public.field.id AND pf7.name::text = 'isManual'::text
   LEFT JOIN public.boolean_primitive_field ff7 ON ff7.id = pf7.id
   LEFT JOIN public.primitive_field pf8 ON pf8.fieldid = public.field.id AND pf8.name::text = 'manualCategory'::text
   LEFT JOIN public.string_primitive_field ff8 ON ff8.id = pf8.id
   LEFT JOIN public.primitive_field pf9 ON pf9.fieldid = public.field.id AND pf9.name::text = 'manualCriteria'::text
   LEFT JOIN public.string_primitive_field ff9 ON ff9.id = pf9.id) s1 ON vw.assessmentid = s1.assessmentid;
GRANT SELECT ON $schema.vw_redlistcategoryandcriteria TO $user;

DROP VIEW IF EXISTS $schema.vw_redlistcaveat_value CASCADE;
CREATE VIEW $schema.vw_redlistcaveat_value AS
 SELECT vw.taxonid, vw.assessmentid, CASE WHEN vw.value IS NULL OR vw.value > '$caveat' THEN 'false' ELSE 'true' END as value
 FROM $schema.vw_redlistassessmentdate_value vw;
GRANT SELECT ON $schema.vw_redlistcaveat_value TO $user;

DROP VIEW IF EXISTS $schema.vw_redlistassessors_publication CASCADE;
CREATE VIEW $schema.vw_redlistassessors_publication AS
 SELECT vw.taxonid, vw.assessmentid, 
  CASE
   WHEN vw.text IS NULL THEN
   CASE
    WHEN u.initials = '' AND u.first_name = '' THEN u.last_name
    WHEN u.initials = '' THEN u.last_name||', '||substring(u.first_name from 1 for 1)||'.'
    ELSE u.last_name||', '||u.initials
   END
   ELSE vw.text
  END AS value
 FROM $schema.vw_redlistassessors vw
 LEFT JOIN public."user" u ON u.id = vw.value
 ORDER BY u.last_name, u.first_name;
GRANT SELECT ON $schema.vw_redlistassessors_publication TO $user;

DROP VIEW IF EXISTS $schema.vw_redlistevaluators_publication CASCADE;
CREATE VIEW $schema.vw_redlistevaluators_publication AS
 SELECT vw.taxonid, vw.assessmentid, 
  CASE
   WHEN vw.text IS NULL THEN
   CASE
    WHEN u.initials = '' AND u.first_name = '' THEN u.last_name
    WHEN u.initials = '' THEN u.last_name||', '||substring(u.first_name from 1 for 1)||'.'
    ELSE u.last_name||', '||u.initials
   END
   ELSE vw.text
  END AS value
 FROM $schema.vw_redlistevaluators vw
 LEFT JOIN public."user" u ON u.id = vw.value
 ORDER BY u.last_name, u.first_name;
GRANT SELECT ON $schema.vw_redlistevaluators_publication TO $user;

DROP VIEW IF EXISTS $schema.vw_redlistcontributors_publication CASCADE;
CREATE VIEW $schema.vw_redlistcontributors_publication AS
 SELECT vw.taxonid, vw.assessmentid, 
  CASE
   WHEN vw.text IS NULL THEN
   CASE
    WHEN u.initials = '' AND u.first_name = '' THEN u.last_name
    WHEN u.initials = '' THEN u.last_name||', '||substring(u.first_name from 1 for 1)||'.'
    ELSE u.last_name||', '||u.initials
   END
   ELSE vw.text
  END AS value
 FROM $schema.vw_redlistcontributors vw
 LEFT JOIN public."user" u ON u.id = vw.value
 ORDER BY u.last_name, u.first_name;
GRANT SELECT ON $schema.vw_redlistcontributors_publication TO $user;

DROP VIEW IF EXISTS $schema.vw_redlistfacilitators_publication CASCADE;
CREATE VIEW $schema.vw_redlistfacilitators_publication AS
 SELECT vw.taxonid, vw.assessmentid, 
  CASE
   WHEN vw.text IS NULL THEN
   CASE
    WHEN u.initials = '' AND u.first_name = '' THEN u.last_name
    WHEN u.initials = '' THEN u.last_name||', '||substring(u.first_name from 1 for 1)||'.'
    ELSE u.last_name||', '||u.initials
   END
   ELSE vw.text
  END AS value
 FROM $schema.vw_redlistfacilitators vw
 LEFT JOIN public."user" u ON u.id = vw.value
 ORDER BY u.last_name, u.first_name;
GRANT SELECT ON $schema.vw_redlistfacilitators_publication TO $user;
