TRUNCATE $schema.vw_filter;
INSERT INTO $schema.vw_filter
 SELECT DISTINCT assessment.taxonid, assessment.id
 FROM assessment
 JOIN taxon ON taxon.id = assessment.taxonid
 WHERE 
  taxon.state = 0 AND assessment.state = 0;
 