package org.iucn.sis.shared.api.acl;

import org.iucn.sis.shared.api.models.User;

public class SISPermissionChecker {

	//FIXME
	public static boolean hasRight(User user, String right, Object object) {
		return false;
	
//		String quickGroup = user.getProperty("quickGroup");
//		if (quickGroup == null || quickGroup.equals("") || quickGroup.contains("'sysadmin'")
//				|| quickGroup.contains("'rlu'"))
//			return true;
//		else if (quickGroup.contains("'offline'")) {
//			if (right == Roles.TAXOMATIC)
//				return !quickGroup.contains("'no_taxomatic'");
//			else
//				return true;
//		}
//
//		if (object instanceof Assessment) {
//			Assessment assessment = (Assessment) object;
//
//			if (assessment.getType().equalsIgnoreCase(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
//				if (quickGroup.contains("'canEditPublished'")
//						&& hasRight(user, right, TaxonomyCache.impl.getNode(assessment.getSpeciesID())))
//					return true;
//				else
//					return false;
//			} else if (assessment.getType().equalsIgnoreCase(AssessmentType.USER_ASSESSMENT_TYPE))
//				return true;
//			else
//				return hasRight(user, right, TaxonomyCache.impl.getNode(assessment.getSpeciesID()));
//		} else if (object instanceof TaxonNode) {
//			TaxonNode taxon = (TaxonNode) object;
//
//			if (right == Roles.CREATE_PUBLISHED_ASSESSMENTS)
//				return quickGroup.contains("'canCreatePublished'") && isInTaxonomicScope(quickGroup, taxon);
//			else if (right == Roles.CREATE_DRAFT_ASSESSMENTS)
//				return quickGroup.contains("'canCreateDraft'") && isInTaxonomicScope(quickGroup, taxon);
//			else if (right == Roles.DELETE_PUBLISHED_ASSESSMENT)
//				return quickGroup.contains("'canDeletePublished'") && isInTaxonomicScope(quickGroup, taxon);
//			else if (right == Roles.DELETE_DRAFT_ASSESSMENT)
//				return quickGroup.contains("'canDeleteDraft'") && isInTaxonomicScope(quickGroup, taxon);
//			else
//				return isInTaxonomicScope(quickGroup, taxon);
//		} else if (right == Roles.BATCH_CHANGE || right == Roles.FIND_REPLACE) {
//			if (quickGroup.contains("'no_birds'") || quickGroup.contains("'can_batch'")
//					|| quickGroup.contains("'find_replace'"))
//				return true;
//			else
//				return false;
//		} else if (right == Roles.DEM_UPLOAD || right == Roles.EDIT_PUBLISHED_ASSESSMENTS
//				|| right == Roles.TAXON_FINDER) {
//			return false;
//		} else if (right == Roles.TAXOMATIC) {
//			return !quickGroup.contains("'no_taxomatic'");
//		} else if (right == Roles.MANAGE_REFERENCES) {
//			return !quickGroup.contains("'no_references'");
//		} else if (right == Roles.REFERENCE_REPLACE) {
//			return quickGroup.contains("'reference_replacer'");
//		} else
//			return true;
//	}
//
//	private static boolean isInTaxonomicScope(String quickGroup, TaxonNode taxon) {
//		String stringToMatch = "";
//		if (taxon.getFootprint().length < 3)
//			stringToMatch = taxon.getName();
//		else
//			stringToMatch = taxon.getFootprint()[2];
//
//		if (quickGroup.contains("'guest'"))
//			return false;
//		else if (quickGroup.contains("'gaa'") && stringToMatch.equalsIgnoreCase("AMPHIBIA"))
//			return true;
//		else if (quickGroup.contains("'gma'") && stringToMatch.equalsIgnoreCase("MAMMALIA"))
//			return true;
//		else if (quickGroup.contains("'reptiles'") && stringToMatch.equalsIgnoreCase("REPTILIA"))
//			return true;
//		else if (quickGroup.contains("'no_birds'") && !stringToMatch.equalsIgnoreCase("AVES"))
//			return true;
//		else if (quickGroup.contains("'molluscs'")
//				&& (taxon.getFootprint().length >= 2 && taxon.getFootprint()[1].equalsIgnoreCase("MOLLUSCA"))
//				|| taxon.getName().equalsIgnoreCase("MOLLUSCA"))
//			return true;
//		else if (quickGroup.contains("'bryophyta'")
//				&& (taxon.getFootprint().length >= 2 && taxon.getFootprint()[1].equalsIgnoreCase("BRYOPHYTA"))
//				|| taxon.getName().equalsIgnoreCase("BRYOPHYTA"))
//			return true;
//		else if (quickGroup.contains("'Lepidoptera'")
//				&& (taxon.getFootprint().length >= 4 && taxon.getFootprint()[3].equalsIgnoreCase("Lepidoptera"))
//				|| taxon.getName().equalsIgnoreCase("Lepidoptera"))
//			return true;
//		else if (quickGroup.contains("'cactus'")
//				&& (taxon.getFootprint().length >= 5 && taxon.getFootprint()[4].equalsIgnoreCase("CACTACEAE"))
//				|| taxon.getName().equalsIgnoreCase("CACTACEAE"))
//			return true;
//		else if (quickGroup.contains("'workingSet'")) {
//			// WORKING SET CASE!
//			HashMap<String, WorkingSetData> sets = WorkingSetCache.impl.getWorkingSets();
//			for (Entry<String, WorkingSetData> curEntry : sets.entrySet()) {
//				if (curEntry.getValue().getSpeciesIDs().contains(taxon.getId() + ""))
//					return true;
//			}
//			return false;
//		} else
//			return false;
	}

}
