DELETE FROM $schema.vw_filter;
INSERT INTO $schema.vw_filter
 SELECT DISTINCT t2.taxonid, t2.assessmentid
  FROM
  public.vw_redlistassessmentdate_value t2,
  public.vw_regioninformation_regions,
  public.assessment,
  public.taxon, 
  (
    SELECT taxonid, assessmentid
    FROM (
      SELECT vw_filter.taxonid, vw_filter.assessmentid, s1.autocategory, s1.ismanual, s1.manualcategory
      FROM vw_filter
       LEFT JOIN ( 
       SELECT vw_filter.assessmentid, ff1.value AS autocategory, ff7.value AS ismanual, ff8.value AS manualcategory
       FROM vw_filter
       JOIN field ON field.assessmentid = vw_filter.assessmentid AND field.name::text = 'RedListCriteria'::text
       LEFT JOIN primitive_field pf1 ON pf1.fieldid = field.id AND pf1.name::text = 'autoCategory'::text
       LEFT JOIN string_primitive_field ff1 ON ff1.id = pf1.id
       LEFT JOIN primitive_field pf7 ON pf7.fieldid = field.id AND pf7.name::text = 'isManual'::text
       LEFT JOIN boolean_primitive_field ff7 ON ff7.id = pf7.id
       LEFT JOIN primitive_field pf8 ON pf8.fieldid = field.id AND pf8.name::text = 'manualCategory'::text
       LEFT JOIN string_primitive_field ff8 ON ff8.id = pf8.id
       LEFT JOIN primitive_field pf9 ON pf9.fieldid = field.id AND pf9.name::text = 'manualCriteria'::text
       LEFT JOIN string_primitive_field ff9 ON ff9.id = pf9.id
      ) s1 ON vw_filter.assessmentid = s1.assessmentid
    ) tbl
    WHERE
    CASE 
     WHEN ismanual = true THEN manualcategory <> 'NE'
     ELSE autocategory <> 'NE'
    END 
  ) redlistcriteria
  WHERE 
  t2.assessmentid = redlistcriteria.assessmentid AND
  t2.assessmentid = public.assessment.id AND
  public.assessment.taxonid = public.taxon.id AND
  public.assessment.id = public.vw_regioninformation_regions.assessmentid AND
  public.assessment.assessment_typeid=1 AND
  public.assessment.state = 0 AND
  public.taxon.taxon_statusid in(1,4) AND
  t2.value >= '1996-01-01' AND
  t2.assessmentid NOT IN (
    SELECT assessmentid FROM public.vw_redlisthidden_value
  ) AND
  (t2.taxonid,t2.value,public.vw_regioninformation_regions.value) IN (
    SELECT t1.taxonid, MAX(t1.value), public.vw_regioninformation_regions.value
    FROM public.vw_redlistassessmentdate_value as t1,
    public.vw_regioninformation_regions,
    public.assessment,
    public.taxon
    WHERE public.assessment.taxonid=taxon.id AND
    t1.assessmentid=assessment.id AND
    public.assessment.id=public.vw_regioninformation_regions.assessmentid AND
    public.assessment.assessment_typeid=1 AND
    public.taxon.taxon_statusid in(1,4)
    GROUP BY t1.taxonid, public.vw_regioninformation_regions.value
  );
