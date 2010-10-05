package org.iucn.sis.client.panels.taxa;

import com.google.gwt.event.dom.client.ClickEvent;

public class TaxonHomePage extends LayoutContainer {

	class DescriptionPanel extends LayoutContainer {

		DockPanel wrapper;

		private DescriptionPanel() {
			addStyleName("padded");
		}

		private ContentPanel getAssessmentInformationPanel(final Taxon node) {
			final ContentPanel assessmentInformation = new ContentPanel();
			assessmentInformation.setStyleName("x-panel");
			assessmentInformation.setWidth(350);
			assessmentInformation.setHeight(200);
			assessmentInformation.setHeading("Most Recent Published Status");
			assessmentInformation.setLayoutOnChange(true);

			if (node.getAssessments().size() > 0) {
				VerticalPanel assessPanel = new VerticalPanel();
				Assessment curAssessment = null;
				Set<Assessment> assess = TaxonomyCache.impl.getTaxon(node.getId()).getAssessments();
				for (Assessment cur : assess) {
					if (!cur.getIsHistorical()) {
						curAssessment = cur;
						break;
					}
				}

				VerticalPanel assessInfoPanel = new VerticalPanel();

				// TEST THIS
				if (curAssessment == null) {
					assessInfoPanel.add(new HTML("This taxon does not have a non-historical assessment."));
				} else {
					assessInfoPanel.setStyleName("SIS_taxonSummaryHeader_assessHeader");

					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessment ID: " + curAssessment.getId()));

					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Category: "
							+ AssessmentFormatter.getProperCategoryAbbreviation(curAssessment)));
					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Criteria: " + AssessmentFormatter.getProperCriteriaString(curAssessment)));
					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessed: " + curAssessment.getDateAssessed()));
					assessInfoPanel.add(new HTML("&nbsp;&nbsp;Assessor: " + AssessmentFormatter.getDisplayableAssessors(curAssessment)));
					assessPanel.add(assessInfoPanel);

					assessPanel.add(new HTML("&nbsp;&nbsp;Major Threats: " + ""));
					assessPanel.add(new HTML("&nbsp;&nbsp;Population Trend: " + ""));
					assessPanel.add(new HTML("&nbsp;&nbsp;Major Importance Habitats: " + ""));
					assessPanel.add(new HTML("&nbsp;&nbsp;Conservation Actions Needed: " + ""));
					headerAssess.setText(
							AssessmentFormatter.getProperCategoryAbbreviation(curAssessment)
							+ curAssessment.getDateAssessed());
				}
				assessmentInformation.add(assessPanel);
			} else
				assessmentInformation.add(new HTML("There is no published data for this taxon."));
			return assessmentInformation;
		}

		private ContentPanel getAssessmentsPanel(final Taxon node) {
			final ContentPanel assessments = new ContentPanel();
			assessments.setStyleName("x-panel");

			TableColumn[] columns = new TableColumn[6];
			columns[0] = new TableColumn("Assessment Date", 150);
			columns[1] = new TableColumn("Category", 100);
			columns[2] = new TableColumn("Criteria", 100);
			columns[3] = new TableColumn("Status", 100);
			columns[4] = new TableColumn("Edit/View", 60);
			columns[4].setRenderer(new CellRenderer<Component>() {
				public String render(Component item, String property, Object value) {
					return "<img src =\"images/application_form_edit.png\" class=\"SIS_HyperlinkBehavior\"></img> "
							+ value;
				}
			});
			columns[5] = new TableColumn("Trash", 60);
			columns[5].setRenderer(new CellRenderer<Component>() {
				public String render(Component item, String property, Object value) {
					if (!((String) value).equals("n"))
						return "<img src =\"tango/places/user-trash.png\" class=\"SIS_HyperlinkBehavior\"></img> "
								+ value;

					return "";
				}
			});

			TableColumnModel cm = new TableColumnModel(columns);

			final Table tbl = new Table(cm);
			tbl.setSelectionModel(new TableSelectionModel());
			tbl.setBorders(false);
			assessments.setWidth(com.google.gwt.user.client.Window.getClientWidth() - 500);
			assessments.setHeight(panelHeight);
			tbl.setHeight(panelHeight - 25);
			tbl.setWidth("100%");

			assessments.setHeading("Assessment List");
			assessments.setLayoutOnChange(true);

			tbl.removeAllListeners();
			tbl.addTableListener(new TableListener() {

				public void tableCellClick(TableEvent be) {
					if (be.getItem() == null)
						return;

					final int columnIndex = be.getColumnIndex();
					final Integer id = (Integer) be.getItem().getValue(6);
					final String status = (String) be.getItem().getValue(3);
					final String type = status.equals("Published") ? AssessmentType.PUBLISHED_ASSESSMENT_TYPE : AssessmentType.DRAFT_ASSESSMENT_TYPE;
					if (columnIndex == 5) {
						if (type == AssessmentType.PUBLISHED_ASSESSMENT_TYPE
								&& !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser,
										AuthorizableObject.DELETE, AssessmentCache.impl.getPublishedAssessment(id,
												false))) {
							WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission "
									+ "to perform this operation.");
						} else if (type == AssessmentType.DRAFT_ASSESSMENT_TYPE
								&& !AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser,
										AuthorizableObject.DELETE, AssessmentCache.impl.getDraftAssessment(id, false))) {
							WindowUtils.errorAlert("Insufficient Permissions", "You do not have permission "
									+ "to perform this operation.");
						} else {
							WindowUtils.confirmAlert("Confirm Delete",
									"Are you sure you want to delete this assessment?",
									new WindowUtils.MessageBoxListener() {
										public void onNo() {
										}

										public void onYes() {
											if (AssessmentCache.impl.getCurrentAssessment() != null
													&& AssessmentCache.impl.getCurrentAssessment().getId() == id
															.intValue())
												AssessmentCache.impl.resetCurrentAssessment();
											NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
											doc.delete(UriBase.getInstance().getSISBase() + "/assessments/" + type
													+ "/" + id, new GenericCallback<String>() {
												public void onFailure(Throwable arg0) {

												}

												public void onSuccess(String arg0) {
													TaxonomyCache.impl.evict(String.valueOf(node.getId()));
													TaxonomyCache.impl.fetchTaxon(node.getId(), true,
															new GenericCallback<Taxon>() {
																public void onFailure(Throwable caught) {
																};

																public void onSuccess(Taxon result) {
																	AssessmentCache.impl.clear();
																	update(node.getId());
																	panelManager.recentAssessmentsPanel.update();
																};
															});
												}
											});
										}
									});
						}
					} else if (columnIndex == 4) {
						Assessment fetched = AssessmentCache.impl.getAssessment(id, false);
						System.out.println("Got assessment " + fetched == null ? fetched : fetched.getId());
						// CHANGE
						if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ,
								fetched)) {
							AssessmentCache.impl.setCurrentAssessment(fetched);
							ClientUIContainer.headerContainer.update();
							ClientUIContainer.bodyContainer
									.setSelection(ClientUIContainer.bodyContainer.tabManager.assessmentEditor);
						} else {
							WindowUtils.errorAlert("Sorry, you do not have permission to view this assessment.");
						}
					}
				}

			});

			
			
			for (Assessment data : AssessmentCache.impl.getPublishedAssessmentsForTaxon(node.getId())) {
				String date = data.getDateAssessed() == null ? null : FormattedDate.impl
						.getDate(data.getDateAssessed());

				Object[] values = new Object[7];
				values[0] = data.getDateAssessed() == null ? "(Not set)" : FormattedDate.impl.getDate(data.getDateAssessed());
				values[1] = AssessmentFormatter.getProperCategoryAbbreviation(data);
				values[2] = AssessmentFormatter.getProperCriteriaString(data);
				values[3] = "Published";
				values[4] = "";
				values[5] = "";
				values[6] = Integer.valueOf(data.getId());

				TableItem item = new TableItem(values);
				tbl.add(item);
			}

			for (Assessment data : AssessmentCache.impl.getDraftAssessmentsForTaxon(node.getId())) {

				Object[] values = new Object[7];

				if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.READ, data)) {
					values[0] = data.getDateAssessed() == null ? "(Not set)" :  FormattedDate.impl.getDate(data.getDateAssessed());
					values[1] = AssessmentFormatter.getProperCategoryAbbreviation(data);
					values[2] = AssessmentFormatter.getProperCriteriaString(data);
					if (data.isRegional())
						values[3] = "Draft - " + RegionCache.impl.getRegionName(data.getRegionIDs());
					else
						values[3] = "Draft";
					values[4] = "";
					values[5] = "";
					values[6] = Integer.valueOf(data.getId());
				} else {
					values[0] = "Sorry, you";
					values[1] = "do not have";
					values[2] = "permission";
					values[3] = "to view this";
					values[4] = "draft assessment.";
					values[5] = "";
					values[6] = Integer.valueOf(data.getId());
				}

				TableItem item = new TableItem(values);
				tbl.add(item);

			}

			tbl.sort(0, SortDir.DESC);
			ClientUIContainer.headerContainer.update();

			assessments.add(tbl);
			return assessments;
		}

		private ContentPanel getChildrenPanel() {
			final ContentPanel children = new ContentPanel();
			children.setLayout(new RowLayout(Orientation.VERTICAL));
			children.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			children.setHeight(200);
			if (Taxon.getDisplayableLevelCount() > taxon.getLevel() + 1) {
				try {
					children.setHeading(Taxon.getDisplayableLevel(taxon.getLevel() + 1));
					// children.setLayoutOnChange(true);
					final TaxonPagingLoader loader = new TaxonPagingLoader();
					final PagingToolBar bar = new PagingToolBar(30);
					bar.bind(loader.getPagingLoader());
					final DataList list = new DataList();
					list.setSize((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2, 148);
					list.setScrollMode(Scroll.AUTOY);
					final ListStore<TaxonListElement> store = new ListStore<TaxonListElement>(loader.getPagingLoader());
					store.setStoreSorter(new StoreSorter<TaxonListElement>(new PortableAlphanumericComparator()));

					final DataListBinder<TaxonListElement> binder = new DataListBinder<TaxonListElement>(list, store);
					binder.setDisplayProperty("name");
					binder.init();

					TaxonTreePopup.fetchChildren(taxon, new GenericCallback<List<TaxonListElement>>() {

						public void onFailure(Throwable caught) {
							children.add(new HTML("No " + Taxon.getDisplayableLevel(taxon.getLevel() + 1) + "."));
						}

						public void onSuccess(List<TaxonListElement> result) {
							loader.getFullList().addAll(result);
							ArrayUtils.quicksort(loader.getFullList(), new Comparator<TaxonListElement>() {
								public int compare(TaxonListElement o1, TaxonListElement o2) {
									return ((String) o1.get("name")).compareTo((String) o2.get("name"));
								}
							});
							children.add(list);
							children.add(bar);

							loader.getPagingLoader().load(0, loader.getPagingLoader().getLimit());

							children.layout();
						}
					});

					binder.addSelectionChangedListener(new SelectionChangedListener<TaxonListElement>() {

						@Override
						public void selectionChanged(SelectionChangedEvent<TaxonListElement> se) {
							if (se.getSelectedItem() != null) {
								update(se.getSelectedItem().getNode().getId());
							}
						}
					});
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else {
				children.setHeading("Not available.");
			}

			return children;
		}

		private ContentPanel getDistributionMapPanel(final Taxon node) {
			ContentPanel cp = new ContentPanel();
			cp.setHeading("Distribution Map");
			cp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			cp.setHeight(200);
			final LayoutContainer vp = new LayoutContainer();
			vp.setLayoutOnChange(true);
			vp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			vp.setHeight(200);
			cp.add(vp);
			vp.setStyleName("SIS_taxonSummaryHeader_mapPanel");

			googleMap = new Image(UriBase.getInstance().getSISBase() + "/raw/browse/spatial/noMapAvailable.jpg");
			NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			doc.get(UriBase.getInstance().getSISBase() + "/raw/browse/spatial/" + node.getId() + ".jpg",
					new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							Image map = new Image(UriBase.getInstance().getSISBase()
									+ "/raw/browse/spatial/noMapAvailable.jpg");
							map.setSize((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2 + "", "175");
							ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.setMap(map);
							vp.add(googleMap);

						}

						public void onSuccess(String result) {
							Image map = new Image("/raw/browse/spatial/" + node.getId() + ".jpg");
							map.addClickHandler(new ClickHandler() {
								public void onClick(ClickEvent event) {
									Window s = WindowUtils.getWindow(false, false, "Map Distribution Viewer");
									LayoutContainer content = s;
									content
											.add(ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel);
									if (!ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel
											.isRendered())
										ClientUIContainer.bodyContainer.getTabManager().getPanelManager().imageViewerPanel
												.update(new ManagedImage(googleMap, ManagedImage.IMG_JPEG));
									s.setHeight(600);
									s.setWidth(800);
									s.show();
									s.center();

								}
							});

							ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.setMap(map);
							vp.add(googleMap);
						}
					});

			return cp;
		}

		private ContentPanel getGeneralInformationPanel(final Taxon node) {
			final ContentPanel generalInformation = new ContentPanel();
			generalInformation.setStyleName("x-panel");
			panelHeight = 180;
			generalInformation.setWidth(350);

			generalInformation.setHeading("General Information");
			BorderLayout generalLayout = new BorderLayout();
			// generalLayout.setSpacing(5);
			generalInformation.setLayout(generalLayout);
			generalInformation.setLayoutOnChange(true);
			final BorderLayoutData image = new BorderLayoutData(LayoutRegion.WEST, 100);

			final BorderLayoutData info = new BorderLayoutData(LayoutRegion.CENTER);

			final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
			doc.get(UriBase.getInstance().getImageBase() + "/images/" + node.getId(), new GenericCallback<String>() {
				public void onFailure(Throwable caught) {
					Debug.println("failed to fetch xml");
				}
				public void onSuccess(String result) {
					taxonImage = null;
					NativeNodeList list = doc.getDocumentElement().getElementsByTagName("image");
					for (int i = 0; i < list.getLength(); i++) {
						boolean primary = ((NativeElement) list.item(i)).getAttribute("primary").equals("true");
						if (primary) {
							String ext = "";
							if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/jpeg"))
								ext = "jpg";
							if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/gif"))
								ext = "gif";
							if (((NativeElement) list.item(i)).getAttribute("encoding").equals("image/png"))
								ext = "png";

							ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
									.setImage(new Image(UriBase.getInstance().getSISBase() + "/raw/images/bin/"
											+ ((NativeElement) list.item(i)).getAttribute("id") + "." + ext));
						}
					}
					if (taxonImage == null) {
						taxonImage = new Image("images/unavailable.png");
						ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel
								.setImage(taxonImage);
					}

					VerticalPanel vp = new VerticalPanel();
					vp.setSize("100px", "100px");

					taxonImage.setWidth("100px");
					taxonImage.setHeight("100px");
					taxonImage.setStyleName("SIS_taxonSummaryHeader_image");
					taxonImage.setTitle("Click for Image Viewer");
					vp.add(taxonImage);
					generalInformation.add(vp, image);

				}
			});

			// ADD GENERAL INFO
			LayoutContainer data = new LayoutContainer();
			data.setWidth(240);
			if (!node.isDeprecated())
				data.add(new HTML("Name: <i>" + node.getName() + "</i>"));
			else
				data.add(new HTML("Name: <s>" + node.getName() + "</s>"));

			data.add(new HTML("&nbsp;&nbsp;Taxon ID: "
					+ "<a target='_blank' href='http://www.iucnredlist.org/apps/redlist/details/" + node.getId()
					+ "'>" + node.getId() + "</a>"));

			if (node.getLevel() >= TaxonLevel.SPECIES) {
				panelHeight += 10;
				data.add(new HTML("Full Name:  <i>" + node.getFullName() + "</i>"));
			}

			data.add(new HTML("Level: " + node.getDisplayableLevel()));
			if (node.getParentName() != null) {
				panelHeight += 10;
				HTML parentHTML = new HTML("Parent:  <i>" + node.getParentName() + "</i>"
						+ "<img src=\"images/icon-tree.png\"></img>");
				parentHTML.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						new TaxonTreePopup(node).show();
					}
				});
				data.add(parentHTML);
			}
			if (node.getTaxonomicAuthority() != null && !node.getTaxonomicAuthority().equalsIgnoreCase("")) {
				panelHeight += 20;
				data.add(new HTML("Taxonomic Authority: " + node.getTaxonomicAuthority()));
			}

			data.add(new HTML("Status: " + node.getStatusCode()));
			data.add(new HTML("Hybrid: " + node.getHybrid()));

			// ADD SYNONYMS
			if (node.getSynonyms().size() != 0) {
				panelHeight += 150;
				data.add(new HTML("<hr><br />"));
				data.add(new HTML("<b>Synonyms</b>"));
				int size = node.getSynonyms().size();
				if (size > 5)
					size = 5;

				for (final Synonym curSyn : node.getSynonyms()) {
					HorizontalPanel hp = new HorizontalPanel();

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
						final Image notesImage = new Image("images/icon-note.png");
						if (curSyn.getNotes().isEmpty())
							notesImage.setUrl("images/icon-note-grey.png");
						notesImage.setTitle("Add/Remove Notes");
						notesImage.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								final Window s = WindowUtils.getWindow(false, false, "Notes for Synonym "
										+ curSyn.getName());
								final LayoutContainer container = s;
								container.setLayoutOnChange(true);
								FillLayout layout = new FillLayout(Orientation.VERTICAL);
								container.setLayout(layout);

								final TextArea area = new TextArea();
								Set<Notes> notesSet = curSyn.getNotes();
								String noteValue = "";
								for (Notes note : notesSet)
									noteValue += note.getValue();
								area.setText(noteValue);
								area.setSize("400", "75");
								container.add(area);
								HorizontalPanel buttonPanel = new HorizontalPanel();

								final Button cancel = new Button();
								cancel.setText("Cancel");
								cancel.addListener(Events.Select, new Listener() {
									public void handleEvent(BaseEvent be) {
										s.hide();

									}
								});
								final Button save = new Button();
								save.setText("Save");
								save.addListener(Events.Select, new Listener() {
									public void handleEvent(BaseEvent be) {
										Notes newNote = new Notes();
										newNote.setValue(area.getText());
										newNote.setSynonym(curSyn);
										curSyn.getNotes().add(newNote);
										if (!curSyn.getNotes().equals(""))
											notesImage.setUrl("images/icon-note.png");
										else
											notesImage.setUrl("images/icon-note-grey.png");
										s.hide();
										TaxonomyCache.impl.saveTaxon(node, new GenericCallback<String>() {
											public void onFailure(Throwable caught) {

											};

											public void onSuccess(String result) {

											};
										});
									}
								});
								buttonPanel.add(cancel);
								buttonPanel.add(save);
								container.add(buttonPanel);

								s.setSize(500, 400);
								s.show();
								s.center();

							}
						});
						hp.add(notesImage);
					}

					String value = curSyn.toDisplayableString();
					if (curSyn.getStatus().equals(Synonym.ADDED) || curSyn.getStatus().equals(Synonym.DELETED))
						value += "-- " + curSyn.getStatus();

					hp.add(new HTML("&nbsp;&nbsp;" + value));

					data.add(hp);
				}
				if (node.getSynonyms().size() > 5) {
					HTML viewAll = new HTML("View all...");
					viewAll.setStyleName("SIS_HyperlinkLookAlike");
					viewAll.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							if (TaxonomyCache.impl.getCurrentTaxon() == null) {
								Info.display(new InfoConfig("No Taxa Selected", "Please select a taxa first."));
								return;
							}

							final Window s = WindowUtils.getWindow(false, false, "Synonyms");
							s.setSize(400, 400);
							LayoutContainer data = s;
							data.setScrollMode(Scroll.AUTO);

							VerticalPanel currentSynPanel = new VerticalPanel();
							currentSynPanel.setSpacing(3);

							HTML curHTML = new HTML("Current Synonyms");
							curHTML.addStyleName("bold");
							currentSynPanel.add(curHTML);

							if (TaxonomyCache.impl.getCurrentTaxon().getSynonyms().size() == 0)
								currentSynPanel.add(new HTML("There are no synonyms for this taxon."));

							for (Synonym curSyn : TaxonomyCache.impl.getCurrentTaxon().getSynonyms()) {
								curHTML = new HTML(curSyn.getFriendlyName());
								currentSynPanel.add(curHTML);
							}

							data.add(currentSynPanel);
							s.show();

						}
					});
					data.add(viewAll);
				}
			}

			// ADD COMMON NAMES
