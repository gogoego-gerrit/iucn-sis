CREATE TEMPORARY TABLE _published_1 AS
  SELECT DISTINCT t2.taxonid, t2.assessmentid
  FROM (
    SELECT DISTINCT taxonid, assessmentid, value 
    FROM public.vw_redlistassessmentdate_value
  ) t2
  JOIN public.vw_regioninformation_regions ON public.vw_regioninformation_regions.assessmentid = t2.assessmentid
  JOIN public.assessment ON public.assessment.id = t2.assessmentid
  JOIN public.taxon ON public.taxon.id = public.assessment.id
  LEFT JOIN public.vw_redlisthidden_value ON public.vw_redlisthidden_value.assessmentid = t2.assessmentid
  WHERE 
  public.vw_redlisthidden_value.value is null AND
  public.assessment.assessment_typeid=1 AND
  public.taxon.taxon_statusid in(1,4) AND
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

DELETE FROM $schema.vw_filter;
INSERT INTO $schema.vw_filter
SELECT 
  _published_1.taxonid, 
  _published_1.assessmentid
FROM 
  _published_1, 
  public.vw_redlistcriteria,
  (SELECT DISTINCT taxonid, assessmentid, value FROM public.vw_redlistassessmentdate_value) as t2
WHERE 
  _published_1.assessmentid = public.vw_redlistcriteria.assessmentid AND
  _published_1.assessmentid=t2.assessmentid AND
  t2.value>='1996-01-01' AND
  
Case 	When IsManual=True then manualCategory <>'NE'
	else autoCategory <>'NE'	
End;