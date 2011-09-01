DELETE FROM $schema.vw_filter;
INSERT INTO $schema.vw_filter
 SELECT DISTINCT taxonid, id
 FROM assessment
 WHERE 
  assessment.state = 0 AND
  assessment.assessment_typeid = 2;
 