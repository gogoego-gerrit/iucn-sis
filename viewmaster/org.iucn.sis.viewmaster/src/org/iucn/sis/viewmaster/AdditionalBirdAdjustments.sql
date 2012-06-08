DROP VIEW IF EXISTS $schema.tmp_threatssubfield_threatslookup CASCADE;

CREATE VIEW $schema.tmp_threatssubfield_threatslookup AS 
SELECT taxonid, assessmentid, recordid, l."ID" as value
FROM (
  SELECT v.taxonid, v.assessmentid, c.id as recordid,
    CASE
	WHEN sf.value = '' or sf.value is null THEN l."REF"
	WHEN sf.value like 'Unspecified%' THEN
	CASE
	    WHEN sf.value like '%(%)%' THEN SUBSTRING(l."REF", 0, 5)||'2'
	    ELSE l."REF"
	END
	WHEN sf.value ilike '%virus%' OR sf.value ilike '%disease%' OR sf.value like 'Avian%' THEN '8.5.2'
	WHEN sf.value like '%(%)%' THEN SUBSTRING(l."REF", 0, 5)||'2'
	ELSE 
	    CASE
		WHEN l."REF" = '8.2.1' THEN l."REF"
		ELSE '8.5.2'
	    END
    END AS value
  FROM $schema.vw_filter v 
  join field f on f.name = 'Threats' and v.assessmentid = f.assessmentid 
  join field c on c.name = 'ThreatsSubfield' and f.id = c.parentid 
  join primitive_field pf on c.id = pf.fieldid and pf.name = 'ThreatsLookup' 
  join foreign_key_primitive_field fk on pf.id = fk.id 
  join lookups."THREATSLOOKUP" l on l."ID" = fk.value 
  left join primitive_field pf2 on pf2.fieldid = c.id and pf2.name = 'text'
  left join string_primitive_field sf on pf2.id = sf.id
  join taxon t on t.id = v.taxonid 
  where (l."REF" like '8.1.%' or l."REF" like '8.2.%')
) x
JOIN lookups."THREATSLOOKUP" l ON l."REF" = x.value;

DROP VIEW IF EXISTS $schema.vw_threatssubfield_threatslookup CASCADE;

CREATE OR REPLACE VIEW $schema.vw_threatssubfield_threatslookup AS 
 SELECT vw_filter.taxonid, vw_filter.assessmentid, sf.id AS recordid, 
   CASE
       WHEN b.value is null THEN ff.value
       ELSE b.value
   END
   FROM $schema.vw_filter
   JOIN field ON field.assessmentid = vw_filter.assessmentid AND field.name::text = 'Threats'::text
   JOIN field sf ON field.id = sf.parentid
   JOIN primitive_field pf ON pf.fieldid = sf.id
   JOIN foreign_key_primitive_field ff ON ff.id = pf.id
   LEFT JOIN $schema.tmp_threatssubfield_threatslookup b ON sf.id = b.recordid
  WHERE sf.name::text = 'ThreatsSubfield'::text AND pf.name::text = 'ThreatsLookup'::text;
   
GRANT SELECT ON $schema.vw_threatssubfield_threatslookup TO $user;

---
-- Birdlife formats species names like so: {Common Name} ({Sci. Name})
-- So, I am removing the common name and parentheses from the text, 
-- so that everything appears as just the scientific name, no matter 
-- where it comes from (text or lookup).
---

DROP VIEW IF EXISTS $schema.vw_threatssubfield_ias_publication CASCADE;
CREATE VIEW $schema.vw_threatssubfield_ias_publication AS 
  SELECT l.taxonid, l.assessmentid, l.recordid,
	CASE
	    WHEN m.value is not null THEN
	    CASE
		WHEN m.value like '%(%)%' THEN TRIM(leading '(' FROM SUBSTRING(m.value from POSITION('(' in m.value) for POSITION(')' in m.value) - POSITION('(' in m.value)))
		ELSE m.value
	    END
	    ELSE t."LABEL"
	END AS value
  FROM $schema.vw_threatssubfield_threatslookup l
  JOIN lookups."THREATSLOOKUP" code ON code."ID" = l.value AND l.value IN (82, 85)
  LEFT JOIN $schema.vw_threatssubfield_ias i ON l.recordid = i.recordid
  LEFT JOIN lookups."THREATS_IASLOOKUP" t ON i.value = t."ID"
  LEFT JOIN $schema.vw_threatssubfield_text m ON l.recordid = m.recordid;

GRANT SELECT ON $schema.vw_threatssubfield_ias_publication TO $user;