//			Image addName = new Image("images/add.png");
//			addName.setSize("14px", "14px");
//			addName.setTitle("Add New Common Name");
//			addName.addClickHandler(new ClickHandler() {
//
//				public void onClick(ClickEvent event) {
//
//					Window addNameBox = CommonNameDisplay.getNewCommonNameDisplay(node, null,
//							new GenericCallback<String>() {
//								public void onFailure(Throwable arg0) {
//									update(node.getId());
//								}
//
//								public void onSuccess(String arg0) {
//									update(node.getId());
//								}
//							});
//
//					addNameBox.show();
//					addNameBox.center();
//				}
//			});
//
			HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");
			LayoutContainer commonNamePanel = new LayoutContainer();
//
//			if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node))
//				commonNamePanel.add(addName);
			commonNamePanel.add(commonNamesHeader);
//
			data.add(new HTML("<hr><br />"));
			data.add(commonNamePanel);

			if (node.getCommonNames().size() != 0) {

				int loop = 5;
				if (node.getCommonNames().size() < 5)
					loop = node.getCommonNames().size();
				panelHeight += loop * 15 + 20;
				
				for (final CommonName curName : node.getCommonNames()) {
					HorizontalPanel hp = new HorizontalPanel();

					if (AuthorizationCache.impl.hasRight(SimpleSISClient.currentUser, AuthorizableObject.WRITE, node)) {
						final Image notesImage = new Image("images/icon-note.png");
						if (curName.getNotes().isEmpty())
							notesImage.setUrl("images/icon-note-grey.png");
						notesImage.setTitle("Add/Remove Notes");
						notesImage.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								final Window s = WindowUtils.getWindow(false, false, "Notes for Common Name "
										+ curName.getName());
								final LayoutContainer container = s;
								container.setLayoutOnChange(true);
								FillLayout layout = new FillLayout(Orientation.VERTICAL);
								container.setLayout(layout);

								final TextArea area = new TextArea();
								Set<Notes> notesSet = curName.getNotes();
								String noteValue = "";
								for (Notes note : notesSet)
									noteValue += note.getValue();
								area.setText(noteValue);
								area.setSize("400", "75");
								container.add(area);
								HorizontalPanel buttonPanel = new HorizontalPanel();

								final Button cancel = new Button();
								cancel.setText("Cancel");
								cancel.addListener(Events.Select, new Listener() {
									public void handleEvent(BaseEvent be) {
										s.hide();

									}
								});
								final Button save = new Button();
								save.setText("Save");
								save.addListener(Events.Select, new Listener() {
									public void handleEvent(BaseEvent be) {
										Notes newNote = new Notes();
										newNote.setValue(area.getText());
										newNote.setCommonName(curName);
										curName.getNotes().add(newNote);
										if (!curName.getNotes().isEmpty())
											notesImage.setUrl("images/icon-note.png");
										else
											notesImage.setUrl("images/icon-note-grey.png");
										s.hide();
										TaxonomyCache.impl.addOrEditCommonName(node, curName, new GenericCallback<String>() {
											
											@Override
											public void onSuccess(String result) {
												WindowUtils.infoAlert("Saved", "Common name " + curName.getName() + " was saved.");
												ClientUIContainer.bodyContainer.tabManager.panelManager.taxonomicSummaryPanel.update(node.getId());
											}
										
											@Override
											public void onFailure(Throwable caught) {
												WindowUtils.errorAlert("Error", "An error occurred when trying to save the common name data related to "
														+ node.getFullName() + ".");
											}
										});
										
										
									}
								});
								buttonPanel.add(cancel);
								buttonPanel.add(save);
								container.add(buttonPanel);

								s.setSize(500, 400);
								s.show();
								s.center();

							}
						});
						hp.add(notesImage);
					}

					String value = curName.getName();
					hp.add(new HTML("&nbsp;&nbsp;" + value));

					data.add(hp);
					
					
				}
