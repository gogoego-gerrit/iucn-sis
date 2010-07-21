package org.iucn.sis.server.crossport.server;

import java.util.HashMap;

import org.iucn.sis.shared.taxonomyTree.TaxonomyTree;

import com.solertium.db.BackgroundExecutionContext;
import com.solertium.db.DBSession;
import com.solertium.db.ExecutionContext;
import com.solertium.vfs.VFS;

/**
 * This will perform an import of DEM data, creating taxa as it goes if required
 * and adding assessments and their data as required. This process speaks with a
 * running SIS server to achieve creation of taxa and assessments, so please
 * ensure a server is running.
 * 
 * @author adam.schwartz
 */
public class DEMExport {

	private DBSession dbsess;
	private ExecutionContext ec = new BackgroundExecutionContext(DEMExport.class.getName());
	private TaxonomyTree tree;

	private VFS vfs;

	private HashMap<Long, String> spcIDMap;

	public DEMExport() {
		spcIDMap = new HashMap<Long, String>();

	}

	// public void doImport() throws Exception
	// {
	// registerDatasource(
	// "jdbc:access:/f:/Documents and Settings/Liz/IUCN/SIS Toolkit/Global_mangroves_27feb08.mdb"
	// ,
	// "com.hxtt.sql.access.AccessDriver","","");
	//		
	//		
	// }
	//	
	// private void registerDatasource(String URL, String driver, String
	// username, String pass)
	// throws Exception
	// {
	// ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	// DBSession.register_datasource("dem", URL, driver, username, pass);
	//		
	// dbsess = DBSession.get("dem");
	// ec.setDBSession( dbsess );
	// }
	//	
	// public void buildTree() throws Exception
	// {
	// tree = new TaxonomyTree();
	//		
	// SelectQuery select = new SelectQuery();
	// select.select("Systematics", "*");
	//
	// Row.Set rows = new Row.Set();
	//
	// ec.doQuery(select, rows);
	//
	// int id = 0;
	//		
	// TaxonNode plantae = TaxonNodeFactory.createNode(id, "PLANTAE", 0, "",
	// tree);
	// id++;
	// tree.addNode(plantae);
	//		
	// List<Row> rowList = rows.getSet();
	//		
	// for( Iterator<Row> iter = rowList.listIterator(); iter.hasNext(); )
	// {
	// Row curCol = iter.next();
	//
	// TaxonNode phylumN;
	// TaxonNode classN;
	// TaxonNode orderN;
	// TaxonNode familyN;
	// TaxonNode genusN;
	// TaxonNode speciesN;
	//			
	// String curPhylum = curCol.get("Phylum").getString();
	// String curClass = curCol.get("Class").getString();
	// String curOrder = curCol.get("Order").getString();
	// String curFamily = curCol.get("Family").getString();
	// String curGenus = curCol.get("Genus").getString();
	// String curSpecies = curCol.get("Species").getString();
	//			
	// phylumN = tree.getNode( TaxonNode.PHYLUM, curPhylum);
	// if( phylumN == null )
	// {
	// phylumN = TaxonNodeFactory.createNode(id, curPhylum, TaxonNode.PHYLUM,
	// "PLANTAE", tree);
	// phylumN.setFullName( phylumN.getName() );
	//				
	// id++;
	// plantae.addChild( phylumN );
	// }
	//			
	// classN = tree.getNode( TaxonNode.CLASS, curClass );
	// if( classN == null )
	// {
	// classN = TaxonNodeFactory.createNode(id, curClass,
	// TaxonNode.CLASS, curPhylum, tree);
	// classN.setFullName( classN.getName() );
	// id++;
	//				
	// phylumN.addChild( classN );
	// }
	//			
	// orderN = tree.getNode( TaxonNode.ORDER, curOrder );
	// if( orderN == null )
	// {
	// orderN = TaxonNodeFactory.createNode(id, curOrder,
	// TaxonNode.ORDER, curClass, tree);
	// id++;
	//
	// orderN.setFullName( orderN.getName() );
	// classN.addChild( orderN );
	// }
	//			
	// familyN = tree.getNode( TaxonNode.FAMILY, curFamily );
	// if( familyN == null )
	// {
	// familyN = TaxonNodeFactory.createNode(id, curFamily,
	// TaxonNode.FAMILY, curOrder, tree);
	// id++;
	// familyN.setFullName( familyN.getName() );
	//				
	// orderN.addChild( familyN );
	// }
	//			
	// genusN = tree.getNode( TaxonNode.GENUS, curGenus );
	// if( genusN == null )
	// {
	// genusN = TaxonNodeFactory.createNode(id, curGenus,
	// TaxonNode.GENUS, curFamily, tree);
	// genusN.setFullName( genusN.getName() );
	// id++;
	//				
	// familyN.addChild( genusN );
	// }
	//			
	// speciesN = tree.getNode( TaxonNode.SPECIES, curSpecies );
	// if( speciesN == null )
	// {
	// speciesN = TaxonNodeFactory.createNode(id, curSpecies,
	// TaxonNode.SPECIES, curGenus, tree);
	// speciesN.setFullName( curGenus + " " + speciesN.getName() );
	//				
	// SysDebugger.getInstance().println( "<species>" +id+ "</species>" );
	//				
	// id++;
	//				
	// genusN.addChild( speciesN );
	// }
	//
	// }
	//
	// // String hierarchy = "<taxonomicHierarchy>\r\n";
	// // hierarchy += TaxonNodeFactory.toRecursiveVerboseXML( plantae );
	// // hierarchy += "</taxonomicHierarchy>";
	// //
	// export();
	// }
	//	
	// private static void setVFSRoot( String vfsroot )
	// {
	// vfs = VFSFactory.getVFS(vfsroot);
	// SysDebugger.getInstance().println("VFS started with root directory " +
	// vfsroot );
	// }
	//	
	// private static void export()
	// {
	// setVFSRoot( "F:/Documents and Settings/Liz/IUCN/SIS Toolkit/sis/vfs/" );
	//		
	// Collection<TaxonNode> kingdoms = tree.getLevel( TaxonNode.KINGDOM
	// ).values();
	// String hierarchy = "<taxonomicHierarchy>\r\n";
	//		
	// for( Iterator<TaxonNode> iter = kingdoms.iterator(); iter.hasNext(); )
	// hierarchy += TaxonNodeFactory.toRecursiveVerboseXML( iter.next() );
	//		
	// hierarchy += "</taxonomicHierarchy>";
	//		
	// try
	// {
	// System.err.println("Writing " + hierarchy.length() + " characters.");
	//			
	// if( vfs.exists( "/browse/taxonomy/taxonomy.xml" ) )
	// vfs.delete( "/browse/taxonomy/taxonomy.xml" );
	//			
	// Writer writer = vfs.getWriter( "/browse/taxonomy/taxonomy.xml" );
	// writer.write( hierarchy );
	// writer.close();
	//			
	// String nodeURL = "/browse/nodes/";
	// for( int i = 0; i < tree.getNumberOfLevels(); i++ )
	// {
	// Collection<TaxonNode> curLevel = tree.getLevel(i).values();
	//				
	// for( Iterator<TaxonNode> iter = curLevel.iterator(); iter.hasNext(); )
	// {
	// TaxonNode curNode = iter.next();
	//					
	// String stripedNodeID = FilenameStriper.getIDAsStripedPath(
	// curNode.getId() );
	// if( vfs.exists( nodeURL + stripedNodeID + ".xml" ) )
	// vfs.delete( nodeURL + stripedNodeID + ".xml" );
	//								
	// Writer temp = vfs.getWriter( nodeURL + stripedNodeID + ".xml" );
	// temp.write( TaxonNodeFactory.nodeToDetailedXML( curNode ) );
	// temp.close();
	//					
	// for( Iterator<AssessmentData> assessmentIter =
	// curNode.getAssessments().iterator(); assessmentIter.hasNext(); )
	// {
	// AssessmentData cur = assessmentIter.next();
	// cur.setSpeciesID( new Long( curNode.getId() ).toString() );
	// cur.setSpeciesName( curNode.getFullName() );
	//						
	// Writer assessWriter = null;
	//						
	// String stripedAssID = FilenameStriper.getIDAsStripedPath(
	// cur.getAssessmentID() );
	// try
	// {
	// if( vfs.exists( "/browse/assessments/" + stripedAssID + ".xml" ) )
	// vfs.delete( "/browse/assessments/" + stripedAssID + ".xml" );
	//								
	// assessWriter = vfs.getWriter( "/browse/assessments/" + stripedAssID +
	// ".xml");
	// assessWriter.write( cur.toXML() );
	// assessWriter.close();
	// }
	// catch (Exception e) {
	// SysDebugger.getInstance().println("Exception writing assessment.");
	// e.printStackTrace();
	// assessWriter.close();
	// }
	// }
	// }
	//				
	// SysDebugger.getInstance().println("Finished exporting node data for level "
	// + TaxonNode.displayableLevel[i] );
	// }
	// }
	// catch (Exception e) {
	// SysDebugger.getInstance().println("Error exporting SIS RLDB data.");
	// e.printStackTrace();
	// }
	// }
}