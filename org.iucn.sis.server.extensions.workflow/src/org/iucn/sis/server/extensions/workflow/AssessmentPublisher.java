package org.iucn.sis.server.extensions.workflow;

import org.iucn.sis.server.api.utils.FormattedDate;
import org.iucn.sis.shared.api.models.Assessment;
import org.iucn.sis.shared.api.models.AssessmentType;
import org.iucn.sis.shared.api.models.Reference;
import org.iucn.sis.shared.api.models.fields.RedListPublicationField;
import org.iucn.sis.shared.api.utils.CanonicalNames;

public class AssessmentPublisher {
	
	
	/**
	 * Performs the standard publication steps for draft assessments. 
	 * 
	 * @param data - assessment to be published, must be Draft type
	 * @param pubRef is the reference that will be attached to the RedListPublication field
	 * @return true if assessment parameter was successfully migrated to Published status
	 */
	public boolean publishAssessment(Assessment data, Reference pubRef) {
		if (true)
			throw new UnsupportedOperationException();
		
		if (!data.isPublished()) {
			data.setType(AssessmentType.PUBLISHED_ASSESSMENT_TYPE);
			data.setDateFinalized(FormattedDate.impl.getDate());
			if (data.getField(CanonicalNames.RedListPublication) == null) {
				data.getField().add(new RedListPublicationField());
			}
			data.addReference(pubRef, CanonicalNames.RedListPublication);
						
			//putAuthorsIfNotNull(data);
			
			return true;
		} else
			return false;
	}
	
	/*private void putAuthorsIfNotNull(Assessment data) {
		ArrayList<String> arr;
		String curAuthors = data.getFirstDataPiece(CanonicalNames.RedListAssessmentAuthors, "");
		if( curAuthors.equals("") ) {
			arr = new ArrayList<String>();
			List<String> structures = (List<String>)data.getDataMap().get(CanonicalNames.RedListAssessors);
			
			if( structures != null && structures.size() > 0 ) {
				String s = structures.get(0);
				if( s == null || s.equals("") ) {
					List<User> userList = new ArrayList<User>();
					for (int i = 2; i < structures.size(); i++) { 
						//START AT 2 - index 1 is now just the total number of users...
						String curID = structures.get(i);
						if( !curID.equals("0") ) {
							if (users.containsKey(curID) )
								userList.add(users.get(curID));
							else
								System.out.println("Could not find user with ID " + curID);
						}
					}

					s = generateTextFromUsers(userList);
				}
				arr.add(s);
				data.getDataMap().put(CanonicalNames.RedListAssessmentAuthors, arr);
			}
		}
	}
	
	private String generateTextFromUsers(List<User> userList) {
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < userList.size(); i++) {
			text.append(userList.get(i).getCitationName());
			
			if (i + 1 < userList.size() - 1)
				text.append(", ");

			else if (i + 1 == userList.size() - 1)
				text.append(" & ");
		}
		
		return text.toString();
	}*/
}
