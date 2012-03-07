package org.iucn.sis.shared.api.structures;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;

public abstract class CriteriaGrid2_3 extends CriteriaGrid {

	public void buildGrid() {
		gridA = new Grid(2, 5);
		gridB = new Grid(3, 5);
		gridC = new Grid(2, 2);
		gridD = new Grid(2, 2);
		gridE = new Grid(1, 1);
		
		ClickHandler handler = new ClickHandler() {
			public void onClick(ClickEvent event) {
				updateCriteriaString(createCriteriaString());
			}
		};

		gridA.addClickHandler(handler);
		gridB.addClickHandler(handler);
		gridC.addClickHandler(handler);
		gridD.addClickHandler(handler);
		gridE.addClickHandler(handler);

		gridA.setCellSpacing(4);
		gridB.setCellSpacing(4);
		gridC.setCellSpacing(4);
		gridD.setCellSpacing(4);
		gridE.setCellSpacing(4);

		gridA.setWidget(0, 0, createWidget("A1a", 0, 0));
		gridA.setWidget(0, 1, createWidget("A1b", 0, 1));
		gridA.setWidget(0, 2, createWidget("A1c", 0, 2));
		gridA.setWidget(0, 3, createWidget("A1d", 0, 3));
		gridA.setWidget(0, 4, createWidget("A1e", 0, 4));
		gridA.setWidget(1, 0, createWidget("A2b", 1, 0));
		gridA.setWidget(1, 1, createWidget("A2c", 1, 1));
		gridA.setWidget(1, 2, createWidget("A2d", 1, 2));
		gridA.setWidget(1, 3, createWidget("A2e", 1, 3));

		gridB.setWidget(0, 0, createWidget("B1", 0, 0));
		gridB.setWidget(1, 0, createWidget("B2a", 1, 0));
		gridB.setWidget(1, 1, createWidget("B2b", 1, 1));
		gridB.setWidget(1, 2, createWidget("B2c", 1, 2));
		gridB.setWidget(1, 3, createWidget("B2d", 1, 3));
		gridB.setWidget(1, 4, createWidget("B2e", 1, 4));
		gridB.setWidget(2, 0, createWidget("B3a", 2, 0));
		gridB.setWidget(2, 1, createWidget("B3b", 2, 1));
		gridB.setWidget(2, 2, createWidget("B3c", 2, 2));
		gridB.setWidget(2, 3, createWidget("B3d", 2, 3));

		gridC.setWidget(0, 0, createWidget("C1", 0, 0));
		gridC.setWidget(1, 0, createWidget("C2a", 1, 0));
		gridC.setWidget(1, 1, createWidget("C2b", 1, 1));

		gridD.setWidget(0, 0, createWidget("D", 0, 0));
		gridD.setWidget(1, 0, createWidget("D1", 1, 0));
		gridD.setWidget(1, 1, createWidget("D2", 1, 1));

		gridE.setWidget(0, 0, createWidget("E", 0, 0));
	}

	@Override
	public boolean isCriteriaValid(String criteria, String category) {
		return true;
	}
}