package org.iucn.sis.server.api.io;

import java.util.Date;
import java.util.List;

import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.persistance.UserDAO;
import org.iucn.sis.server.api.persistance.WorkingSetCriteria;
import org.iucn.sis.server.api.persistance.WorkingSetDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.debug.Debug;
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

	/**
	 * 
	 * @deprecated use getSubscribedWorkingSets(Integer userID) instead
	 */
	public WorkingSet[] getSubscribedWorkingSets(String userName) throws PersistentException {
		WorkingSetCriteria criteria = new WorkingSetCriteria();
		criteria.createSubscribedUsersCriteria().username.eq(userName);
		return WorkingSetDAO.listWorkingSetByCriteria(criteria);
	}

	/**
	 * FIXME: This needs to be based off the user ID, not the username.
	 * @deprecated
	 */
	public WorkingSet[] getUnsubscribedWorkingSets(String userName) throws PersistentException {
		/*String queryString = "SELECT id FROM working_set WHERE id not in (Select working_setid FROM (SELECT id from \"user\" where username='" 
			+ userName + "') t JOIN working_set_subscribe_user on userid = t.id);";*/
		
		String queryString = "SELECT * FROM working_set " +
			"JOIN working_set_subscribe_user ON working_set.id = working_set_subscribe_user.working_setid " +
			"JOIN \"user\" ON working_set_subscribe_user.userid = \"user\".id " +
			"WHERE \"user\".username != '"+userName+"'";
		
		List<WorkingSet> list = 
			SIS.get().getManager().getSession().createSQLQuery(queryString).addEntity(WorkingSet.class).list();
		
		return list.toArray(new WorkingSet[list.size()]);
		
		/*
		
		List<Integer> results  = (List<Integer>) SIS.get().getManager().getSession().createSQLQuery(queryString).list();
		if (!results.isEmpty()) {
			WorkingSetCriteria criteria = new WorkingSetCriteria();
			criteria.id.in(results.toArray(new Integer[results.size()]));
			return WorkingSetDAO.listWorkingSetByCriteria(criteria);
		} else {
			return new WorkingSet[]{};
		}*/
		
		
	}

	public boolean deleteWorkingset(WorkingSet workingSet) {
		try {
			return WorkingSetDAO.deleteAndDissociate(workingSet);
		} catch (PersistentException e) {
			Debug.println(e);
			return false;
		}
	}

	/**
	 * FIXME: This needs to be based off the user ID, not the username.
	 * @deprecated
	 */
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
				Debug.println(e);
			}
		}
		return false;
	}

	/**
	 * FIXME: This needs to be based off the user ID, not the username.
	 * @deprecated
	 */
	public boolean subscribeToWorkingset(Integer workingsetID, String username) {
		WorkingSet ws = readWorkingSet(workingsetID);
		if (ws == null)
			return false;
		
		User user = SIS.get().getUserIO().getUserFromUsername(username);
		if (user == null)
			return false;
				
		ws.getUsers().add(user);
		user.getSubscribedWorkingSets().add(ws);
				
		try {
			WorkingSetDAO.save(ws);
			UserDAO.save(user);
			
			// TODO: MIGHT NEED TO FIGURE OUT HOW TO REFERENCE ALL FIELDS... THAT WOULD BE SAD
			afterSaveWS(ws);
					
			return true;
		} catch (PersistentException e) {
			Debug.println(e);
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

	@SuppressWarnings("unchecked")
	public WorkingSet[] getWorkingSetsForTaxon(Integer taxonID) throws PersistentException {
		/*
		 * Let's leave this since we're not allowed to do example queries for 
		 * things that are not specified as properties.  Also, future req's 
		 * will make us change this code anyway.
		 * 
		 * Changed to select * instead of select ID...
		 */
		String queryString = "select * from working_set where working_set.id in " + 
			"(select working_setid from working_set_taxon where taxonid = '"+taxonID+"');";
		
		/*
		 * ...because calling addEntity() transforms the results to the 
		 * proper object type, no second query necessary. 
		 */
		List<WorkingSet> results =
			SIS.get().getManager().getSession().createSQLQuery(queryString)
			.addEntity(WorkingSet.class)
			.list();
		
		return results.toArray(new WorkingSet[results.size()]);		
	}
}
