package org.iucn.sis.server.api.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.iucn.sis.server.api.application.SIS;
import org.iucn.sis.server.api.permissions.PermissionUtils;
import org.iucn.sis.server.api.persistance.WorkingSetCriteria;
import org.iucn.sis.server.api.persistance.WorkingSetDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.shared.api.acl.BasePermissionUtils;
import org.iucn.sis.shared.api.acl.base.AuthorizableObject;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Edit;
import org.iucn.sis.shared.api.models.User;
import org.iucn.sis.shared.api.models.WorkingSet;

/**
 * Performs file system IO operations for Working Sets.
 * 
 * @author adam.schwartz
 * 
 */
public class WorkingSetIO {

	private final Session session; 
	private final UserIO userIO;
	
	public WorkingSetIO(Session session) {
		this.session = session;
		this.userIO = new UserIO(session);
	}

	public WorkingSet readWorkingSet(Integer id) {
		try {
			return WorkingSetDAO.getWorkingSetByORMID(session, id);
		} catch (PersistentException e) {
			// TODO Auto-generated catch block
			Debug.println(e);
			return null;
		}
	}

	public WorkingSet[] getCreatedWorkingSets(Integer userID) throws PersistentException {
		WorkingSetCriteria criteria = new WorkingSetCriteria(session);
		criteria.createCreatorCriteria().id.eq(userID);
		return WorkingSetDAO.listWorkingSetByCriteria(criteria);
	}
	
	public WorkingSet[] getSubscribedWorkingSets(Integer userID) throws PersistentException {
		WorkingSetCriteria criteria = new WorkingSetCriteria(session);
		criteria.createSubscribedUsersCriteria().id.eq(userID);
		return WorkingSetDAO.listWorkingSetByCriteria(criteria);
	}

	/**
	 * 
	 * @deprecated use getSubscribedWorkingSets(Integer userID) instead
	 */
	public WorkingSet[] getSubscribedWorkingSets(String userName) throws PersistentException {
		WorkingSetCriteria criteria = new WorkingSetCriteria(session);
		criteria.createSubscribedUsersCriteria().username.eq(userName);
		return WorkingSetDAO.listWorkingSetByCriteria(criteria);
	}

	/**
	 * FIXME: This needs to be based off the user ID, not the username.
	 * @deprecated
	 */
	public WorkingSet[] getUnsubscribedWorkingSets(String userName) throws PersistentException {
		User user = userIO.getUserFromUsername(userName);
		if (user == null)
			return new WorkingSet[0];
		
		return getUnsubscribedWorkingSets(user.getId());
	}
	
	@SuppressWarnings("unchecked")
	public WorkingSet[] getUnsubscribedWorkingSets(int userid) throws PersistentException {
		String queryString = SIS.get().getQueries().getSubscribableWorkingSets(userid);
		
		List<WorkingSet> list = 
			session.createSQLQuery(queryString).addEntity(WorkingSet.class).list();
		
		return list.toArray(new WorkingSet[list.size()]);
	}
	
	public List<WorkingSet> getGrantableWorkingSets(int userid) throws PersistentException {
		final User user = new UserIO(session).getUser(userid);
		if (user == null)
			return new ArrayList<WorkingSet>();
			
		final List<WorkingSet> list = new ArrayList<WorkingSet>();
		final PermissionUtils perm = new PermissionUtils(session, user);
		for (WorkingSet ws : SIS.get().getManager().listObjects(WorkingSet.class, session)) {
			if (perm.hasPermission(AuthorizableObject.GRANT, ws))
				list.add(ws);
		}
		
		return list;
	}

	public boolean deleteWorkingset(WorkingSet workingSet) {
		try {
			return WorkingSetDAO.deleteAndDissociate(workingSet, session);
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
		User user = userIO.getUserFromUsername(username);
		if (user == null)
			return false;
		
		return unsubscribeFromWorkingset(user, id);
	}
	
	public boolean unsubscribeFromWorkingset(User user, Integer id) {
		WorkingSet ws = readWorkingSet(id);
		
		if (ws.getUsers().contains(user)) {
			ws.getUsers().remove(user);
			user.getSubscribedWorkingSets().remove(ws);
			
			try {
				SIS.get().getManager().saveObject(session, ws);
				SIS.get().getManager().saveObject(session, user);
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
	public boolean subscribeToWorkingset(Integer workingSetID, String username) {
		User user = userIO.getUserFromUsername(username);
		if (user == null)
			return false;
		
		return subscribeToWorkingSet(workingSetID, user);
	}
	
	public boolean subscribeToWorkingSet(Integer workingSetID, User user) {
		WorkingSet ws = readWorkingSet(workingSetID);
		if (ws == null)
			return false;
		
		//Ensure user is not already subscribed...
		if (ws.getUsers().contains(user))
			return true;
		
		ws.getUsers().add(user);
		user.getSubscribedWorkingSets().add(ws);
				
		try {
			session.update(ws);
			session.update(user);
			
			// TODO: MIGHT NEED TO FIGURE OUT HOW TO REFERENCE ALL FIELDS... THAT WOULD BE SAD
			// afterSaveWS(ws);
					
			return true;
		} catch (HibernateException e) {
			Debug.println(e);
			return false;
		}
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
			SIS.get().getManager().saveObject(session, ws);
			
			// TODO: MIGHT NEED TO FIGURE OUT HOW TO REFERENCE ALL
			// FIELDS... THAT WOULD BE SAD
			afterSaveWS(ws);
		
			return true;
		} catch (PersistentException e) {
			Debug.println(e);
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
		String queryString = SIS.get().getQueries().getWorkingSetsForTaxon(taxonID);
		
		/*
		 * ...because calling addEntity() transforms the results to the 
		 * proper object type, no second query necessary. 
		 */
		List<WorkingSet> results =
			session.createSQLQuery(queryString)
			.addEntity(WorkingSet.class)
			.list();
		
		return results.toArray(new WorkingSet[results.size()]);		
	}
}