//				List<CommonName> namesList = new ArrayList<CommonName>(node.getCommonNames());
//				for (int i = 0; i < loop; i++) {
//					CommonName curName = namesList.get(i);
//					data.add(new CommonNameDisplay(node, curName).show(new GenericCallback<String>() {
//						public void onFailure(Throwable arg0) {
//							update(node.getId());
//						}
//
//						public void onSuccess(String arg0) {
//							update(node.getId());
//						}
//					}));
//				}
				HTML viewAll = new HTML("View all...");
				viewAll.setStyleName("SIS_HyperlinkLookAlike");
				viewAll.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						final Window s = WindowUtils.getWindow(false, false, "Edit Common Names");
						LayoutContainer data = s;
						data.setScrollMode(Scroll.AUTO);

						HTML commonNamesHeader = new HTML("<b>Common Name --- Language</b>");

						LayoutContainer commonNamePanel = new LayoutContainer();
						commonNamePanel.add(commonNamesHeader);

						data.add(new HTML("<hr><br />"));
						data.add(commonNamePanel);

						if (TaxonomyCache.impl.getCurrentTaxon().getCommonNames().size() != 0) {
							for (CommonName curName : TaxonomyCache.impl.getCurrentTaxon().getCommonNames()) {
								data.add(new HTML(curName.getName()));
							}
						} else
							data.add(new HTML("No Common Names."));

						s.setSize(350, 550);
						s.show();
						s.center();

					}
				});
				if (node.getCommonNames().size() > 5)
					data.add(viewAll);
			} else
				data.add(new HTML("No Common Names."));

			generalInformation.setHeight(panelHeight);
			generalInformation.add(data, info);
			return generalInformation;
		}

		private ContentPanel getTaxonomicNotePanel(final Taxon node) {
			ContentPanel cp = new ContentPanel();
			cp.setHeading("Taxonomic Notes");
			cp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			cp.setHeight(200);
			final LayoutContainer vp = new LayoutContainer();
			vp.setLayoutOnChange(true);
			vp.setWidth((com.google.gwt.user.client.Window.getClientWidth() - 500) / 2);
			vp.setHeight(200);
			cp.add(vp);

			if (node.getAssessments().size() > 0) {
				Assessment curAssessment = null;
				if (node.getAssessments().size() == 0) {
					vp.add(new HTML("No Taxonomic Notes Available."));

				} else {
					List<Assessment> pubAssessments = new ArrayList<Assessment>(TaxonomyCache.impl.getTaxon(
							node.getId()).getAssessments());

					Collections.sort(pubAssessments, new Comparator<Assessment>() {
						public int compare(Assessment o1, Assessment o2) {
							Date date2 = o2.getDateAssessed();
							Date date1 = o1.getDateAssessed();

							if (date2 == null)
								return -1;
							else if (date1 == null)
								return 1;

							int ret = date2.compareTo(date1);

							if (ret == 0) {
								if (o2.getIsHistorical())
									ret = -1;
								else
									ret = 1;
							}

							return ret;
						}
					});
					curAssessment = pubAssessments.get(0);

					String notes = (String) curAssessment.getPrimitiveValue(CanonicalNames.TaxonomicNotes, "value");
					if (notes != null)
						vp.add(new Html(XMLUtils.cleanFromXML(notes.replaceAll("<em>", "<i>").replaceAll("</em>",
								"</i>"))));
					else
						vp.add(new Html("No Taxonomic Notes Available."));

					vp.layout();
				}
			}

			return cp;
		}

		public void resize(int width, int height) {
			onResize(width, height);
		}

		public void updatePanel(final Taxon node) {
			removeAll();
			if (node != null) {
				AssessmentCache.impl.fetchAssessments(new AssessmentFetchRequest(null, node.getId()),
						new GenericCallback<String>() {
							public void onSuccess(String result) {
								drawPanel(node);
							}

							public void onFailure(Throwable caught) {
							};
						});
			} else
				drawPanel(node);
		}

		private void drawPanel(final Taxon node) {
			WindowUtils.hideLoadingAlert();
			removeAll();
			setLayoutOnChange(true);
			if (node == null) {
				add(new HTML("No summary available."));
				return;
			}

			RowLayout innerLayout = new RowLayout();
			innerLayout.setOrientation(Orientation.VERTICAL);
			wrapper = new DockPanel();
			FillLayout layout = new FillLayout();

			String name = "";
			HorizontalPanel hPanel = new HorizontalPanel();

			hPanel.setStyleName("SIS_taxonSummaryHeader_panel");

			Image prevTaxon = new Image("tango/actions/go-previous.png");

			prevTaxon.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					TaxonomyCache.impl.fetchTaxon(node.getParentId(), true, new GenericCallback<Taxon>() {
						public void onFailure(Throwable caught) {
							inner.updatePanel(node);
						}

						public void onSuccess(Taxon arg0) {
							inner.updatePanel(arg0);
							ClientUIContainer.headerContainer.update();
							update(node.getParentId());
						}
					});
				}

			});

			if (node.getParentId() != 0) {
				hPanel.add(prevTaxon);
				hPanel.setCellWidth(prevTaxon, "30px");
				hPanel.setCellVerticalAlignment(prevTaxon, HasVerticalAlignment.ALIGN_MIDDLE);
			}
			if (node.getLevel() >= TaxonLevel.SPECIES)
				name = node.getFullName();
			else
				name = node.getName();

			HTML header = new HTML(" <i>" + name + "</i>");
			headerAssess = new HTML("");

			hPanel.add(header);
			hPanel.add(headerAssess);

			headerAssess.setStyleName("SIS_taxonSummaryHeader");
			header.setStyleName("SIS_taxonSummaryHeader");

			wrapper.add(hPanel, DockPanel.NORTH);

			VerticalPanel westPanel = new VerticalPanel();
			westPanel.add(getGeneralInformationPanel(node));

			if (node.getLevel() >= TaxonLevel.SPECIES) {

				westPanel.add(getAssessmentInformationPanel(node));

			} else {
				AssessmentCache.impl.resetCurrentAssessment();
			}

			wrapper.add(westPanel, DockPanel.WEST);

			HorizontalPanel hp = new HorizontalPanel();

			hp.add(getTaxonomicNotePanel(node));

			hp.add(getChildrenPanel());

			VerticalPanel vp = new VerticalPanel();
			vp.clear();
			if (node.getLevel() >= TaxonLevel.SPECIES) {

				vp.add(getAssessmentsPanel(node));

			}

			vp.add(hp);
			wrapper.add(vp, DockPanel.CENTER);
			wrapper.setSize("100%", "100%");
			add(wrapper);
		}
	}

	private PanelManager panelManager = null;
	private Image taxonImage;
	private Window imagePopup = null;
	private Image googleMap;
	private DescriptionPanel inner = null;
	private HTML headerAssess;
	private Taxon taxon;
	private int panelHeight;
	private ImageManagerPanel imageManager;

	public TaxonHomePage(PanelManager manager) {
		this.setScrollMode(Scroll.AUTO);
		addStyleName("gwt-background");
		inner = new DescriptionPanel();
		setLayoutOnChange(true);
		panelManager = manager;

	}

	public void buildNotePopup() {
		final Window s = WindowUtils.getWindow(false, false, "Notes for " + taxon.getFullName());
		final LayoutContainer container = s;
		container.setLayoutOnChange(true);
		final VerticalPanel panelAdd = new VerticalPanel();
		panelAdd.setSpacing(3);
		panelAdd.add(new HTML("Add Note: "));

		final TextArea area = new TextArea();
		area.setSize("400", "75");
		panelAdd.add(area);

		Button save = new Button("Add Note");
		save.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (area.getText().trim().equalsIgnoreCase("")) {
					WindowUtils.errorAlert("Must enter note body.");

				} else {
					final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
					String url = UriBase.getInstance().getNotesBase() + "/notes/taxon/"+ taxon.getId();
					
					doc.post(url, area.getText().trim(), new GenericCallback<String>() {
						public void onFailure(Throwable caught) {
							WindowUtils.errorAlert("Unable to create note.");							
						};

						public void onSuccess(String result) {
							Notes note = Notes.fromXML(doc.getDocumentElement());
							taxon.getNotes().add(note);
						};
					});

					s.hide();
				}
			}
		});
		Button close = new Button("Close");
		close.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				s.hide();
			}
		});

		panelAdd.add(save);
		panelAdd.add(close);
		
		if (taxon == null || taxon.getNotes().isEmpty()) {
			container.add(new HTML("<div style='padding-top:10px';background-color:grey><b>There are no notes for this taxon.</b></div>"));
			container.add(panelAdd);
			s.setSize(500, 400);
			s.show();
			s.center();
		} else {
			
			final VerticalPanel eBar = new VerticalPanel();
			eBar.setSize("400", "200");

			
			
			for (final Notes note : TaxonomyCache.impl.getCurrentTaxon().getNotes()) {
				final HorizontalPanel a = new HorizontalPanel();
				Image deleteNote = new Image("images/icon-note-delete.png");
				deleteNote.setTitle("Delete Note");
				deleteNote.addClickHandler(new ClickHandler() {
					
					public void onClick(ClickEvent event) {
						System.out.println("it was clicked");
						NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
						String url = UriBase.getInstance().getNotesBase() + "/notes/note/" + note.getId();

						doc.post(url + "?option=remove", note.toXML(), new GenericCallback<String>() {
							public void onFailure(Throwable caught) {
								WindowUtils.errorAlert("Unable to delete note.");
							};

							public void onSuccess(String result) {
								taxon.getNotes().remove(note);
								eBar.remove(a);
							};
						});

					}
				});
				
				a.setWidth("100%");
				a.add(deleteNote);
				a.add(new HTML("<b>" + note.getEdit().getUser().getDisplayableName() + " ["
						+ FormattedDate.impl.getDate(note.getEdit().getCreatedDate()) + "]</b>  --"
						+ note.getValue()));// );
				System.out.println("adding a note with text " + note.getValue());
				eBar.add(a);		
			}
			
			container.add(eBar);
			container.add(panelAdd);
			s.setSize(500, 400);
			s.show();
			s.center();
		}
	}

	public void buildReferencePopup() {
		final Window s = WindowUtils.getWindow(false, false, "Add a references to " + taxon.getFullName());
		s.setIconStyle("icon-book");
		LayoutContainer container = s;
		container.setLayout(new FillLayout());

		ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel.setReferences(taxon);

		container.add(ClientUIContainer.bodyContainer.tabManager.panelManager.refViewPanel);

		s.setSize(850, 550);
		s.show();
		s.center();
	}

	public void onResize(int width, int height) {
		super.onResize(width, height);
		inner.resize(width, height);
	}

	public void setImage(Image image) {
		taxonImage = image;
		taxonImage.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (imagePopup == null) {
					imageManager = new ImageManagerPanel(String.valueOf(taxon.getId()));

					imagePopup = WindowUtils.getWindow(false, false, "Photo Station");
					imagePopup.add(imageManager);
				}

				imageManager.setTaxonId(String.valueOf(taxon.getId()));
				imageManager.update();
				imagePopup.setScrollMode(Scroll.AUTO);
				imagePopup.show();
				imagePopup.setSize(600, 330);
				imagePopup.setPagePosition(event.getNativeEvent().getClientX(), event.getNativeEvent().getClientY());
			}
		});
	}

	public void setMap(Image image) {
		googleMap = image;
	}

	public void update(final Integer nodeID) {
		System.out.println("calling update");
		WindowUtils.showLoadingAlert("Loading...");

		Timer timer = new Timer() {
			public void run() {
				updateDelayed(nodeID);

			}
		};
		timer.schedule(150);

	}

	private void updateDelayed(final Integer nodeID) {
		WindowUtils.hideLoadingAlert();
		if (imagePopup != null && imagePopup.isRendered())
			imageManager.setTaxonId(String.valueOf(taxon.getId()));
		removeAll();

		if (nodeID == null)
			inner.updatePanel(null);
		else {
			boolean resetAsCurrent = TaxonomyCache.impl.getCurrentTaxon() == null ? true : !nodeID.equals(Integer
					.valueOf(TaxonomyCache.impl.getCurrentTaxon().getId()));
			TaxonomyCache.impl.fetchTaxon(nodeID, resetAsCurrent, new GenericCallback<Taxon>() {
				public void onFailure(Throwable caught) {
					WindowUtils.errorAlert("Taxon ID " + nodeID + " does not exist.");
					inner.updatePanel(null);
					add(inner);
				}

				public void onSuccess(Taxon arg0) {
					taxon = (Taxon) arg0;
					if (ClientUIContainer.bodyContainer.getSelectedItem().equals(
							ClientUIContainer.bodyContainer.tabManager.taxonHomePage))
						inner.updatePanel((Taxon) arg0);
					ClientUIContainer.headerContainer.update();
					add(inner);
				}
			});
		}

	}
}
