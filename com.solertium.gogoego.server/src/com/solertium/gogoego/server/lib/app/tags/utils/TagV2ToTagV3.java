/**
 *
 */
package com.solertium.gogoego.server.lib.app.tags.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.gogoego.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.query.InsertQuery;
import com.solertium.gogoego.server.lib.app.tags.container.TagApplication;
import com.solertium.gogoego.server.lib.app.tags.resources.BaseTagResource;
import com.solertium.util.NodeCollection;
import com.solertium.util.TrivialExceptionHandler;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;

/**
 * TagV2ToTagV3.java
 * 
 * First, a canonical list of tags needs to be established from all available
 * view documents and assigned unique IDs. Concurrently, a canonical list of tag
 * groups needs to be established from the very same view document and assigned
 * unique IDs. Finally, a one to many mapping needs to be created between tag
 * groups and their tags.
 * 
 * Second, tagged resources can be assessed. The .tags.xml files in each
 * collection folder should be perused and mapped to the previously located
 * tags, and stored with their URIs. The IDs should be used here for later
 * database insertion.
 * 
 * Third, database insertions may occur. This will include the tags themselves,
 * tag groups, the tag-to-tag-group relation, the group views, resource uris,
 * and resource tags.
 * 
 * All done!
 * 
 * @author carl.scott
 * 
 */
public abstract class TagV2ToTagV3 {

	protected final String TABLE_TAGS = "tags";
	protected final String TABLE_DEFAULTTAGS = "defaulttags";
	protected final String TABLE_GROUPS = "groups";
	protected final String TABLE_GROUPTAGS = "grouptags";
	protected final String TABLE_GROUPDIRECTORIES = "groupdirectories";
	protected final String TABLE_DIRECTORYRULES = "directoryrules";
	protected final String TABLE_GROUPVIEWS = "groupviews";
	protected final String TABLE_RESOURCEURIS = "resourceuris";
	protected final String TABLE_RESOURCETAGS = "resourcetags";

	private final HashMap<String, Integer> idCount = new HashMap<String, Integer>();

	private final TreeSet<Tag> allTags = new TreeSet<Tag>() {
		private static final long serialVersionUID = 1L;

		public boolean add(Tag e) {
			boolean isAdded = super.add(e);
			if (isAdded)
				e.id = getID(TABLE_TAGS);
			return isAdded;
		}
	};
	private final TreeSet<TagGroup> allTagGroups = new TreeSet<TagGroup>() {
		private static final long serialVersionUID = 1L;

		public boolean add(TagGroup e) {
			boolean isAdded = super.add(e);
			if (isAdded)
				e.id = getID(TABLE_GROUPS);
			return isAdded;
		}
	};

	private final TreeSet<GroupView> allGroupViews = new TreeSet<GroupView>() {
		private static final long serialVersionUID = 1L;

		public boolean add(GroupView e) {
			boolean isAdded = super.add(e);
			if (isAdded)
				e.rowID = getID(TABLE_GROUPVIEWS);
			return isAdded;
		}
	};

	private final ArrayList<TaggedResource> allTaggedResources = new ArrayList<TaggedResource>() {
		private static final long serialVersionUID = 1L;

		public boolean add(TaggedResource e) {
			e.id = getID(TABLE_RESOURCEURIS);
			return super.add(e);
		}
	};

	private final HashMap<String, ArrayList<Integer>> defaultTags = new HashMap<String, ArrayList<Integer>>();

	// Given a tag list, only add it if it came from the mapped view ID
	protected final HashMap<String, String> tagListToViewID = new HashMap<String, String>();

	private final String siteID;

	private final boolean TEST_MODE;

	public TagV2ToTagV3(String siteID, VFS vfs, ExecutionContext ec, boolean test) throws DBException {
		this.siteID = siteID;
		this.TEST_MODE = test;

		initViewToTagList();
		initIDCount();

		System.out.println("------------ Doing step 1 -----------");
		doStep1(vfs);
		System.out.println("------------ Doing step 2 -----------");
		doStep3(vfs);
		System.out.println("------------ Doing step 3 (test) -----------");
		doStep4_Test();
		System.out.println("------------ Doing step 3 -----------");
		initTables(ec);
		doStep4(ec);
	}

