package org.iucn.sis.shared.api.structures;

import java.util.ArrayList;
import java.util.List;

import org.iucn.sis.shared.api.models.Field;
import org.iucn.sis.shared.api.models.PrimitiveField;

public abstract class SISPrimitiveStructure<T> extends Structure<PrimitiveField<T>> {
	
	//protected Map<String, PrimitiveField> currentData;

	public SISPrimitiveStructure(String struct, String descript, String structID) {
		this(struct, descript, structID, null);
	}

	public SISPrimitiveStructure(String struct, String descript, String structID, Object data) {
		super(struct, descript, structID, data);
		//currentData = new HashMap<String, PrimitiveField>();
	}
	
	/**
	 * Returns an ArrayList of descriptions (as Strings) for this structure, and
	 * if it contains multiples structures, all of those, in order.
	 */
	@Override
	public ArrayList<String> extractDescriptions() {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(description);
		return ret;
	}
	
	/**
	 * Compares the data this structure was set with, with what it gets from its widget(s).
	 * Returns true if they differ.
	 * @return true or false
	 */
	public boolean hasChanged(PrimitiveField<T> field) {
		String oldValue = field == null ? null : field.getRawValue();
		String newValue = getData();
		if (newValue == null)
			return oldValue != null;
		else
			if (oldValue == null)
				return true;
			else
				return !newValue.equals(oldValue);
		
		/*if (newData != null && !newData.equals("") ) {
			if (currentData.containsKey(getId())) {
				return !newData.equals( currentData.get(getId()).getRawValue() );
			} else
				return !newData.equals("");
		} else
			return currentData.containsKey(getId());*/
	}
	
	/**
	 * Sinks Widget data into the appropriate PrimitiveField object(s), associating
	 * them with the Field argument.
	 * 
	 * @return true if the save succeeded, or false if something unexpected occurred
	 */
	/*public void save(Field field) {
		//PrimitiveField newPrim = currentData.get(getId());
		PrimitiveField newPrim = currentData.get(getId());
		if( newPrim == null ) { 
			newPrim = getNewPrimitiveField();
			newPrim.setName(getId());
			newPrim.setField(field);
		}
		
		if( getData() != null ) {
			newPrim.setRawValue(getData());
			field.getPrimitiveField().add(newPrim);
		} else
			field.getPrimitiveField().remove(newPrim);
	}*/
	
	public boolean isPrimitive() {
		return true;
	}
	
	@Override
	public void save(Field parent, PrimitiveField<T> field) {
		final String data = getData();
		System.out.println("Saving data for " + getId() + ": " + data);
		if (getData() != null) {
			if (field == null) {
				field = getNewPrimitiveField();
				field.setField(parent);
				parent.addPrimitiveField(field);
			}
			field.setRawValue(data);
		}
		else
			System.out.println("Skipping" + getId() + ", no data to save");
	}
	
	/*public final void setData(Field field){
		Map<String, PrimitiveField> data = field.getKeyToPrimitiveFields();
		System.out.println("Setting data for structure " + getId() + " to be " + data.get(getId()));
		if( data.containsKey(getId()) )
			currentData.put(getId(), data.get(getId()));
		
		ArrayList<String> keys = extractDescriptions();
		
//		if( dataList.size()-dataOffset-keys.size() >= 0 )
		try {
			for(String key: keys) {
				//FIXME: This needs to properly set both a data and description entry
				//for each piece of data.
//				model.set(key, data.get(dataOffset+keys.indexOf(key)));
				model.set(key, "");
			}
		} catch (Exception ignored) {}
		
		setData(data);
	}*/
	
	@Override
	public List<ClassificationInfo> getClassificationInfo() {
		final ArrayList<ClassificationInfo> list = new ArrayList<ClassificationInfo>();
		list.add(new ClassificationInfo(description, getData()));
		return list;
	}
	
	/**
	 * Returns an empty PrimitiveField object, typed properly for this PrimitiveFieldWidget's
	 * data type.
	 * 
	 * @return a PrimitiveField object
	 */
	protected abstract PrimitiveField<T> getNewPrimitiveField();
	
	/*protected abstract void setData(Map<String, PrimitiveField> data);*/

}
