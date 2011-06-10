DROP VIEW IF EXISTS vw_kingdom CASCADE;
CREATE VIEW vw_kingdom AS
  SELECT taxon.id as taxonid, name as kingdom
  FROM taxon where taxon_levelid=1;
GRANT SELECT ON vw_kingdom TO iucn;

DROP VIEW IF EXISTS vw_phylum CASCADE;
CREATE VIEW vw_phylum AS
  SELECT taxon.id as taxonid, kingdom, name as phylum
  FROM vw_kingdom
  JOIN taxon on parentid = taxonid;
GRANT SELECT ON vw_phylum TO iucn;

DROP VIEW IF EXISTS vw_class CASCADE;
CREATE VIEW vw_class AS
  SELECT taxon.id as taxonid, kingdom, phylum, name as class
  FROM vw_phylum
  JOIN taxon on parentid = taxonid;
GRANT SELECT ON vw_class TO iucn;

DROP VIEW IF EXISTS vw_order CASCADE;
CREATE VIEW vw_order AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, name as order
  FROM vw_class
  JOIN taxon on parentid = taxonid;
GRANT SELECT ON vw_order TO iucn;

DROP VIEW IF EXISTS vw_family CASCADE;
CREATE VIEW vw_family AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", name as family
  FROM vw_order
  JOIN taxon on parentid = taxonid;
GRANT SELECT ON vw_family TO iucn;

DROP VIEW IF EXISTS vw_genus CASCADE;
CREATE VIEW vw_genus AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, name as genus
  FROM vw_family
  JOIN taxon on parentid = taxonid;
GRANT SELECT ON vw_genus TO iucn;

DROP VIEW IF EXISTS vw_species CASCADE;
CREATE VIEW vw_species AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, genus, name as species
  FROM vw_genus
  JOIN taxon on parentid = taxonid;
GRANT SELECT ON vw_species TO iucn;

DROP VIEW IF EXISTS vw_infrarank CASCADE;
CREATE VIEW vw_infrarank AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, genus, species, taxon.name as infrarank, public.infratype.name as infratype
  FROM vw_species
  JOIN taxon on parentid = taxonid and taxon_levelid=8
  JOIN public.infratype on taxon.taxon_infratype_id = public.infratype.id;
GRANT SELECT ON vw_infrarank TO iucn;

DROP VIEW IF EXISTS vw_subpopulation_species CASCADE;
CREATE VIEW vw_subpopulation_species AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, genus, species, NULL as infrarank, NULL as infratype, name as subpopulation
  FROM vw_species
  JOIN taxon on parentid = taxonid and (taxon_levelid=9 or taxon_levelid=10);
GRANT SELECT ON vw_subpopulation_species TO iucn;

DROP VIEW IF EXISTS vw_subpopulation_infrarank CASCADE;
CREATE VIEW vw_subpopulation_infrarank AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, genus, species, infrarank, infratype, name as subpopulation
  FROM vw_infrarank
  JOIN taxon on parentid = taxonid and (taxon_levelid=9 or taxon_levelid=10);
GRANT SELECT ON vw_subpopulation_infrarank TO iucn;

DROP VIEW IF EXISTS vw_footprint CASCADE;
CREATE VIEW vw_footprint AS
  SELECT vw_subpopulation_species.*, taxon.taxonomic_authority from vw_subpopulation_species
  JOIN taxon ON taxon.id = vw_subpopulation_species.taxonid
  UNION ALL
  SELECT vw_subpopulation_infrarank.*, taxon.taxonomic_authority from vw_subpopulation_infrarank
  JOIN taxon ON taxon.id = vw_subpopulation_infrarank.taxonid
  UNION ALL
  SELECT vw_infrarank.*, NULL, taxon.taxonomic_authority from vw_infrarank
  JOIN taxon ON taxon.id = vw_infrarank.taxonid
  UNION ALL
  SELECT vw_species.*, NULL, NULL, NULL, taxon.taxonomic_authority from vw_species
  JOIN taxon ON taxon.id = vw_species.taxonid;
GRANT SELECT ON vw_footprint TO iucn;