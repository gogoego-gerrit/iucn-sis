package com.solertium.db;

public class BooleanLiteral implements Literal {
	Boolean b;
	
	public BooleanLiteral(final Boolean b) {
		this.b = b;
	}
	
	public Boolean getBoolean() {
		return b;
	}
	
	public boolean getPrimitiveBoolean() {
		return b == null ? false : b.booleanValue();
	}
	
	@Override
	public String toString() {
		return b == null ? Boolean.FALSE.toString() : b.toString();
	}

}