	public void initIDCount() {
		idCount.put(TABLE_TAGS, Integer.valueOf(1));
		idCount.put(TABLE_DEFAULTTAGS, Integer.valueOf(1));
		idCount.put(TABLE_GROUPS, Integer.valueOf(1));
		idCount.put(TABLE_GROUPTAGS, Integer.valueOf(1));
		idCount.put(TABLE_GROUPDIRECTORIES, Integer.valueOf(1));
		idCount.put(TABLE_DIRECTORYRULES, Integer.valueOf(1));
		idCount.put(TABLE_GROUPVIEWS, Integer.valueOf(1));
		idCount.put(TABLE_RESOURCEURIS, Integer.valueOf(1));
		idCount.put(TABLE_RESOURCETAGS, Integer.valueOf(1));
	}

	private void initTables(ExecutionContext ec) throws DBException {
		final Document struct = TagApplication.getStructureDocument(siteID);
		ec.setStructure(struct);

		if (!TEST_MODE) {
			final NodeCollection nodes = new NodeCollection(struct.getDocumentElement().getChildNodes());

			for (Node node : nodes) {
				if (node.getNodeName().equals("table")) {
					String name = DocumentUtils.impl.getAttribute(node, "name");
					if (!"".equals(name))
						try {
							ec.doUpdate("DROP TABLE " + name);
							System.out.println("Dropped " + name);
						} catch (DBException e) {
							System.out.println("Failed to drop " + name + ": " + e.getMessage());
							TrivialExceptionHandler.ignore(this, e);
						}
				}
			}

			ec.createStructure(struct);
		}
	}

	public abstract void initViewToTagList();

