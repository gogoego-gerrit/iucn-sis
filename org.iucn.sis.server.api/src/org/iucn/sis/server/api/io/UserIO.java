package org.iucn.sis.server.api.io;

import org.iucn.sis.server.api.persistance.UserCriteria;
import org.iucn.sis.server.api.persistance.UserDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.ServerPaths;
import org.iucn.sis.shared.api.models.User;

import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;

public class UserIO {
	
	private final VFS vfs;
	
	public UserIO(VFS vfs) {
		this.vfs = vfs;
	}

	public boolean removeSISPermission(String username) {
		User user = getUserFromUsername(username);
		if (user != null) {
			user.setSisUser(false);
			try {
				return saveUser(user);
			} catch (PersistentException e) {
				return false;
			}
		}
		return false;
	}

	public User getUserFromUsername(String username) {
		UserCriteria criteria;
		try {
			criteria = new UserCriteria();
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		criteria.username.eq(username);
		return UserDAO.getUserByCriteria(criteria);
	}

	public boolean trashUser(String username) {
		User user = getUserFromUsername(username);
		if (user != null) {
			user.state = User.DELETED;
			try {
				return saveUser(user);
			} catch (PersistentException e) {
				return false;
			}
		}
		return false;
	}

	public boolean saveUser(User user) throws PersistentException {
		if (UserDAO.save(user)) {
			try {
				afterSave(user);
			} catch (NotFoundException e) {
				//FIXME: If you don't care, why throw the exception?
				TrivialExceptionHandler.ignore(this, e);
			} catch (ConflictException e) {
				//FIXME: If you don't care, why throw the exception?
				TrivialExceptionHandler.ignore(this, e);
			}	
			return true;
		}
		else
			return false;
	}

	public void afterSave(User user) throws NotFoundException, ConflictException {
		if (!vfs.exists(ServerPaths.getUserPath(user.getUsername())))
			vfs.makeCollection(ServerPaths.getUserPath(user.getUsername()));
	}

	public User[] getAllUsers() throws PersistentException {
		return UserDAO.getUsersByCriteria(new UserCriteria());
	}

	public User[] getAllSISUsers() throws PersistentException {
		UserCriteria criteria = new UserCriteria();
		criteria.sisUser.eq(true);
		return UserDAO.getUsersByCriteria(criteria);
	}

	private String determineNewGroup(String curGroup) {
		String newGroup = "";
		if (curGroup == null)
			newGroup = "sysAdmin";
		else if (curGroup.equals("'guest'"))
			newGroup = "guest";
		else if (curGroup.equals("'workingSet','no_taxomatic'"))
			newGroup = "workingSetAssessor";
		else if (curGroup.equals("'workingSet','no_taxomatic','can_batch','canCreateDraft','reference_replacer'"))
			newGroup = "workingSetFacilitator,batchChangeUser,referenceReplaceUser";
		else if (curGroup.equals("'molluscs','no_taxomatic'"))
			newGroup = "molluscsAssessor";
		else if (curGroup.contains("'bryophyta','no_taxomatic'"))
			newGroup = "bryophytaFacilitator";
		else if (curGroup.equals("'cephalopods','workingSet','no_taxomatic'"))
			newGroup = "cephalopodAssessor,workingSetAssessor";
		else if (curGroup.equals("'Lepidoptera','workingSet','no_taxomatic'"))
			newGroup = "lepidopteraAssessor,workingSetAssessor";
		else if (curGroup.equals("'Lepidoptera','no_taxomatic'"))
			newGroup = "lepidopteraAssessor";
		else if (curGroup.equals("'gaa','gma'"))
			newGroup = "gaaAssessor,gmaAssessor,taxomaticUser";
		else if (curGroup.equals("'gaa','gma','canEditPublished'"))
			newGroup = "gaaAdmin,gmaAdmin,taxomaticUser";
		else if (curGroup.equals("'gaa','gma','canEditPublished','no_taxomatic'"))
			newGroup = "gaaAdmin,gmaAdmin";
		else if (curGroup.contains("'gaa','canCreateDraft','canEditPublished'"))
			newGroup = "gaaAdmin,gmaAdmin";
		else if (curGroup.equals("'rlu','workingSet'"))
			newGroup = "rlu";
		else if (curGroup.equals("'gma','canEditPublished','workingSet'"))
			newGroup = "gmaAdmin,workingSetAdmin";
		else if (curGroup.contains("'workingSet'"))
			newGroup = "workingSetAssessor";
		else if (curGroup.startsWith("'gaa','reptiles','canEditPublished','canCreateDraft','canDeleteDraft'"))
			newGroup = "gaaAdmin,reptilesAdmin";
		else if (curGroup.equals("'reptiles'"))
			newGroup = "reptilesAssessor";
		else if (curGroup.equals("'Cambaridae','workingSet','can_batch','canCreateDraft'"))
			newGroup = "cambaridaeFacilitator,workingSetFacilitator,batchChangeUser";
		else if (curGroup.equals("'Cambaridae','workingSet','canCreateDraft'"))
			newGroup = "cambaridaeFacilitator,workingSetFacilitator";

		if (curGroup.contains("can_batch"))
			newGroup += ",batchChangeUser";
		if (curGroup.contains("reference_replacer"))
			newGroup += ",referenceReplaceUser";
		if (curGroup.contains("find_replace"))
			newGroup += ",findReplaceUser";
		if (curGroup.contains("no_taxomatic"))
			newGroup += ",redactTaxomatic";

		return newGroup;
	}
}
