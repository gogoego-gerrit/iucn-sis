package org.iucn.sis.shared.api.schemes;

import java.util.List;

import org.iucn.sis.shared.api.data.TreeDataRow;
import org.iucn.sis.shared.api.structures.Structure;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

public interface ClassificationSchemeViewer {
	
	/**
	 * Draw the interface.
	 * @param isViewOnly true if it should be drawn 
	 * 		in view-only mode, false otherwise
	 * @return the widget containing your interface
	 */
	public LayoutContainer draw(final boolean isViewOnly);
	
	/**
	 * Determine if changes have been made.
	 * @return true if so, false otherwise
	 */
	public boolean hasChanged();
	
	/**
	 * Undo any changes and revert back to the last 
	 * saved state.
	 */
	public void revert();
	
	/**
	 * Return the current set of data and update 
	 * and change markers.
	 * @param deep - true if the data is saved to the server 
	 * and the internal hasChanged marker can be cleared, 
	 * false if this save is being done only to display the 
	 * data but it is still not saved to the server. 
	 * @return the data
	 */
	public List<ClassificationSchemeModelData> save(boolean deep);
	
	/**
	 * Provides the initial data set for the viewer.  This 
	 * will be called before draw()
	 * @param models the data
	 */
	public void setData(List<ClassificationSchemeModelData> models);
	
	/**
	 * Add a new model to the server. 
	 * @param model the data
	 */
	public void addModel(ClassificationSchemeModelData model);
	
	/**
	 * Remove a model from the server. 
	 * @param model
	 */
	public void removeModel(ClassificationSchemeModelData model);
	
	/**
	 * Updates the specified model.
	 * @param model
	 */
	public void updateModel(ClassificationSchemeModelData model);
	
	/**
	 * Returns a listing of all models currently in the 
	 * viewer.  These models may or may not have been 
	 * saved yet.
	 * @return
	 */
	public List<ClassificationSchemeModelData> getModels();
	
	/**
	 * Creates a new instance of model data.
	 * @param structure
	 * @return
	 */
	public ClassificationSchemeModelData newInstance(Structure structure);
	
	/**
	 * Generates the correct structure for the given tree data 
	 * row, or the default one if no override exists.
	 * @param row
	 * @return
	 */
	public Structure generateDefaultStructure(TreeDataRow row);
	
	/**
	 * Opens an editor capable of creating and editing models 
	 * for this viewer.
	 * @param model
	 * @param addToPagingLoader
	 * @param isViewOnly
	 * @return
	 */
	public ClassificationSchemeRowEditorWindow createRowEditorWindow(ClassificationSchemeModelData model, boolean addToPagingLoader, boolean isViewOnly);
	
	/**
	 * Returns true if this row is already added to the 
	 * server and can not be added again, false otherwise.
	 * @param row
	 * @return
	 */
	public boolean containsRow(TreeDataRow row);
}
