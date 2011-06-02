create index assessment_taxonid on assessment (taxonid);
create index assessment_typeid on assessment (assessment_typeid);

create index assessment_edit_assessmentid on assessment_edit (assessmentid);
create index assessment_edit_editid on assessment_edit (editid);

create index assessment_reference_assessmentid on assessment_reference (assessmentid);
create index assessment_reference_referenceid on assessment_reference (referenceid);

create index common_name_taxonid on common_name (taxonid);
create index common_name_iso_languageid on common_name (iso_languageid);

create index common_name_notes_common_nameid on common_name_notes (common_nameid);
create index common_name_notes_notesid on common_name_notes (notesid);

create index common_name_reference_common_nameid on common_name_reference (common_nameid);
create index common_name_reference_referenceid on common_name_reference (referenceid);

create index edit_userid on edit (userid);

create index field_assessmentid on field (assessmentid);
create index field_parentid on field (parentid);

create index field_reference_fieldid on field_reference (fieldid);
create index field_reference_referenceid on field_reference (referenceid);

create index fk_list_primitive_values_fk_list_primitive_id on fk_list_primitive_values (fk_list_primitive_id);

create index notes_edit_editid on notes_edit (editid);
create index notes_edit_notesid on notes_edit (notesid);

create index notes_field_fieldid on notes_field (fieldid);
create index notes_field_notesid on notes_field (notesid);

create index permission_resource_attribute_permission_id on permission_resource_attribute (permission_id);

create index primitive_field_fieldid on primitive_field (fieldid);

create index reference_taxon_referenceid on reference_taxon (referenceid);
create index reference_taxon_taxonid on reference_taxon (taxonid);

create index synonym_taxonid on "synonym" (taxonid);
create index synonym_taxon_levelid on "synonym" (taxon_levelid);

create index synonym_notes_synonymid on synonym_notes (synonymid);
create index synonym_notes_notesid on synonym_notes (notesid);

create index synonym_reference_referenceid on synonym_reference (referenceid);
create index synonym_reference_synonymid on synonym_reference (synonymid);

create index taxon_taxon_statusid on taxon (taxon_statusid);
create index taxon_taxon_levelid on taxon (taxon_levelid);
create index taxon_taxon_infratype_id on taxon (taxon_infratype_id);
create index taxon_parentid on taxon (parentid);
create index taxon_internalid on taxon (internalid);

create index taxon_edit_taxonid on taxon_edit (taxonid);
create index taxon_edit_editid on taxon_edit (editid);

create index taxon_notes_notesid on taxon_notes (notesid);
create index taxon_notes_taxonid on taxon_notes (taxonid);

create index user_username on "user" (username);
create index user_email on "user" (email);

create index user_permission_user_id on user_permission (user_id);
create index user_permission_permission_group_id on user_permission (permission_group_id);

create index virus_userid on virus (userid);

create index workflow_notes_workflowstatusid on workflow_notes (workflowstatusid);
create index workflow_notes_userid on workflow_notes (userid);

create index workflow_status_workingsetid on workflow_status (workingsetid);

create index working_set_relationshipid on working_set (relationshipid);
create index working_set_creator on working_set (creator);

create index working_set_assessment_type_working_setid on working_set_assessment_type (working_setid);
create index working_set_assessment_type_assessment_typeid on working_set_assessment_type (assessment_typeid);

create index working_set_edit_editid on working_set_edit (editid);
create index working_set_edit_working_setid on working_set_edit (working_setid);

create index working_set_region_regionid on working_set_region (regionid);
create index working_set_region_working_setid on working_set_region (working_setid);

create index working_set_subscribe_user_working_setid on working_set_subscribe_user (working_setid);
create index working_set_subscribe_user_userid on working_set_subscribe_user (userid);

create index working_set_taxon_taxonid on working_set_taxon (taxonid);
create index working_set_taxon_working_setid on working_set_taxon (working_setid);

create index field_name on field (name);
create index primitive_field_name on primitive_field (name);