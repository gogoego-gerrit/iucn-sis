DROP TABLE IF EXISTS vw_published.recent_by_region;
CREATE TABLE vw_published.recent_by_region AS
  SELECT taxonid, fi2.value as region, MAX(df1.value) as date
  FROM assessment a
  JOIN taxon ON a.taxonid = taxon.id
  LEFT JOIN field f1 ON f1.assessmentid = a.id AND f1.name = 'RedListAssessmentDate'
  LEFT JOIN primitive_field pf1 ON f1.id = pf1.fieldid AND pf1.name = 'value'
  LEFT JOIN date_primitive_field df1 ON pf1.id = df1.id AND df1.value >= '1996-01-01'
  LEFT JOIN field f2 ON f2.assessmentid = a.id AND f2.name = 'RegionInformation'
  LEFT JOIN primitive_field pf2 ON f2.id = pf2.fieldid AND pf2.name = 'regions'
  LEFT JOIN foreign_key_list_primitive_field fk2 ON pf2.id = fk2.id
  LEFT JOIN fk_list_primitive_values fi2 ON fk2.id = fi2.fk_list_primitive_id
  WHERE taxon.taxon_statusid NOT IN (2, 3) AND
    a.state = 0 AND
    a.assessment_typeid = 1
  GROUP BY taxonid, fi2.value;

DROP TABLE IF EXISTS vw_published.recent_regional;
CREATE TABLE vw_published.recent_regional AS
  SELECT a.id AS assessmentid, a.taxonid
  FROM assessment a
  JOIN field f1 ON f1.assessmentid = a.id AND f1.name = 'RedListAssessmentDate'
  JOIN primitive_field pf1 ON f1.id = pf1.fieldid AND pf1.name = 'value'
  JOIN date_primitive_field df1 ON pf1.id = df1.id
  JOIN field f2 ON f2.assessmentid = a.id AND f2.name = 'RegionInformation'
  JOIN primitive_field pf2 ON f2.id = pf2.fieldid AND pf2.name = 'regions'
  JOIN foreign_key_list_primitive_field fk2 ON pf2.id = fk2.id
  JOIN fk_list_primitive_values fi2 ON fk2.id = fi2.fk_list_primitive_id
  JOIN vw_published.recent_by_region rbr ON a.taxonid = rbr.taxonid AND
    fi2.value = rbr.region AND df1.value = rbr.date
  WHERE a.state = 0 AND a.assessment_typeid = 1;
  
TRUNCATE vw_published.vw_filter;
INSERT INTO vw_published.vw_filter (assessmentid, taxonid)
  SELECT DISTINCT a.assessmentid, a.taxonid
  FROM vw_published.recent_regional a
  JOIN field f ON f.assessmentid = a.assessmentid AND f.name = 'RedListCriteria'
  LEFT JOIN primitive_field pf1 ON pf1.fieldid = f.id AND pf1.name = 'isManual'
  LEFT JOIN boolean_primitive_field bf1 ON pf1.id = bf1.id
  LEFT JOIN primitive_field pf2 ON pf2.fieldid = f.id AND pf2.name = 'autoCategory'
  LEFT JOIN string_primitive_field sf2 ON sf2.id = pf2.id
  LEFT JOIN primitive_field pf3 ON pf3.fieldid = f.id AND pf3.name = 'manualCategory'
  LEFT JOIN string_primitive_field sf3 ON sf3.id = pf3.id
  LEFT JOIN primitive_field pf5 ON pf5.fieldid = f.id AND pf5.name = 'critVersion'
  LEFT JOIN foreign_key_primitive_field fk5 ON fk5.id = pf5.id
  LEFT JOIN field f2 ON f2.assessmentid = a.assessmentid AND f2.name = 'RedListHidden'
  LEFT JOIN primitive_field pf4 ON pf4.fieldid = f2.id AND pf4.name = 'value'
  LEFT JOIN boolean_primitive_field bf4 ON bf4.id = pf4.id
  WHERE bf4.value is null AND fk5.value < 3 AND
    CASE
      WHEN bf1.value = true THEN sf3 is not null AND sf3.value <> 'NE'
      ELSE sf2 is not null AND sf2.value <> 'NE'
    END;