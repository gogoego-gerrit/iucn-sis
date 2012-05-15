package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.iucn.sis.server.api.io.TaxomaticIO;
import org.iucn.sis.server.api.io.TaxonIO;
import org.iucn.sis.server.api.io.UserIO;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.server.api.persistance.TaxonDAO;
import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
import org.iucn.sis.server.api.utils.TaxomaticException;
import org.iucn.sis.shared.api.debug.Debug;
import org.iucn.sis.shared.api.models.Synonym;
import org.iucn.sis.shared.api.models.Taxon;
import org.iucn.sis.shared.api.models.TaxonLevel;
import org.iucn.sis.shared.api.models.TaxonStatus;
import org.iucn.sis.shared.api.models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Taxomatic extends BasicHibernateTest {

	private int[] map = new int[6];
	
	@Before
	public void buildLineage() throws TaxomaticException {
		Session session = openTransaction();
		
		UserIO userIO = new UserIO(session);
		User user = userIO.getUserFromUsername("admin");
		
		TaxonIO io = new TaxonIO(session);
		createLineage(session, io, user);
		
		closeTransaction(session);
	}
	
	@Test
	public void testDemoteNodes() throws TaxomaticException {
		//Open the transaction
		Session session = openTransaction();
		
		UserIO userIO = new UserIO(session);
		User user = userIO.getUserFromUsername("admin");
		
		/*
		 * Create two new species.  Passing the parent level Genus 
		 * grabs the genus created by buildLineage.
		 */
		Taxon species1 = addTaxon(session, "JUnit Test Species 1", TaxonLevel.SPECIES);
		Taxon species2 = addTaxon(session, "JUnit Test Species 2", TaxonLevel.SPECIES);
		
		Assert.assertEquals(TaxonLevel.SPECIES, species1.getLevel());
		Assert.assertEquals(TaxonLevel.SPECIES, species2.getLevel());
		
		//Perform the operation 
		TaxomaticIO io = new TaxomaticIO(session);
		io.demoteSpecies(species1, species2, user);
		
		//Save the ID's, we'll use these later
		int spc1ID = species1.getId(), spc2ID = species2.getId();
		
		//Close transaction & save the data.
		closeTransaction(session);
		
		//Open new read-only transaction so we can check the results.
		session = openSession();
		
		species1 = getTaxon(session, spc1ID);
		species2 = getTaxon(session, spc2ID);
		Taxon oldParent = getTaxonForLevel(session, TaxonLevel.GENUS);
		
		Assert.assertNotNull(species1);
		Assert.assertNotNull(species2);
		
		//Ensure one got demoted
		Assert.assertEquals(TaxonLevel.INFRARANK, species1.getLevel());
		Assert.assertEquals(TaxonLevel.SPECIES, species2.getLevel());
		
		//Ensure synonyms are there
		Synonym synonym = species1.getSynonyms().iterator().next();
		Debug.println(synonym.toDisplayableString());
		Assert.assertEquals(oldParent.getName(), synonym.getGenusName());
		Assert.assertEquals(species1.getName(), synonym.getSpeciesName());
		Assert.assertEquals(Synonym.NEW, synonym.getStatus());
	}
	
	@Test
	public void testMergeNodes() throws TaxomaticException {
		/*
		TaxomaticIO io = new TaxomaticIO(session);
		io.mergeTaxa(mergedTaxa, mainTaxa, user);
		*/
	}
	
	@Test
	public void testMergeUpInfrarank() throws TaxomaticException {
		/*
		TaxomaticIO io = new TaxomaticIO(session);
		io.mergeUpInfraranks(taxa, species, user);
		*/
	}
	
	@Test
	public void testPromoteNode() throws TaxomaticException {
		Session session = openTransaction();
		
		UserIO userIO = new UserIO(session);
		User user = userIO.getUserFromUsername("admin");
		
		Taxon parent = addTaxon(session, "Test Species", TaxonLevel.SPECIES);
		Taxon taxon = addTaxon(session, "Test Infrarank", TaxonLevel.INFRARANK, parent);
		
		Assert.assertEquals(TaxonLevel.INFRARANK, taxon.getLevel());
		
		int id = taxon.getId(), parentID = parent.getId();
		
		TaxomaticIO io = new TaxomaticIO(session);
		io.promoteInfrarank(taxon, user);
		
		closeTransaction(session);
		
		session = openSession();
		
		parent = getTaxon(session, parentID);
		taxon = getTaxon(session, id);
		
		Assert.assertNotNull(taxon);
		Assert.assertEquals(TaxonLevel.SPECIES, taxon.getLevel());
		Assert.assertEquals(parent.getParent(), taxon.getParent());
		
		//Ensure synonyms are there
		Synonym synonym = taxon.getSynonyms().iterator().next();
		Debug.println(synonym.toDisplayableString());
		Assert.assertEquals(parent.getParent().getName(), synonym.getGenusName());
		Assert.assertEquals(parent.getName(), synonym.getSpeciesName());
		Assert.assertEquals(taxon.getName(), synonym.getInfraName());
		Assert.assertEquals(Synonym.NEW, synonym.getStatus());
	}
	
	@Test
	public void testSplitNodes() throws TaxomaticException {
		Session session = openTransaction();
		
		addTaxon(session, "Child 1", TaxonLevel.SPECIES);
		addTaxon(session, "Child 2", TaxonLevel.SPECIES);
		addTaxon(session, "Child 3", TaxonLevel.SPECIES);
		
		Taxon parent1 = addTaxon(session, "New Parent 1", TaxonLevel.GENUS);
		Taxon parent2 = addTaxon(session, "New Parent 2", TaxonLevel.GENUS);
		
		int p1ID = parent1.getId(), p2ID = parent2.getId();
		
		closeTransaction(session);
		
		session = openTransaction();
		
		parent1 = getTaxon(session, p1ID);
		parent2 = getTaxon(session, p2ID);
		Taxon original = getTaxonForLevel(session, TaxonLevel.GENUS);
		
		Assert.assertEquals(original.getChildren().size(), 3);
		
		List<Taxon> children = new ArrayList<Taxon>(original.getChildren());
		
		HashMap<Taxon, List<Taxon>> parentToChildren = 
			new HashMap<Taxon, List<Taxon>>();
		List<Taxon> p1 = new ArrayList<Taxon>();
		p1.add(children.get(0));
		parentToChildren.put(parent1, p1);
		
		List<Taxon> p2 = new ArrayList<Taxon>();
		p2.add(children.get(1));
		p2.add(children.get(2));
		parentToChildren.put(parent2, p2);
		
		UserIO userIO = new UserIO(session);
		User user = userIO.getUserFromUsername("admin");
		
		TaxomaticIO io = new TaxomaticIO(session);
		io.splitNodes(getTaxonForLevel(session, TaxonLevel.GENUS), user, parentToChildren);
		
		Assert.assertTrue(parent1.getChildren().size() == 1);
		Assert.assertTrue(parent2.getChildren().size() == 2);
		
		closeTransaction(session);
		
		session = openSession();

		parent1 = getTaxon(session, p1ID);
		parent2 = getTaxon(session, p2ID);
		original = getTaxonForLevel(session, TaxonLevel.GENUS);
		
		Assert.assertNotNull(parent1);
		Assert.assertNotNull(parent2);
		
		Assert.assertTrue(parent1.getChildren().size() == 1);
		Assert.assertTrue(parent2.getChildren().size() == 2);
		
		Taxon child = parent1.getChildren().iterator().next();
		Synonym synonym = child.getSynonyms().iterator().next();
		
		Debug.println(synonym.toDisplayableString());
		
		Assert.assertEquals(Synonym.SPLIT, synonym.getStatus());
		Assert.assertEquals(original.getName(), synonym.getGenusName());
		Assert.assertEquals(child.getName(), synonym.getSpeciesName());
	}
	
	@Test
	public void testLateralMoveRecursive() throws TaxomaticException {
		Session session = openTransaction();
		
		Taxon oldParent = addTaxon(session, "OldParent", TaxonLevel.GENUS);
		Taxon newParent = addTaxon(session, "NewParent", TaxonLevel.GENUS);
		Taxon oldSpecies = addTaxon(session, "OldSpecies", TaxonLevel.SPECIES, oldParent);
		Taxon oldInfrarank = addTaxon(session, "OldInfrarank", TaxonLevel.INFRARANK, oldSpecies);
		
		int sourceGenus = oldParent.getId();
		int targetGenus = newParent.getId();
		int sourceSpecies = oldSpecies.getId();
		int sourceInfrarank = oldInfrarank.getId();
		
		Debug.println("Sp: {0}", oldSpecies.getFriendlyName());
		Debug.println("Ssp: {0}", oldInfrarank.getFriendlyName());
		Debug.println("IDs: {0}, sp: {1}, ssp: {2}", targetGenus, sourceSpecies, sourceInfrarank);
		
		closeTransaction(session);
		
		session = openTransaction();
		
		UserIO userIO = new UserIO(session);
		User user = userIO.getUserFromUsername("admin");
		
		newParent = (Taxon)session.load(Taxon.class, targetGenus);
		
		List<Taxon> children = new ArrayList<Taxon>();
		children.add((Taxon)session.load(Taxon.class, sourceSpecies));
		
		TaxomaticIO io = new TaxomaticIO(session);
		io.moveTaxa(newParent, children, user);
		
		closeTransaction(session);
		
		session = openSession();
		
		oldParent = (Taxon)session.load(Taxon.class, sourceGenus);
		newParent = (Taxon)session.load(Taxon.class, targetGenus);
		oldSpecies = (Taxon)session.load(Taxon.class, sourceSpecies);
		oldInfrarank = (Taxon)session.load(Taxon.class, sourceInfrarank);
		
		Debug.println("Sp: {0}", oldSpecies.getFriendlyName());
		Debug.println("Ssp: {0}", oldInfrarank.getFriendlyName());
		
		Assert.assertTrue(oldParent.getChildren().isEmpty());
		Assert.assertEquals(1, newParent.getChildren().size());
		Assert.assertTrue(oldSpecies.getFriendlyName().contains("NewParent"));
		Assert.assertEquals(sourceSpecies, oldInfrarank.getParentId());
		Assert.assertEquals(targetGenus, oldInfrarank.getParent().getParent().getId());
		Assert.assertTrue("Recursive infrarank update", oldInfrarank.getFriendlyName().contains("NewParent"));
		
		closeSession(session);
	}
	
	@Test
	public void testLateralMove() throws TaxomaticException {
		Session session = openTransaction();
		
		Taxon genus2 = addTaxon(session, "Genus 2", TaxonLevel.GENUS);
		
		Assert.assertTrue(genus2.getChildren().isEmpty());
		
		addTaxon(session, "Child of G2", TaxonLevel.SPECIES, genus2);
		
		int G2ID = genus2.getId();
		
		closeTransaction(session);
		
		session = openTransaction();
		
		UserIO userIO = new UserIO(session);
		User user = userIO.getUserFromUsername("admin");
		
		Taxon genus1 = getTaxonForLevel(session, TaxonLevel.GENUS);
		genus2 = getTaxon(session, G2ID);
		
		Assert.assertNotNull(genus1);
		Assert.assertNotNull(genus2);
		Assert.assertTrue(genus1.getChildren().isEmpty());
		Assert.assertTrue(genus2.getChildren().size() == 1);
		
		TaxomaticIO io = new TaxomaticIO(session);
		io.moveTaxa(genus1, genus2.getChildren(), user);
		
		closeTransaction(session);
		
		session = openTransaction();
		
		genus1 = getTaxonForLevel(session, TaxonLevel.GENUS);
		genus2 = getTaxon(session, G2ID);
		
		Assert.assertNotNull(genus1);
		Assert.assertNotNull(genus2);
		Assert.assertTrue(genus1.getChildren().size() == 1);
		Assert.assertTrue(genus2.getChildren().isEmpty());
		
		Taxon species = genus1.getChildren().iterator().next();
		
		Assert.assertFalse(species.getSynonyms().isEmpty());
		
		//Ensure proper synonyms got added
		Synonym synonym = species.getSynonyms().iterator().next();
		Debug.println(synonym.toDisplayableString());
		Assert.assertEquals(genus2.getName(), synonym.getGenusName());
		Assert.assertEquals(species.getName(), synonym.getSpeciesName());
		Assert.assertEquals(Synonym.NEW, synonym.getStatus());
	}
	
	private Taxon getTaxon(Session session, int id) {
		Taxon result = (Taxon)session.get(Taxon.class, id);
		init(result);
		return result;
	}
	
	private void init(Taxon taxon) {
		Hibernate.initialize(taxon.getChildren());
		Hibernate.initialize(taxon.getSynonyms());
	}
	
	private Taxon getTaxonForLevel(Session session, int level) {
		return getTaxon(session, map[level]);
	}
	
	/**
	 * Adds a new taxon to the lineage.  The taxon will be appended to 
	 * the parent at level - 1, unless level is 0, in which case, it's 
	 * a kingdom. 
	 * @param session
	 * @param name
	 * @param level
	 * @return
	 */
	private Taxon addTaxon(Session session, String name, int level) {
		return level == TaxonLevel.KINGDOM ? 
			addTaxon(session, name, level, null) : 
			addTaxon(session, name, level, level - 1);
	}
	
	/**
	 * Adds a new taxon to the lineage.  The taxon will be appended to 
	 * the taxon at the given parentLevel, accessed through the lineage. 
	 * @param session
	 * @param name
	 * @param level
	 * @param parentLevel
	 * @return
	 */
	private Taxon addTaxon(Session session, String name, int level, int parentLevel) {
		return addTaxon(session, name, level, getTaxonForLevel(session, parentLevel));
	}
	
	/**
	 * Adds a new taxon, setting the parent to the be the given parent 
	 * taxon.  Prepends JUnit Test to the name so that it can be 
	 * deleted later via the After function. 
	 * @param session
	 * @param name
	 * @param level
	 * @param parent
	 * @return
	 */
	private Taxon addTaxon(Session session, String name, int level, Taxon parent) {
		Taxon taxon = new Taxon();
		taxon.setTaxonLevel(TaxonLevel.getTaxonLevel(level));
		taxon.setName("JUnit Test - " + name);
		taxon.setStatus(TaxonStatus.STATUS_NEW);
		taxon.setState(Taxon.ACTIVE);
		taxon.setParent(parent);
		taxon.correctFullName();
		
		session.save(taxon);
		
		return taxon;
	}
	
	private void createLineage(Session session, TaxonIO io, User user) throws TaxomaticException{
		for (int i = 0; i < 6; i++) {
			Taxon parent = null;
			if (i > 0)
				parent = (Taxon) session.get(Taxon.class, map[i-1]);
			
			map[i] = addTaxon(session, TaxonLevel.displayableLevel[i], i, parent).getId();
		}
	}
	
	@After
	public void destroy() {
		Session session = SISPersistentManager.instance().openSession();
		session.beginTransaction();
		
		Criteria criteria = session.createCriteria(Taxon.class)
			.createAlias("taxonLevel", "TaxonLevel")
			.add(Restrictions.like("name", "JUnit Test", MatchMode.START))
			.addOrder(Order.desc("TaxonLevel.level"));
		
		
		for (Taxon taxon : ((List<Taxon>)criteria.list())) {
			try {
				//Debug.println("Deleting {0}", taxon.getFriendlyName());
				TaxonDAO.deleteAndDissociate(taxon, session);
			} catch (PersistentException e) {
				e.printStackTrace();
				session.getTransaction().rollback();
			}
		}
		
		session.getTransaction().commit();
		session.close();
	}

}
