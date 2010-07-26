CREATE FUNCTION getPrimitiveId(a int, f varchar(255), t varchar(255), n varchar(255)) RETURNS int as $$
DECLARE
  field_id int;
  primitive_id int;
BEGIN
  EXECUTE 'SELECT id FROM field WHERE assessmentid=$1 and name=$2'
    INTO field_id
    USING a, f;
  IF field_id IS NULL
  THEN
    RAISE NOTICE 'inserting new field for %', f;
    SELECT nextval('hibernate_sequence') INTO field_id;
    EXECUTE 'INSERT INTO field (id,assessmentid,name) VALUES ($1,$2,$3)'
      USING field_id, a, f;
  ELSE
    RAISE NOTICE 'existing field_id is %', field_id;
  END IF;
  EXECUTE 'SELECT id FROM primitive_field WHERE fieldid=$1 and name=$2'
    INTO primitive_id
    USING field_id, n;
  IF primitive_id IS NULL
  THEN
    RAISE NOTICE 'inserting new primitive for %', n;
    SELECT nextval('hibernate_sequence') INTO primitive_id;
    EXECUTE 'INSERT INTO primitive_field (id,name,fieldid) VALUES ($1,$2,$3)'
      USING primitive_id, n, field_id;
    RAISE NOTICE 'inserting new typed primitive for %', n;
    EXECUTE 'INSERT INTO ' || t || ' (id,value) VALUES ($1,0)'
      USING primitive_id;
  ELSE
    RAISE NOTICE 'existing primitive_id is %', primitive_id;
  END IF;
  RETURN primitive_id;
END
$$ LANGUAGE plpgsql;