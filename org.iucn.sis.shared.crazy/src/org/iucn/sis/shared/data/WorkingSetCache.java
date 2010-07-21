package org.iucn.sis.shared.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.iucn.sis.client.acl.AuthorizationCache;
import org.iucn.sis.client.components.ClientUIContainer;
import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.acl.base.AuthorizableObject;
import org.iucn.sis.shared.data.assessments.AssessmentFilter;
import org.iucn.sis.shared.xml.XMLUtils;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.portable.PortableAlphanumericComparator;

/**
 * Class that represents the working set cache. Main member is the static
 * representation of the class, to be used to represent the current working set.
 * 
 * @author liz.schwartz
 * 
 */
public class WorkingSetCache {

	public static class WorkingSetComparator extends PortableAlphanumericComparator {
		public WorkingSetComparator() {
			super();
		}

		@Override
		public int compare(Object ol, Object or) {
			WorkingSetData ws1 = (WorkingSetData) ol;
			WorkingSetData ws2 = (WorkingSetData) or;

			return super.compare(ws1.getWorkingSetName(), ws2.getWorkingSetName());
		}
	}

	public static final WorkingSetCache impl = new WorkingSetCache();
	/**
	 * A hashmap of working sets <String id, WorkingSetData>
	 */
	private HashMap<String, WorkingSetData> workingSets;

	private WorkingSetData currentWorkingSet;

	/**
	 * an arraylist of working set data that is possible to subscribe to.
	 */
	private ArrayList<WorkingSetData> subscribableWorkingSets;

	private WorkingSetCache() {
		workingSets = new HashMap<String, WorkingSetData>();
		currentWorkingSet = null;
		subscribableWorkingSets = new ArrayList<WorkingSetData>();
	}

	/**
	 * adds the given working set data object to the users private working set.
	 * Assumes that the id of the working set is either null or "", will send to
	 * edit if the id matches another working set, and fails otherwise.
	 * 
	 * @param ws
	 * @param wayBack
	 */
	public void addToPrivateWorkingSets(final WorkingSetData ws, final GenericCallback<String> wayBack) {

		if (ws.getId() == null || ws.getId().trim().equalsIgnoreCase("")) {

			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.putAsText("/workingSet/private/" + SimpleSISClient.currentUser.username, ws.toXML(),
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							wayBack.onFailure(caught);
						}

						public void onSuccess(String arg0) {
							ws.setID(ndoc.getText());
							workingSets.put(ws.getId(), ws);
							wayBack.onSuccess(arg0);
						}
					});
		} else if (workingSets.containsKey(ws.getId())) {
			editPrivateWorkingSet(ws, wayBack);
		} else
			wayBack.onFailure(new Throwable("Don't know what to do because user dosen't have rights to specify"
					+ " the working set id."));
	}

	/**
	 * adds the given working set data object to the public working set pool.
	 * Assumes that the id of the working set is either null or "", will send to
	 * edit if the id matches another working set, and fails otherwise.
	 * 
	 * @param ws
	 * @param wayBack
	 */
	public void addToPublicWorkingSets(final WorkingSetData ws, final GenericCallback<String> wayBack) {

		if (ws.getId() == null || ws.getId().trim().equalsIgnoreCase("")) {

			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.putAsText("/workingSet/public/" + SimpleSISClient.currentUser.getUsername(), ws.toXML(),
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							wayBack.onFailure(caught);
						}

						public void onSuccess(String arg0) {
							ws.setID(ndoc.getText());
							workingSets.put(ws.getId(), ws);
							wayBack.onSuccess(arg0);
						}
					});
		} else if (workingSets.containsKey(ws.getId())) {
			editPrivateWorkingSet(ws, wayBack);
		} else
			wayBack.onFailure(new Throwable("Don't know what to do because user dosen't have rights to specify"
					+ " the working set id."));
	}

	private void addToWorkingSetCache(ArrayList<WorkingSetData> ws) {
		for (WorkingSetData curWS : ws)
			addToWorkingSetCache(curWS);
	}

	private void addToWorkingSetCache(WorkingSetData ws) {
		workingSets.put(ws.getId(), ws);
	}

	public void createTaxaList(String id, final GenericCallback<String> wayback) {
		if (getWorkingSet(id) != null) {
			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.getAsText("/workingSet/taxaList/" + SimpleSISClient.currentUser.username + "/" + id,
					new GenericCallback<String>() {

						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							wayback.onFailure(caught);
						}

						public void onSuccess(String arg0) {
							// NDOC.TEXT SHOULD CONTAIN URL TO LET THE USER
							// DOWNLOAD THE FILE
							wayback.onSuccess(ndoc.getText());
						}

					});
		} else
			wayback.onFailure(new Throwable("Not a valid working set"));
	}
	
	public void createTaxaListWithCats(String id, AssessmentFilter filter, final GenericCallback<String> wayback) {
		if (getWorkingSet(id) != null) {
			final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.postAsText("/workingSet/taxaList/" + SimpleSISClient.currentUser.username + "/" + id,
					"<root>" + filter.toXML() + "</root>",	new GenericCallback<String>() {

						public void onFailure(Throwable caught) {
							// TODO Auto-generated method stub
							wayback.onFailure(caught);
						}

						public void onSuccess(String arg0) {
							// NDOC.TEXT SHOULD CONTAIN URL TO LET THE USER
							// DOWNLOAD THE FILE
							wayback.onSuccess(ndoc.getText());
						}

					});
		} else
			wayback.onFailure(new Throwable("Not a valid working set"));
	}

	public void deletePrivateWorkingSet(final WorkingSetData ws, final GenericCallback<String> wayBack) {

		NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.delete("/workingSet/private/" + SimpleSISClient.currentUser.getUsername() + "/" + ws.getId(),
				new GenericCallback<String>() {
					public void onFailure(Throwable caught) {
						wayBack.onFailure(caught);
					}

					public void onSuccess(String arg0) {
						workingSets.remove(ws.getId());
						wayBack.onSuccess("YAY");
					}
				});
	}

