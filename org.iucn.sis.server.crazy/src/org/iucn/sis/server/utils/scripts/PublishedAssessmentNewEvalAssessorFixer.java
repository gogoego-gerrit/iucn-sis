package org.iucn.sis.server.utils.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.NamingException;

import org.iucn.sis.server.simple.SISContainerApp;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.MostRecentFlagger;
import org.iucn.sis.shared.acl.User;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.ExperimentalSelectQuery;
import com.solertium.vfs.VFS;

public class PublishedAssessmentNewEvalAssessorFixer extends BasePublishedAssessmentModder {
	public static class PublishedAssessmentNewEvalAssessorFixerResource extends Resource {

		public PublishedAssessmentNewEvalAssessorFixerResource() {
		}

		public PublishedAssessmentNewEvalAssessorFixerResource(final Context context, final Request request,
				final Response response) {
			super(context, request, response);
			getVariants().add(new Variant(MediaType.TEXT_HTML));
		}

		@Override
		public Representation represent(final Variant variant) {
			boolean writeback = false;
			String wb = (String)getRequest().getAttributes().get("writeback");
			if( wb != null && wb.equalsIgnoreCase("true") )
				writeback = true;
			
			System.out.println("Writeback is " + writeback);
			try {
				if (!BasePublishedAssessmentModder.running) {
					new Thread(new PublishedAssessmentNewEvalAssessorFixer(SISContainerApp.getStaticVFS(), writeback)).run();
					System.out.println("Started a new historian!");
				} else
					System.out.println("A published assessment script is already running!");

				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append(BasePublishedAssessmentModder.results.toString());
				sb.append("</body></html>");

				return new StringRepresentation(sb, MediaType.TEXT_HTML);
			} catch (Exception e) {
				
				StringBuilder sb = new StringBuilder();
				sb.append("<html><head></head><body style='font-family:Verdana; font-size:x-small'>");
				sb.append(DocumentUtils.getStackTraceAsString(e));
				sb.append("</body></html>");
				return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
			}
		}
	}

	private StringBuilder sql;
	private HashMap<String, User> users;
	private int numFixed = 0;
	private boolean writeback = false;
	
	public PublishedAssessmentNewEvalAssessorFixer(File vfsRoot, boolean writeback) throws Exception {
		super(vfsRoot);
		this.writeback = writeback;
	}

	public PublishedAssessmentNewEvalAssessorFixer(VFS vfs, boolean writeback) throws Exception {
		super(vfs);
		this.writeback = writeback;
	}

	@Override
	public void run() {
		sql = new StringBuilder();
		
		try {
			getUserInfo();
			super.run();

			System.out.println(sql.toString());
			System.out.println("NumFixed is " + numFixed);
			
			DocumentUtils.writeVFSFile("/fixEvalAssessors.sql", vfs, sql.toString());
		} catch (NamingException e) {
			System.out.println("Couldn't get user info. Bailing.");
		}
		
	}
	
	private void getUserInfo() throws NamingException {
		users = new HashMap<String, User>();
		
		SystemExecutionContext ec2 = new SystemExecutionContext("users");
		ec2.setExecutionLevel(ExecutionContext.ADMIN);
		ec2.setAPILevel(ExecutionContext.SQL_ALLOWED);
		
		final ExperimentalSelectQuery query = new ExperimentalSelectQuery();
		query.select("user", "*");
		query.select("profile", "firstname");
		query.select("profile", "lastname");
		query.select("profile", "initials");
		query.select("profile", "affiliation");
		
		final Row.Set rs = new Row.Set();
		try {
			ec2.doQuery(query, rs);
			
			for( Row curRow : rs.getSet() ) {
				String f = curRow.get("firstname").getString();
				String l = curRow.get("lastname").getString();
				String i = curRow.get("initials").getString();
				String id = curRow.get("id").getString();
				
				User user = new User();
				user.setFirstName(f);
				user.setLastName(l);
				user.setInitials(i);
				
				users.put(id, user);
			}
		} catch (DBException e) {
			e.printStackTrace();
		}
	}
	
	
	private void fixAssessorEval(AssessmentData data) {
		String [] fields = new String[] { "RedListEvaluators", "RedListContributors", 
				"RedListAssessors" };
		
		for( String curField : fields ) {
			boolean fixed = false;
			
			List<String> structures = (List<String>)data.getDataMap().get(curField);
			if( structures != null && structures.size() > 0 ) {
				String s = structures.get(0);
				if( s == null || s.equals("") ) {
					List<User> userList = new ArrayList<User>();
					for (int i = 1; i < structures.size(); i++) { 
						//START AT 2 - index 1 is now just the total number of users...
						String curID = structures.get(i);
						
						if( i == 1 ) {
							if (curID.equals("2") && structures.size() > 3)
								fixed = true;
						} else if( !curID.equals("0") ) {
							if( users.containsKey(curID) )
								userList.add(users.get(curID));
							else
								System.out.println("Could not find user with ID " + curID);
						}
					}
					
					s = generateTextFromUsers(userList);
				}
				
				if( fixed ) {
					sql.append("UPDATE assessment SET " + curField.toLowerCase().replace("redlist", "") + "='" + s + "' WHERE id='" + data.getAssessmentID()+"';\n");
					numFixed++;
				}
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
	}
	
	@Override
	protected void workOnFullList(List<AssessmentData> assessments) {
		
		List<AssessmentData> updatedHistorical = MostRecentFlagger.flagMostRecentInList(assessments);
		
		if( updatedHistorical.size() > 0 ) {
			System.out.println("Changed " + updatedHistorical.size() + " historical flags for " +
					"taxon " + updatedHistorical.get(0).getSpeciesID());
		}
		
		for( AssessmentData data : assessments ) {
			if( !data.isHistorical() )
				fixAssessorEval(data);
			
		}
	}
	
	@Override
	protected void workOnHistorical(AssessmentData data) {
		//Nothing to do.
	}

	@Override
	protected void workOnMostRecent(AssessmentData data) {
		//Nothing to do.
	}
}
