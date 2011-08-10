UPDATE fk_list_primitive_values 
SET "value" = 8 
WHERE ("value" = 11 OR "value" = 14) AND fk_list_primitive_id IN (
  SELECT fk.id
  FROM field
  JOIN primitive_field ON field.id = primitive_field.fieldid
  JOIN foreign_key_list_primitive_field fk ON primitive_field.id = fk.id
  WHERE field.name = 'RegionInformation'
    AND primitive_field.name = 'regions'
);

UPDATE fk_list_primitive_values 
SET "value" = 10 
WHERE "value" = 20 AND fk_list_primitive_id IN (
  SELECT fk.id
  FROM field
  JOIN primitive_field ON field.id = primitive_field.fieldid
  JOIN foreign_key_list_primitive_field fk ON primitive_field.id = fk.id
  WHERE field.name = 'RegionInformation'
    AND primitive_field.name = 'regions'
);

UPDATE fk_list_primitive_values 
SET "value" = 9
WHERE ("value" = 19) AND fk_list_primitive_id IN (
  SELECT fk.id
  FROM field
  JOIN primitive_field ON field.id = primitive_field.fieldid
  JOIN foreign_key_list_primitive_field fk ON primitive_field.id = fk.id
  WHERE field.name = 'RegionInformation'
    AND primitive_field.name = 'regions'
);

UPDATE working_set_region
SET "regionid" = 8
WHERE "regionid" = 11 OR "regionid" = 14;

UPDATE working_set_region
SET "regionid" = 10
WHERE "regionid" = 20;

UPDATE working_set_region
SET "regionid" = 9
WHERE "regionid" = 19;

DELETE FROM region
WHERE "id" = 11 OR "id" = 14;

DELETE FROM region
WHERE "id" = 20;

DELETE FROM region
WHERE "id" = 19;

DELETE FROM region 
WHERE "id" = 6;