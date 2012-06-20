
-- Table 3a
DROP VIEW IF EXISTS $schema.table_3a_base CASCADE;
CREATE VIEW $schema.table_3a_base AS
  SELECT f.class, cc.rlcategory as category, count(cc.assessmentid) as count
  FROM $schema.vw_redlistcategoryandcriteria cc
  JOIN $schema.vw_species f ON cc.taxonid = f.taxonid AND f.kingdom = 'ANIMALIA'
  GROUP BY f.class, cc.rlcategory
  ORDER BY f.class, cc.rlcategory;
GRANT SELECT ON $schema.table_3a_base TO $user;

DROP VIEW IF EXISTS $schema.table_3a_total CASCADE;
CREATE VIEW $schema.table_3a_total AS
  SELECT class, SUM(count) as total 
  FROM $schema.table_3a_base
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3a_total TO $user;
  
DROP VIEW IF EXISTS $schema.table_3a_subtotal_extinct CASCADE;
CREATE VIEW $schema.table_3a_subtotal_extinct AS
  SELECT class, SUM(count) as total
  FROM $schema.table_3a_base
  WHERE category = 'EX' OR category = 'EW'
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3a_subtotal_extinct TO $user;

DROP VIEW IF EXISTS $schema.table_3a_subtotal_threatened CASCADE;
CREATE VIEW $schema.table_3a_subtotal_threatened AS
  SELECT class, SUM(count) as total
  FROM $schema.table_3a_base
  WHERE category = 'CR' OR category = 'EN' or category = 'VU'
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3a_subtotal_threatened TO $user;

-- Table 3b

DROP VIEW IF EXISTS $schema.table_3b_base CASCADE;
CREATE VIEW $schema.table_3b_base AS
  SELECT f.class, cc.rlcategory as category, count(cc.assessmentid) as count
  FROM $schema.vw_redlistcategoryandcriteria cc
  JOIN $schema.vw_species f ON cc.taxonid = f.taxonid AND f.kingdom = 'PLANTAE'
  GROUP BY f.class, cc.rlcategory
  ORDER BY f.class, cc.rlcategory;
GRANT SELECT ON $schema.table_3b_base TO $user;

DROP VIEW IF EXISTS $schema.table_3b_total CASCADE;
CREATE VIEW $schema.table_3b_total AS
  SELECT class, SUM(count) as total 
  FROM $schema.table_3b_base
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3b_total TO $user;
  
DROP VIEW IF EXISTS $schema_table_3b_subtotal_extinct CASCADE;
CREATE VIEW $schema.table_3b_subtotal_extinct AS
  SELECT class, SUM(count) as total
  FROM $schema.table_3b_base
  WHERE category = 'EX' OR category = 'EW'
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3b_subtotal_extinct TO $user;

DROP VIEW IF EXISTS $schema.table_3b_subtotal_threatened CASCADE;
CREATE VIEW $schema.table_3b_subtotal_threatened AS
  SELECT class, SUM(count) as total
  FROM $schema.table_3b_base
  WHERE category = 'CR' OR category = 'EN' or category = 'VU'
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3b_subtotal_threatened TO $user;

-- Table 3c

DROP VIEW IF EXISTS $schema.table_3c_base CASCADE;
CREATE VIEW $schema.table_3c_base AS
  SELECT f.class, cc.rlcategory as category, count(cc.assessmentid) as count
  FROM $schema.vw_redlistcategoryandcriteria cc
  JOIN $schema.vw_species f ON cc.taxonid = f.taxonid AND (f.kingdom <> 'ANIMALIA' AND f.kingdom <> 'PLANTAE')
  GROUP BY f.class, cc.rlcategory
  ORDER BY f.class, cc.rlcategory;
GRANT SELECT ON $schema.table_3c_base TO $user;

DROP VIEW IF EXISTS $schema.table_3c_total CASCADE;
CREATE VIEW $schema.table_3c_total AS
  SELECT class, SUM(count) as total 
  FROM $schema.table_3c_base
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3c_total TO $user;
  
DROP VIEW IF EXISTS $schema.table_3c_subtotal_extinct CASCADE;
CREATE VIEW $schema.table_3c_subtotal_extinct AS
  SELECT class, SUM(count) as total
  FROM $schema.table_3c_base
  WHERE category = 'EX' OR category = 'EW'
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3c_subtotal_extinct TO $user;

DROP VIEW IF EXISTS $schema.table_3c_subtotal_threatened CASCADE;
CREATE VIEW $schema.table_3c_subtotal_threatened AS
  SELECT class, SUM(count) as total
  FROM $schema.table_3c_base
  WHERE category = 'CR' OR category = 'EN' or category = 'VU'
  GROUP BY class
  ORDER BY class;
