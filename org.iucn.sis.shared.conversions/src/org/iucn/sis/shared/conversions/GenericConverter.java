package org.iucn.sis.shared.conversions;

public abstract class GenericConverter<T> extends Converter {

	protected T data;
	
	public GenericConverter() {
		super();
	}
	
	public void setData(T data) {
		this.data = data;
	}

}