//	public void deletePublicWorkingSet(final WorkingSetData ws, final GenericCallback<String> wayBack) {
//
//		NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
//		ndoc.delete("/workingSet/public/" + SimpleSISClient.currentUser.getUsername() + "/" + ws.getId(),
//				new GenericCallback<String>() {
//					public void onFailure(Throwable caught) {
//						wayBack.onFailure(caught);
//					}
//
//					public void onSuccess(String arg0) {
//						workingSets.remove(ws.getId());
//						wayBack.onSuccess("YAY");
//					}
//				});
//	}

	public void unsubscribeToWorkingSet(final WorkingSetData ws, final GenericCallback<String> wayBack) {
		if (ws.getMode().equalsIgnoreCase(WorkingSetData.PRIVATE)) {
			wayBack.onFailure(null);
		} else {
			NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.delete("/workingSet/unsubscribe/" + SimpleSISClient.currentUser.getUsername() + "/" + ws.getId(),
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							wayBack.onFailure(caught);
						}

						public void onSuccess(String arg0) {
							workingSets.remove(ws.getId());
							wayBack.onSuccess("YAY");
						}
					});
		}
			
	}
	
	public void deletePublicWorkingSet(final WorkingSetData ws, final GenericCallback<String> wayBack) {
		if (ws.getMode().equalsIgnoreCase(WorkingSetData.PRIVATE)) {
			deletePrivateWorkingSet(ws, wayBack);
		} else {
			NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.delete("/workingSet/public/" + SimpleSISClient.currentUser.getUsername() + "/" + ws.getId(),
					new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					wayBack.onFailure(caught);
				}

				public void onSuccess(String arg0) {
					workingSets.remove(ws.getId());
					wayBack.onSuccess("YAY");
				}
			});
		}
	}

	public void doLogout() {
		workingSets.clear();
		currentWorkingSet = null;
	}

	private void editPrivateWorkingSet(final WorkingSetData ws, final GenericCallback<String> wayBack) {

		if (workingSets.containsKey(ws.getId())) {
			NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();

			ndoc.post("/workingSet/private/" + SimpleSISClient.currentUser.getUsername() + "/", ws.toXML(),
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							wayBack.onFailure(caught);
						}

						public void onSuccess(String arg0) {
							workingSets.put(ws.getId(), ws);
							wayBack.onSuccess(arg0);
						}
					});

		} else {
			wayBack.onFailure(new Throwable("Not currently in the workingSet"));
		}
	}

	private void editPublicWorkingSet(final WorkingSetData ws, final GenericCallback<String> wayBack) {

		if (workingSets.containsKey(ws.getId())) {
			NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
			ndoc.post("/workingSet/public/" + SimpleSISClient.currentUser.getUsername() + "/", ws.toXML(),
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							wayBack.onFailure(caught);
						}

						public void onSuccess(String arg0) {
							workingSets.put(ws.getId(), ws);
							wayBack.onSuccess(arg0);
						}
					});

		} else {
			wayBack.onFailure(new Throwable("Not currently in the workingSet"));
		}
	}

	public void editWorkingSet(final WorkingSetData ws, final GenericCallback<String> wayback) {

		String id = ws.getId();
		WorkingSetData otherWorkingSet = workingSets.get(id);

		if (otherWorkingSet.getMode().equalsIgnoreCase(ws.getMode())) {
			if (ws.getMode().equalsIgnoreCase(WorkingSetData.PRIVATE))
				editPrivateWorkingSet(ws, wayback);
			else
				editPublicWorkingSet(ws, wayback);
		} else {
			// CHANGING TO PRIVATE ... CREATING NEW COPY
			if (ws.getMode().equalsIgnoreCase(WorkingSetData.PRIVATE)) {

			}
			// CHANGING TO PUBLIC
			else {

			}
		}

	}

	

	public void exportWorkingSet(final String workingSetID, final boolean lock, final GenericCallback<String> wayBack) {
		if (workingSets.containsKey(workingSetID)) {
			WorkingSetData ws = workingSets.get(workingSetID);
			String mode = "public";
			if (ws.getMode().equalsIgnoreCase(WorkingSetData.PRIVATE))
				mode = "private";
			
			if (workingSets.containsKey(workingSetID)) {
				final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
				ndoc.getAsText("/workingSetExporter/" + mode + "/" + SimpleSISClient.currentUser.getUsername() + "/"
						+ workingSetID + "?lock=" + lock, new GenericCallback<String>() {

					public void onFailure(Throwable caught) {
						WindowUtils.errorAlert("Server error while exporting working set." + "<br>Error as follows: "
								+ ndoc.getText());
						wayBack.onFailure(caught);
					}

					public void onSuccess(String arg0) {
						String body = ndoc.getText();
						int firstLineBreak = body.indexOf("\r\n", 0);
						final String path = body.substring(0, firstLineBreak);
						
						if( lock && body.contains("<div>") ) {
							final Window w = WindowUtils.getWindow(true, false, "Assessments Locked");
							w.getButtonBar().add(new Button("Close", new SelectionListener<ButtonEvent>() {
								public void componentSelected(ButtonEvent ce) {
									wayBack.onSuccess(path);
									w.close();
								};
							}));
							w.add(new Html(body.substring(firstLineBreak)));
							w.show();
							w.setSize(500, 400);
							w.center();
						} else
							wayBack.onSuccess(path);
					}
				});
			} else {
				wayBack.onFailure(new Throwable("Not a valid working set."));
			}

		} else
			wayBack.onFailure(new Throwable("oh no, how did you get here, "
					+ "the working set is not in your working sets"));
	}

	public void fetchTaxaForWorkingSet(final WorkingSetData ws, final GenericCallback<String> wayback) {
		TaxonomyCache.impl.fetchList(ws.getSpeciesIDsAsString(), new GenericCallback<String>() {
			public void onFailure(Throwable caught) {
				wayback.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				List<String> toRemove = new ArrayList<String>();
				for( String cur : ws.getSpeciesIDs() ) {
					if( !TaxonomyCache.impl.containsNodeByID(cur) )
						toRemove.add(cur);
				}
				
				if( toRemove.size() > 0 )
					ws.removeSpeciesIDs(toRemove);
				
				wayback.onSuccess(arg0);
			}
		});
	}
	
	
	/**
	 * gets all public working sets that the user can subscribe to
	 * 
	 * @param wayBack
	 */
	public void getAllSubscribableWorkingSets(final GenericCallback<String> wayBack) {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.get("/workingSet/subscribe/" + SimpleSISClient.currentUser.getUsername(), new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				subscribableWorkingSets.clear();
				wayBack.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				subscribableWorkingSets.clear();
				NativeNodeList list = ndoc.getDocumentElement().getElementsByTagName("workingSet");
				for (int i = 0; i < list.getLength(); i++) {
					NativeElement element = (NativeElement) list.item(i);
					WorkingSetData ws = new WorkingSetData();
					ws.setCreator(element.getAttribute("creator"));
					ws.setID(element.getAttribute("id"));
					ws.setDate(element.getAttribute("date"));
					ws.setWorkingSetName(XMLUtils.cleanFromXML(element.getAttribute("name")));
					
					if( AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, ws))
						subscribableWorkingSets.add(ws);
				}
				wayBack.onSuccess(arg0);
			}

		});
	}

	public WorkingSetData getCurrentWorkingSet() {
		return currentWorkingSet;
	}

	public int getNumberOfWorkingSets() {
		return workingSets.size();
	}

	public ArrayList<WorkingSetData> getSubscribable() {
		return subscribableWorkingSets;
	}

	/**
	 * given the working set id, returns the working set associated with it,
	 * null if it doesn't exist
	 * 
	 * @param id
	 */
	public WorkingSetData getWorkingSet(String id) {
		return workingSets.get(id);
	}

	public HashMap<String, WorkingSetData> getWorkingSets() {
		return workingSets;
	}

	public void resetCurrentWorkingSet() {
		setCurrentWorkingSet(null);
	}

	public void setCurrentWorkingSet(String id) {
		currentWorkingSet = workingSets.get(id);
		ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.updateWorkflowStatus();
		ClientUIContainer.headerContainer.workingSetChanged();
	}

	public void setCurrentWorkingSetData(WorkingSetData ws) {
		currentWorkingSet = ws;
		ClientUIContainer.bodyContainer.tabManager.panelManager.DEM.updateWorkflowStatus();
		ClientUIContainer.headerContainer.workingSetChanged();
	}

	/**
	 * given the workingSetID, adds the id to your list of working sets that you
	 * are subscribed to
	 * 
	 * @param workingSetID
	 * @param wayBack
	 */
	public void subscribeToPublicWorkingSet(final String workingSetID, final GenericCallback<String> wayBack) {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.put("/workingSet/subscribe/" + SimpleSISClient.currentUser.getUsername() + "/" + workingSetID, "",
				new GenericCallback<String>() {

					public void onFailure(Throwable caught) {
						wayBack.onFailure(caught);
					}

					public void onSuccess(String arg0) {
						workingSets.clear();
						resetCurrentWorkingSet();
						update(wayBack);
//						NativeElement docElement = ndoc.getDocumentElement();
//						WorkingSetParser parser = new WorkingSetParser();
//						parser.parseSingleWorkingSet(docElement);
//						addToWorkingSetCache(parser.getWorkingSets());
//						String ids = parser.getAllSpeciesIDsAsCSV();
//						if (ids.length() > 0) {
//							TaxonomyCache.impl.fetchList(ids, new GenericCallback<String>() {
//
//								public void onFailure(Throwable caught) {
//									wayBack.onFailure(caught);
//								}
//
//								public void onSuccess(String arg0) {
//									wayBack.onSuccess(arg0);
//								}
//
//							});
//						} else
//							wayBack.onSuccess(arg0);

					}

				});
	}

	/**
	 * given the workingSetIDs in csv form, removes the ids to your list of
	 * working sets that you are subscribed to
	 * 
	 * @param workingSetID
	 * @param wayBack
	 */
	public void unsubscribeToPublicWorkingSets(final String workingSetID, final GenericCallback<String> wayBack) {

	}

	public void update(final GenericCallback<String> backInTheDay) {
		final NativeDocument ndoc = SimpleSISClient.getHttpBasicNativeDocument();
		ndoc.get("/workingSet/" + SimpleSISClient.currentUser.getUsername(), new GenericCallback<String>() {

			public void onFailure(Throwable caught) {
				backInTheDay.onFailure(caught);
			}

			public void onSuccess(String arg0) {
				try {
				WorkingSetParser parser = new WorkingSetParser();
				parser.parseWorkingSetXML(ndoc);
				addToWorkingSetCache(parser.getWorkingSets());
				backInTheDay.onSuccess(arg0);
				// parseWorkingSetXML(ndoc, backInTheDay);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