GRANT SELECT ON $schema.table_3c_subtotal_threatened TO $user;

-- table 4a

DROP VIEW IF EXISTS $schema.table_4a_base CASCADE;
CREATE VIEW $schema.table_4a_base AS
  SELECT f.class, f."order", cc.rlcategory as category, count(cc.assessmentid) as count
  FROM $schema.vw_redlistcategoryandcriteria cc 
  JOIN $schema.vw_species f ON f.taxonid = cc.taxonid AND f.kingdom = 'ANIMALIA'
  GROUP BY class, "order", rlcategory
  ORDER BY class, "order", rlcategory;
GRANT SELECT ON $schema.table_4a_base TO $user;

DROP VIEW IF EXISTS $schema.table_4a_total CASCADE;
CREATE VIEW $schema.table_4a_total AS
  SELECT class, SUM(count) as total 
  FROM $schema.table_4a_base
  GROUP BY class, "order"
  ORDER BY class, "order";
GRANT SELECT ON $schema.table_4a_total TO $user;
  
DROP VIEW IF EXISTS $schema.table_4a_subtotal_extinct CASCADE;
CREATE VIEW $schema.table_4a_subtotal_extinct AS
  SELECT class, "order", SUM(count) as total
  FROM $schema.table_4a_base
  WHERE category = 'EX' OR category = 'EW'
  GROUP BY class, "order"
  ORDER BY class, "order";
GRANT SELECT ON $schema.table_4a_subtotal_extinct TO $user;

DROP VIEW IF EXISTS $schema.table_4a_subtotal_threatened CASCADE;
CREATE VIEW $schema.table_4a_subtotal_threatened AS
  SELECT class, "order", SUM(count) as total
  FROM $schema.table_4a_base
  WHERE category = 'CR' OR category = 'EN' or category = 'VU'
  GROUP BY class, "order"
  ORDER BY class, "order";
GRANT SELECT ON $schema.table_4a_subtotal_threatened TO $user;

DROP VIEW IF EXISTS $schema.table_4a_class_total;
CREATE VIEW $schema.table_4a_class_total AS
  SELECT "class", category, SUM(count) as total
  FROM vw_published.table_4a_base
  GROUP BY "class", category
  ORDER BY "class", category;
GRANT SELECT ON $schema.table_4a_class_total TO $user;

-- table 4b

DROP VIEW IF EXISTS $schema.table_4b_base CASCADE;
CREATE VIEW $schema.table_4b_base AS
  SELECT f.class, f."order", f.family, cc.rlcategory as category, count(cc.assessmentid) as count
  FROM $schema.vw_redlistcategoryandcriteria cc 
  JOIN $schema.vw_species f ON f.taxonid = cc.taxonid AND f.kingdom = 'PLANTAE'
  GROUP BY class, "order", family, rlcategory
  ORDER BY class, "order", family, rlcategory;
GRANT SELECT ON $schema.table_4b_base TO $user;

DROP VIEW IF EXISTS $schema.table_4b_total CASCADE;
CREATE VIEW $schema.table_4b_total AS
  SELECT class, SUM(count) as total 
  FROM $schema.table_4b_base
  GROUP BY class, "order", family
  ORDER BY class, "order", family;
GRANT SELECT ON $schema.table_4b_total TO $user;
  
DROP VIEW IF EXISTS $schema.table_4b_subtotal_extinct CASCADE;
CREATE VIEW $schema.table_4b_subtotal_extinct AS
  SELECT class, "order", family, SUM(count) as total
  FROM $schema.table_4b_base
  WHERE category = 'EX' OR category = 'EW'
  GROUP BY class, "order", family
  ORDER BY class, "order", family;
GRANT SELECT ON $schema.table_4b_subtotal_extinct TO $user;

DROP VIEW IF EXISTS $schema.table_4b_subtotal_threatened CASCADE;
CREATE VIEW $schema.table_4b_subtotal_threatened AS
  SELECT class, "order", SUM(count) as total
  FROM $schema.table_4b_base
  WHERE category = 'CR' OR category = 'EN' or category = 'VU'
  GROUP BY class, "order", family
  ORDER BY class, "order", family;
GRANT SELECT ON $schema.table_4b_subtotal_threatened TO $user;

DROP VIEW IF EXISTS $schema.table_4b_class_total;
CREATE VIEW $schema.table_4b_class_total AS
  SELECT "class", category, SUM(count) as total
  FROM vw_published.table_4b_base
  GROUP BY "class", category
  ORDER BY "class", category;
GRANT SELECT ON $schema.table_4b_class_total TO $user;