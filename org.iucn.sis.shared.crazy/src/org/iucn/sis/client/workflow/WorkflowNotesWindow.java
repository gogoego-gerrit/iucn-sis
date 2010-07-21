package org.iucn.sis.client.workflow;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.iucn.sis.client.simple.SimpleSISClient;
import org.iucn.sis.shared.data.WorkingSetCache;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import com.solertium.lwxml.shared.GWTNotFoundException;
import com.solertium.lwxml.shared.GenericCallback;
import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.lwxml.shared.utils.RowData;
import com.solertium.lwxml.shared.utils.RowParser;
import com.solertium.util.extjs.client.WindowUtils;
import com.solertium.util.gwt.ui.DrawsLazily;
import com.solertium.util.gwt.ui.StyledHTML;
import com.solertium.util.portable.XMLWritingUtils;

public class WorkflowNotesWindow extends Window {
	
	private final String workingSetID;
	private final String scope;
	
	private final LayoutContainer commentPanel;
	
	public WorkflowNotesWindow() {
		this("global");
	}
	
	public WorkflowNotesWindow(String scope) {
		this(WorkingSetCache.impl.getCurrentWorkingSet().getId(), scope);
	}

	public WorkflowNotesWindow(String workingSetID, String scope) {
		super();
		this.workingSetID = workingSetID;
		this.scope = scope;
		
		final TableLayout layout = new TableLayout(1);
		layout.setCellSpacing(10);
		
		commentPanel = new LayoutContainer();
		commentPanel.setScrollMode(Scroll.AUTO);
		commentPanel.setLayout(layout);
		
		setHeading("Comments");
		setIconStyle("icon-workflow");
		setLayout(new FillLayout());
		setSize(350, 450);
		setScrollMode(Scroll.AUTO);
// setAlignment(HorizontalAlignment.CENTER);
	}
	
	private void updateCommentPanel(final DrawsLazily.DoneDrawingCallback callback) {
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get("/workflow/set/" + workingSetID + "/notes/" + scope, new GenericCallback<String>() {
			public void onSuccess(String result) {
				commentPanel.removeAll();
				final RowParser parser = new RowParser(document);
				if (parser.getRowCount() == 0)
					commentPanel.add(new HTML("No comments"));
				else {
					final List<RowData> rows = parser.getRows();
					
					Collections.sort(rows, new NoteComparator());
					
					for (int i = rows.size()-1; i >= 0; i--) {
						final RowData current = rows.get(i);
						final String text = 
							"[" + current.getField("date") + "] " + 
							current.getField("user") + ": " + 
							current.getField("comment");
						final String styleName = 
							i % 2 == 0 ? "workflow-comment" : "workflow-comment-alternate";
						
						commentPanel.add(new StyledHTML(text, styleName));
					}
				}
				
				callback.isDrawn();
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("No notes found for this working set.");
			}
		});
	}
	
	private void draw(final DrawsLazily.DoneDrawingCallback callback) {
		final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
		document.get("/workflow/set/"+workingSetID+"/status", new GenericCallback<String>() {
			public void onSuccess(String result) {
				final RowParser parser = new RowParser(document);
				if (parser.getRowCount() == 0)
					onFailure(new GWTNotFoundException());
				else 
					completeRender(parser.getFirstRow().getField("status"), callback);
			}
			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("This assessment has not started the workflow process.");
			}
		});
	}
	
	private void completeRender(String status, DrawsLazily.DoneDrawingCallback callback) {
		final TextArea area = new TextArea();
		area.setValue(null);
		area.setAllowBlank(false);
		area.setEmptyText("Click to add a comment");
				
		final LayoutContainer addCommentForm = new LayoutContainer(new FillLayout());
		addCommentForm.add(area);
		
		int size = 100, top = 25;
		
		final HtmlContainer statusContainer = new HtmlContainer("Status: " + status);
		statusContainer.addStyleName("bold");
		statusContainer.addStyleName("sis_workflow_notes_header");
		
		final LayoutContainer container = new LayoutContainer(new BorderLayout());
		container.add(statusContainer, new BorderLayoutData(LayoutRegion.NORTH, top, top, top));
		container.add(commentPanel, new BorderLayoutData(LayoutRegion.CENTER));
		container.add(addCommentForm, new BorderLayoutData(LayoutRegion.SOUTH, size, size, size));
				
		add(container);
				
		addButton(new Button("Add Comment", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				if (area.getValue() != null && !"".equals(area.getValue())) {
					final StringBuilder bodyBuilder = new StringBuilder();
					bodyBuilder.append("<root>");
					bodyBuilder.append("<user>");
					bodyBuilder.append(XMLWritingUtils.writeCDATATag("id", SimpleSISClient.currentUser.getUsername()));
					bodyBuilder.append(XMLWritingUtils.writeCDATATag("name", SimpleSISClient.currentUser.getDisplayableName()));
					bodyBuilder.append(XMLWritingUtils.writeCDATATag("email", SimpleSISClient.currentUser.getEmail()));
					bodyBuilder.append("</user>");
					bodyBuilder.append(XMLWritingUtils.writeCDATATag("comment", area.getValue()));
					bodyBuilder.append(XMLWritingUtils.writeCDATATag("scope", scope));
					bodyBuilder.append("</root>");
							
					final NativeDocument document = SimpleSISClient.getHttpBasicNativeDocument();
					document.post("/workflow/set/" + workingSetID + "/status", bodyBuilder.toString(), new GenericCallback<String>() {
						public void onSuccess(String result) {
							Info.display("Success", "Comment added");
							updateCommentPanel(new DrawsLazily.DoneDrawingCallback() {
								public void isDrawn() {
									commentPanel.layout();
								}
							});
						}
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Could not add comment, please try again later.");
						}
					});
				}
			}
		}));
		addButton(new Button("Close", new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				close();
			}
		}));
				
		updateCommentPanel(callback);
	}
	
	public void show() {
		draw(new DrawsLazily.DoneDrawingCallback() {
			public void isDrawn() {
				deferred_show();
			}
		});
	}
	
	private void deferred_show() {
		super.show();
	}
	
	public static class NoteComparator implements Comparator<RowData> {
		
		private final DateTimeFormat format =
			DateTimeFormat.getFormat("yyyy-MM-dd hh:mm:ss");
		
		public int compare(RowData o1, RowData o2) {
			try {
				final Date date1 = format.parse(o1.getField("date"));
				final Date date2 = format.parse(o2.getField("date"));
				return date1.compareTo(date2);
			} catch (Exception impossible) {
				return -1;
			}
		}
		
	}

}
