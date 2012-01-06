DROP TABLE IF EXISTS aves;

CREATE TABLE aves AS
  SELECT s.id, s.name
  FROM taxon s
  JOIN taxon g ON g.id = s.parentid
  JOIN taxon f ON f.id = g.parentid
  JOIN taxon o ON o.id = f.parentid
  JOIN taxon c ON c.id = o.parentid
  WHERE c.name = 'AVES';

INSERT INTO aves (id, name)
  SELECT g.id, g.name
  FROM taxon g
  JOIN taxon f ON f.id = g.parentid
  JOIN taxon o ON o.id = f.parentid
  JOIN taxon c ON c.id = o.parentid
  WHERE c.name = 'AVES';

INSERT INTO aves (id, name)
  SELECT f.id, f.name
  FROM taxon f
  JOIN taxon o ON o.id = f.parentid
  JOIN taxon c ON c.id = o.parentid
  WHERE c.name = 'AVES';

INSERT INTO aves (id, name)
  SELECT o.id, o.name
  FROM taxon o
  JOIN taxon c ON c.id = o.parentid
  WHERE c.name = 'AVES';

INSERT INTO aves (id, name)
  SELECT c.id, c.name
  FROM taxon c
  WHERE c.name = 'AVES';