CREATE OR REPLACE VIEW $schema.vw_taxonomicnotes_attachments AS 
 SELECT f.taxonid, f.assessmentid, a.name, a.key AS url
   FROM vw_published.vw_filter f
   JOIN public.field i ON i.assessmentid = f.assessmentid AND i.name::text = 'TaxonomicNotes'::text
   JOIN public.fieldattachment_field ai ON ai.fieldid = i.id
   JOIN public.fieldattachment a ON a.id = ai.fieldattachmentid
  WHERE a.publish = true;

GRANT SELECT ON $schema.vw_taxonomicnotes_attachments TO $user;

CREATE OR REPLACE VIEW $schema.vw_habitatdocumentation_attachments AS 
 SELECT f.taxonid, f.assessmentid, a.name, a.key AS url
   FROM vw_published.vw_filter f
   JOIN public.field i ON i.assessmentid = f.assessmentid AND i.name::text = 'HabitatDocumentation'::text
   JOIN public.fieldattachment_field ai ON ai.fieldid = i.id
   JOIN public.fieldattachment a ON a.id = ai.fieldattachmentid
  WHERE a.publish = true;

GRANT SELECT ON $schema.vw_habitatdocumentation_attachments TO $user;

CREATE OR REPLACE VIEW $schema.vw_populationdocumentation_attachments AS 
 SELECT f.taxonid, f.assessmentid, a.name, a.key AS url
   FROM vw_published.vw_filter f
   JOIN public.field i ON i.assessmentid = f.assessmentid AND i.name::text = 'PopulationDocumentation'::text
   JOIN public.fieldattachment_field ai ON ai.fieldid = i.id
   JOIN public.fieldattachment a ON a.id = ai.fieldattachmentid
  WHERE a.publish = true;

GRANT SELECT ON $schema.vw_populationdocumentation_attachments TO $user;

CREATE OR REPLACE VIEW $schema.vw_rangedocumentation_attachments AS 
 SELECT f.taxonid, f.assessmentid, a.name, a.key AS url
   FROM vw_published.vw_filter f
   JOIN public.field i ON i.assessmentid = f.assessmentid AND i.name::text = 'RangeDocumentation'::text
   JOIN public.fieldattachment_field ai ON ai.fieldid = i.id
   JOIN public.fieldattachment a ON a.id = ai.fieldattachmentid
  WHERE a.publish = true;

GRANT SELECT ON $schema.vw_rangedocumentation_attachments TO $user;

CREATE OR REPLACE VIEW $schema.vw_threatsdocumentation_attachments AS 
 SELECT f.taxonid, f.assessmentid, a.name, a.key AS url
   FROM vw_published.vw_filter f
   JOIN public.field i ON i.assessmentid = f.assessmentid AND i.name::text = 'ThreatsDocumentation'::text
   JOIN public.fieldattachment_field ai ON ai.fieldid = i.id
   JOIN public.fieldattachment a ON a.id = ai.fieldattachmentid
  WHERE a.publish = true;

GRANT SELECT ON $schema.vw_threatsdocumentation_attachments TO $user;


CREATE OR REPLACE VIEW $schema.vw_conservationactionsdocumentation_attachments AS 
 SELECT f.taxonid, f.assessmentid, a.name, a.key AS url
   FROM vw_published.vw_filter f
   JOIN public.field i ON i.assessmentid = f.assessmentid AND i.name::text = 'ConservationActionsDocumentation'::text
   JOIN public.fieldattachment_field ai ON ai.fieldid = i.id
   JOIN public.fieldattachment a ON a.id = ai.fieldattachmentid
  WHERE a.publish = true;

GRANT SELECT ON $schema.vw_conservationactionsdocumentation_attachments TO $user;

CREATE OR REPLACE VIEW $schema.vw_usetradedocumentation_attachments AS 
 SELECT f.taxonid, f.assessmentid, a.name, a.key AS url
   FROM vw_published.vw_filter f
   JOIN public.field i ON i.assessmentid = f.assessmentid AND i.name::text = 'UseTradeDocumentation'::text
   JOIN public.fieldattachment_field ai ON ai.fieldid = i.id
   JOIN public.fieldattachment a ON a.id = ai.fieldattachmentid
  WHERE a.publish = true;

GRANT SELECT ON $schema.vw_usetradedocumentation_attachments TO $user;

CREATE OR REPLACE VIEW $schema.vw_redlistrationale_attachments AS 
 SELECT f.taxonid, f.assessmentid, a.name, a.key AS url
   FROM vw_published.vw_filter f
   JOIN public.field i ON i.assessmentid = f.assessmentid AND i.name::text = 'RedListRationale'::text
   JOIN public.fieldattachment_field ai ON ai.fieldid = i.id
   JOIN public.fieldattachment a ON a.id = ai.fieldattachmentid
  WHERE a.publish = true;

GRANT SELECT ON $schema.vw_redlistrationale_attachments TO $user;