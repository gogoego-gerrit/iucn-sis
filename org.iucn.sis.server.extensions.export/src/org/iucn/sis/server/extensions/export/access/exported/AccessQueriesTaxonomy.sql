CREATE TABLE $schema.vw_kingdom AS
  SELECT taxon.id as taxonid, name as kingdom
  FROM taxon 
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid 
  where taxon_levelid=1;

CREATE TABLE $schema.vw_phylum AS
  SELECT taxon.id as taxonid, kingdom, name as phylum
  FROM $schema.vw_kingdom t
  JOIN taxon on parentid = t.taxonid
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid;

CREATE TABLE $schema.vw_class AS
  SELECT taxon.id as taxonid, kingdom, phylum, name as class
  FROM $schema.vw_phylum t
  JOIN taxon on parentid = t.taxonid
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid;

CREATE TABLE $schema.vw_order AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, name as order
  FROM $schema.vw_class t
  JOIN taxon on parentid = t.taxonid
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid;

CREATE TABLE $schema.vw_family AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", name as family
  FROM $schema.vw_order t 
  JOIN taxon on parentid = t.taxonid
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid;

CREATE TABLE $schema.vw_genus AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, name as genus
  FROM $schema.vw_family t 
  JOIN taxon on parentid = t.taxonid
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid;

CREATE TABLE $schema.vw_species AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, genus, name as species
  FROM $schema.vw_genus t
  JOIN taxon on parentid = t.taxonid
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid;

CREATE TABLE $schema.vw_infrarank AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, genus, species, taxon.name as infrarank, public.infratype.name as infratype
  FROM $schema.vw_species t
  JOIN taxon on parentid = t.taxonid and taxon_levelid=8
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid
  JOIN public.infratype on taxon.taxon_infratype_id = public.infratype.id;

CREATE TABLE $schema.vw_subpopulation_species AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, genus, species, NULL::character varying as infrarank, NULL::character varying as infratype, name as subpopulation
  FROM $schema.vw_species t
  JOIN taxon on parentid = t.taxonid and (taxon_levelid=9 or taxon_levelid=10)
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid;

CREATE TABLE $schema.vw_subpopulation_infrarank AS
  SELECT taxon.id as taxonid, kingdom, phylum, class, "order", family, genus, species, infrarank, infratype, name as subpopulation
  FROM $schema.vw_infrarank t
  JOIN taxon on parentid = t.taxonid and (taxon_levelid=9 or taxon_levelid=10)
  JOIN $schema.vw_filter_taxa f ON taxon.id = f.taxonid;

CREATE TABLE $schema.vw_footprint AS
  SELECT $schema.vw_subpopulation_species.*, taxon.taxonomic_authority from $schema.vw_subpopulation_species
  JOIN taxon ON taxon.id = $schema.vw_subpopulation_species.taxonid
  UNION ALL
  SELECT $schema.vw_subpopulation_infrarank.*, taxon.taxonomic_authority from $schema.vw_subpopulation_infrarank
  JOIN taxon ON taxon.id = $schema.vw_subpopulation_infrarank.taxonid
  UNION ALL
  SELECT $schema.vw_infrarank.*, NULL, taxon.taxonomic_authority from $schema.vw_infrarank
  JOIN taxon ON taxon.id = $schema.vw_infrarank.taxonid
  UNION ALL
  SELECT $schema.vw_species.*, NULL, NULL, NULL, taxon.taxonomic_authority from $schema.vw_species
  JOIN taxon ON taxon.id = $schema.vw_species.taxonid;
GRANT SELECT ON vw_footprint TO iucn;