	/**
	 * First, a canonical list of tags needs to be established from all
	 * available view documents and assigned unique IDs. Concurrently, a
	 * canonical list of tag groups needs to be established from the very same
	 * view document and assigned unique IDs. Finally, a one to many mapping
	 * needs to be created between tag groups and their tags.
	 * 
	 * @param vfs
	 */
	public void doStep1(VFS vfs) {
		final VFSPath viewsDirectory = new VFSPath("/(SYSTEM)/views");
		final VFSPathToken[] tokens;
		try {
			tokens = vfs.list(viewsDirectory);
		} catch (IOException e) {
			return;
		}

		// First, let's find the views that are explicitly called for...
		final Iterator<String> initerator = tagListToViewID.keySet().iterator();
		while (initerator.hasNext()) {
			String tagListName = initerator.next();
			String viewID = tagListToViewID.get(tagListName);

			final Document document;
			try {
				document = vfs.getDocument(viewsDirectory.child(new VFSPathToken(viewID + ".xml")));
			} catch (IOException e) {
				continue;
			}

			final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

			for (Node node : nodes) {
				if (node.getNodeName().equals("tagList")
						&& DocumentUtils.impl.getAttribute(node, "name").equals(tagListName)) {
					TagGroup group = new TagGroup();
					group.name = tagListName;
					group.viewIDs.add(viewID);

					final NodeCollection tagNodes = new NodeCollection(node.getChildNodes());

					for (Node tagNode : tagNodes) {
						if (tagNode.getNodeName().equals("tag")) {
							Tag tag = new Tag();
							tag.name = tagNode.getTextContent();
							System.out.print("Adding tag " + tag.name);
							allTags.add(tag);

							if (tag.id == null)
								tag.id = findTagID(tag.name);

							group.tags.add(tag);
							System.out.println("; id = " + tag.id);
						}
					}

					allTagGroups.add(group);

					System.out.println("## Set up named group " + group.name + " from view " + viewID + " (" + group.id
							+ ")");
				}
			}
		}

		/*
		 * Now, go through all the files and see which ones use which group.
		 * Alert if one is not utilized. If found, use that ID!
		 */

		for (VFSPathToken token : tokens) {
			final VFSPath current = viewsDirectory.child(token);
			final Document document;
			try {
				document = vfs.getDocument(current);
			} catch (IOException e) {
				continue;
			}

			final String viewID = token.toString().substring(0, token.toString().lastIndexOf('.'));

			final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());

			for (Node node : nodes) {
				if (node.getNodeName().equals("tagList")) {
					final String name = DocumentUtils.impl.getAttribute(node, "name");

					Integer groupID = findTagGroupID(name);

					if (groupID == null) {
						System.out.println(viewID + " hold unidentified tag list " + name);
					} else
						System.out.println(viewID + " uses tag list " + name + " with id " + groupID + "...");

					GroupView view = new GroupView();
					view.groupID = groupID;
					view.viewID = viewID;

					allGroupViews.add(view);
				}
			}
		}
	}

	/**
	 * Third, tagged resources can be assessed. The .tags.xml files in each
	 * collection folder should be perused and mapped to the previously located
	 * tags, and stored with their URIs. The IDs should be used here for later
	 * database insertion.
	 * 
	 */
	public void doStep3(VFS vfs) {
		doStep3_peruse(new VFSPath("/(SYSTEM)/collections"), vfs);
	}

	private String clean(String in) {
		String out = in.replaceFirst("/\\(SYSTEM\\)", "");
		if (out.endsWith(".xml"))
			out = out.substring(0, out.lastIndexOf('.'));
		return out;
		// return in;
	}

	private VFSPath clean(VFSPath in) {
		return new VFSPath(clean(in.toString()));
	}

	private void doStep3_peruse(VFSPath path, VFS vfs) {
		final VFSPath file = path.child(new VFSPathToken(".tags.xml"));

		Document document = null;
		try {
			document = vfs.getDocument(file);
		} catch (IOException e) {
			document = null;
		}

		if (document != null) {
			final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
			final ArrayList<Integer> localDefaultTags = new ArrayList<Integer>();
			for (Node node : nodes)
				if (node.getNodeName().equals("default"))
					localDefaultTags.add(findTagID(node.getTextContent()));

			for (Node node : nodes) {
				if (node.getNodeName().equals("file")) {
					final VFSPathToken fileName = new VFSPathToken(DocumentUtils.impl.getAttribute(node, "name"));

					final TaggedResource resource = new TaggedResource();
					resource.uri = clean(path.child(fileName));
					resource.tagIDs.addAll(localDefaultTags);

					final NodeCollection children = new NodeCollection(node.getChildNodes());
					for (Node curChild : children)
						if (curChild.getNodeName().equals("tag"))
							resource.tagIDs.add(findTagID(curChild.getTextContent()));

					allTaggedResources.add(resource);
				}
			}

			if (!localDefaultTags.isEmpty())
				defaultTags.put(clean(path.toString()), localDefaultTags);

		}

		final VFSPathToken[] tokens;
		try {
			tokens = vfs.list(path);
		} catch (Exception impossible) {
			TrivialExceptionHandler.impossible(this, impossible);
			return;
		}

		for (VFSPathToken token : tokens) {
			final VFSPath uri = path.child(token);
			boolean isCollection = false;
			try {
				isCollection = vfs.isCollection(uri);
			} catch (IOException impossible) {
				TrivialExceptionHandler.impossible(this, impossible);
			}
			if (isCollection)
				doStep3_peruse(uri, vfs);
		}
	}

	/**
	 * Fourth, database insertions may occur. This will include the tags
	 * themselves, tag groups, the tag-to-tag-group relation, the group views,
	 * resource uris, and resource tags.
	 * 
	 */
	public void doStep4(final ExecutionContext ec) {
		// Add all tags to tag table...
		{
			final String table = BaseTagResource.convertTableBySite(TABLE_TAGS, siteID);
			for (Tag tag : allTags) {
				final Row row;
				try {
					row = ec.getRow(table);
					row.get("id").setObject(tag.id);
					row.get("name").setObject(tag.name);
				} catch (DBException e) {
					continue;
				}

				insert(table, row, ec);
			}
		}
		// Add tag groups
		{
			final String table = BaseTagResource.convertTableBySite(TABLE_GROUPS, siteID);
			for (TagGroup group : allTagGroups) {
				final Row row;
				try {
					row = ec.getRow(table);
					row.get("id").setObject(group.id);
					row.get("name").setObject(group.name);
				} catch (DBException e) {
					continue;
				}

				insert(table, row, ec);
			}
		}
		// Add tags to groups
		{
			final String table = BaseTagResource.convertTableBySite(TABLE_GROUPTAGS, siteID);
			for (TagGroup group : allTagGroups) {
				for (Tag tag : group.tags) {
					final Row row;
					try {
						row = ec.getRow(table);
						row.get("id").setObject(getID(TABLE_GROUPTAGS));
						row.get("tagid").setObject(tag.id);
						row.get("groupid").setObject(group.id);
					} catch (Exception e) {
						continue;
					}

					insert(table, row, ec);
				}
			}
		}
		// Set up view ID to group ID relationships
		{
			final String table = BaseTagResource.convertTableBySite(TABLE_GROUPVIEWS, siteID);
			for (GroupView groupView : allGroupViews) {
				final Row row;
				try {
					row = ec.getRow(table);
					row.get("id").setObject(groupView.rowID);
					row.get("viewid").setObject(groupView.viewID);
					row.get("groupid").setObject(groupView.groupID);
				} catch (DBException e) {
					continue;
				}

				insert(table, row, ec);
			}
		}
		// Enter all resources that have tags
		{
			final String table = BaseTagResource.convertTableBySite(TABLE_RESOURCEURIS, siteID);
			final Long today = Long.valueOf(Calendar.getInstance().getTimeInMillis());
			for (TaggedResource resource : allTaggedResources) {
				final Row row;
				try {
					row = ec.getRow(table);
					row.get("id").setObject(resource.id);
					row.get("uri").setObject(resource.uri.toString());
					row.get("datatype").setObject("resource");
					row.get("lasttagged").setObject(today.toString());
				} catch (DBException e) {
					continue;
				}

				insert(table, row, ec);
			}
		}
		// Enter tags for a given resource
		{
			final String table = BaseTagResource.convertTableBySite(TABLE_RESOURCETAGS, siteID);
			for (TaggedResource resource : allTaggedResources) {
				for (Integer tagID : resource.tagIDs) {
					final Row row;
					try {
						row = ec.getRow(table);
						row.get("id").setObject(getID(TABLE_RESOURCETAGS));
						row.get("tagid").setObject(tagID);
						row.get("uriid").setObject(resource.id);
					} catch (DBException e) {
						continue;
					}

					insert(table, row, ec);
				}
			}
		}
		// Add all default tags
		{
			final String table = BaseTagResource.convertTableBySite(BaseTagResource.TABLE_DEFAULTTAGS, siteID);
			final Iterator<Map.Entry<String, ArrayList<Integer>>> iterator = defaultTags.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, ArrayList<Integer>> current = iterator.next();
				for (Integer tagID : current.getValue()) {
					final Row row;
					try {
						row = ec.getRow(table);
						row.get("id").setObject(getID(TABLE_DEFAULTTAGS));
						row.get("uri").setObject(current.getKey());
						row.get("tagid").setObject(tagID);
					} catch (DBException e) {
						System.out.println("Failed to write for " + current);
						continue;
					}

					insert(table, row, ec);
				}
			}
		}
	}

	private void insert(String table, Row row, ExecutionContext ec) {
		final InsertQuery query = new InsertQuery();
		query.setTable(table);
		query.setRow(row);

		try {
			System.out.println("Executing: " + query.getSQL(ec.getDBSession()));
			if (TEST_MODE)
				System.out.println(" - Testing, will not insert");
			else
				ec.doUpdate(query);
		} catch (DBException e) {
			System.out.println("# Failed: " + query.getSQL(ec.getDBSession()));
		}
	}

	public void doStep4_Test() {
		System.out.println("-------- All tags found (" + allTags.size() + ")-----------");
		{
			final Iterator<Tag> iterator = allTags.iterator();
			while (iterator.hasNext())
				System.out.println(iterator.next());
		}

		System.out.println("-----------------------------------");

		System.out.println("-------- All groups found (" + allTagGroups.size() + ")-----------");
		{
			final Iterator<TagGroup> iterator = allTagGroups.iterator();
			while (iterator.hasNext())
				System.out.println(iterator.next());
		}

		System.out.println("-----------------------------------");

		System.out.println("-------- All group views found (" + allGroupViews.size() + ")-----------");
		{
			final Iterator<GroupView> iterator = allGroupViews.iterator();
			while (iterator.hasNext())
				System.out.println(iterator.next());
		}

		System.out.println("-----------------------------------");

		System.out.println("-------- All default tags found -----------");
		{
			final Iterator<String> iterator = defaultTags.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				System.out.println(key + " -> " + defaultTags.get(key));
			}
		}

		System.out.println("-----------------------------------");

		System.out.println("-------- All tagged resources found (" + allTaggedResources.size() + ")-----------");
		{
			final Iterator<TaggedResource> iterator = allTaggedResources.iterator();
			while (iterator.hasNext())
				System.out.println(iterator.next());
		}
	}

	private Integer getID(String table) {
		Integer cur = idCount.get(table);
		Integer next = Integer.valueOf(cur.intValue() + 1);
		idCount.put(table, next);
		return cur;
	}

	private Integer findTagID(String tagName) {
		final Iterator<Tag> wasteful = allTags.iterator();
		while (wasteful.hasNext()) {
			Tag t = wasteful.next();
			if (t.name.equals(tagName))
				return t.id;
		}
		System.out.println("## No entry found for " + tagName);
		Tag newTag = new Tag();
		newTag.name = tagName;
		allTags.add(newTag);
		return newTag.id;
	}

	private Integer findTagGroupID(String groupName) {
		final Iterator<TagGroup> wasteful = allTagGroups.iterator();
		while (wasteful.hasNext()) {
			TagGroup t = wasteful.next();
			if (t.name.equals(groupName))
				return t.id;
		}
		return null;
	}

	private class TagGroup implements Comparable<TagGroup> {
		private String name;
		private Integer id;
		private ArrayList<String> viewIDs = new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			public boolean add(String e) {
				return !contains(e) && super.add(e);
			}
		};
		private ArrayList<Tag> tags = new ArrayList<Tag>() {
			private static final long serialVersionUID = 1L;

			public boolean add(Tag e) {
				return !contains(e) && super.add(e);
			}
		};

		public String toString() {
			return id + ": " + name + " contains views: " + viewIDs + " and " + tags.size() + " tags";
		}

		public int compareTo(TagGroup o) {
			return name.compareTo(o.name);
		}
	}

	private class Tag implements Comparable<Tag> {
		private String name;
		private Integer id;

		public String toString() {
			return id + ": " + name;
		}

		public int compareTo(Tag o) {
			return name.compareTo(o.name);
		}
	}

	private class GroupView implements Comparable<GroupView> {
		private String viewID;
		private Integer groupID;
		private Integer rowID;

		public String toString() {
			return rowID + ": " + viewID + " for group " + groupID;
		}

		public int compareTo(GroupView o) {
			return (groupID.equals(o.groupID)) ? viewID.compareTo(o.viewID) : groupID.compareTo(o.groupID);
		}
	}

	private class TaggedResource {
		private Integer id;
		private VFSPath uri;
		private ArrayList<Integer> tagIDs = new ArrayList<Integer>() {
			private static final long serialVersionUID = 1L;

			public boolean add(Integer e) {
				return !contains(e) && super.add(e);
			}
		};

		public String toString() {
			return id + ": " + uri + " contains tags: \r\n" + tagIDs;
		}
	}

}
