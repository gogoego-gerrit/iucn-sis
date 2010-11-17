package org.iucn.sis.shared.api.criteriacalculator;




public class Line {

	public double slope;
	public int x1;
	public int x2;
	public double y1;
	public double y2;

	public Line() {

	}

	public Line(int x1, int x2, double y1, double y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.slope = (y2 - y1) / (x2 - x1);
	}

	/**
	 * give it the y value, it will spit out corresponding x in the line
	 * segment, if it isn't in the line segment it will return -1
	 * 
	 * @param y
	 * @return
	 */
	public int x(double y) {
		int x = -1;
		x = (int) ((y - y1) / slope + x1);
		return x;
	}

}
