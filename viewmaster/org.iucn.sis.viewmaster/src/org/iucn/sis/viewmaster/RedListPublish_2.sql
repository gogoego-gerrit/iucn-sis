DELETE FROM $schema.vw_filter;
INSERT INTO $schema.vw_filter
 SELECT DISTINCT t2.taxonid, t2.assessmentid
  FROM
  public.vw_redlistassessmentdate_value t2,
  public.vw_regioninformation_regions,
  public.vw_redlistcriteria,
  public.assessment,
  public.taxon
  WHERE 
  t2.assessmentid = public.vw_redlistcriteria.assessmentid AND
  t2.assessmentid = public.assessment.id AND
  public.assessment.taxonid = public.taxon.id AND
  public.assessment.id = public.vw_regioninformation_regions.assessmentid AND
  public.assessment.assessment_typeid=1 AND
  public.assessment.state = 0 AND
  public.taxon.taxon_statusid in(1,4) AND
  t2.value >= '1996-01-01' AND
  Case 	When IsManual=True then manualCategory <>'NE'
	else autoCategory <>'NE'	
  End AND
  t2.assessmentid NOT IN (
    SELECT assessmentid FROM vw_redlisthidden_value
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
