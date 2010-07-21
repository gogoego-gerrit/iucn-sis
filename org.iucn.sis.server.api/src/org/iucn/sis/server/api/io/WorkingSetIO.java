package org.iucn.sis.server.api.io;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.UserDAO;
import org.iucn.sis.server.api.persistance.WorkingSetCriteria;
import org.iucn.sis.server.api.persistance.WorkingSetDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;

import com.solertium.vfs.provider.VersionedFileVFS;

/**
 * Performs file system IO operations for Working Sets.
 * 
 * @author adam.schwartz
 * 
 */
public class WorkingSetIO {

	protected VersionedFileVFS vfs;

	public WorkingSetIO(VersionedFileVFS vfs) {
		this.vfs = vfs;
	}

	public WorkingSet readWorkingSet(Integer id) {
		try {
			return WorkingSetDAO.getWorkingSetByORMID(id);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public WorkingSet[] getCreatedWorkingSets(Integer userID) throws PersistentException {
		WorkingSetCriteria criteria = new WorkingSetCriteria();
		criteria.createCreatorCriteria().id.eq(userID);
		return WorkingSetDAO.listWorkingSetByCriteria(criteria);
	}
	
	public WorkingSet[] getSubscribedWorkingSets(Integer userID) throws PersistentException {
		WorkingSetCriteria criteria = new WorkingSetCriteria();
		criteria.createSubscribedUsersCriteria().id.eq(userID);
		return WorkingSetDAO.listWorkingSetByCriteria(criteria);
	}

	public WorkingSet[] getSubscribedWorkingSets(String userName) throws PersistentException {
		WorkingSetCriteria criteria = new WorkingSetCriteria();
		criteria.createSubscribedUsersCriteria().username.eq(userName);
		return WorkingSetDAO.listWorkingSetByCriteria(criteria);
	}

	public WorkingSet[] getUnsubscribedWorkingSets(String userName) throws PersistentException {
		String queryString = "SELECT id FROM working_set WHERE id not in (Select working_setid FROM (SELECT id from \"user\" where username='" 
			+ userName + "') t JOIN working_set_subscribe_user on userid = t.id);";
		
		System.out.println(queryString);
		List<Integer> results  = (List<Integer>) SIS.get().getManager().getSession().createSQLQuery(queryString).list();
		if (!results.isEmpty()) {
			WorkingSetCriteria criteria = new WorkingSetCriteria();
			criteria.id.in(results.toArray(new Integer[results.size()]));
			return WorkingSetDAO.listWorkingSetByCriteria(criteria);
		} else {
			return new WorkingSet[]{};
		}
		
		
	}

	public boolean deleteWorkingset(WorkingSet workingSet) {
		try {
			return WorkingSetDAO.deleteAndDissociate(workingSet);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean unsubscribeFromWorkingset(String username, Integer id) {
		WorkingSet ws = readWorkingSet(id);
		User user = SIS.get().getUserIO().getUserFromUsername(username);
		if (ws.getUsers().contains(user)) {
			ws.getUsers().remove(user);
			user.getSubscribedWorkingSets().remove(ws);
			
			try {
				WorkingSetDAO.save(ws);
				UserDAO.save(user);
				// TODO: MIGHT NEED TO FIGURE OUT HOW TO REFERENCE ALL
				// FIELDS... THAT WOULD BE SAD
				afterSaveWS(ws);
				
				return true;
			} catch (PersistentException e) {
				e.printStackTrace();
				
			}

		}
		return false;
	}

	public boolean subscribeToWorkingset(Integer workingsetID, String username) {
		WorkingSet ws = readWorkingSet(workingsetID);
		if (ws != null) {
			User user = SIS.get().getUserIO().getUserFromUsername(username);
			if (user != null) {
				ws.getUsers().add(user);
				user.getSubscribedWorkingSets().add(ws);
				
				try {
					WorkingSetDAO.save(ws);
					UserDAO.save(user);
					// TODO: MIGHT NEED TO FIGURE OUT HOW TO REFERENCE ALL
					// FIELDS... THAT WOULD BE SAD
					afterSaveWS(ws);
					
					return true;
				} catch (PersistentException e) {
					e.printStackTrace();
				}

			}
		}
		return false;
	}

	public boolean saveWorkingSet(WorkingSet ws, User user) {
		
		Edit edit = new Edit();
		edit.getWorking_set().add(ws);
		edit.setUser(user);
		if (ws.getCreatedDate() == null) {
			ws.setCreatedDate(new Date());
		}
		ws.getEdit().add(edit);
		try {
			WorkingSetDAO.save(ws);
			
			// TODO: MIGHT NEED TO FIGURE OUT HOW TO REFERENCE ALL
			// FIELDS... THAT WOULD BE SAD
			afterSaveWS(ws);
		
			return true;
		} catch (PersistentException e) {
			e.printStackTrace();
		}

		return false;
	}

	protected void afterSaveWS(WorkingSet ws) {

	}


}
