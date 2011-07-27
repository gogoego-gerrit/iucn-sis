package org.iucn.sis.shared.api.criteriacalculator;

import org.iucn.sis.shared.api.debug.Debug;



/**
 * Represents the "range" object, which is 4 numerical fields, the original
 * unparsed data entered in by the user, and the type of range (either a boolean
 * or a int range).
 * 
 * All fields that are negative were not specified by the user.
 * 
 * @author liz.schwartz
 * 
 */
public class Range {
	
	public static final int OBSERVED = 1;
	public static final int PROJECTED = 2;
	public static final int INFERRED = 3;
	public static final int ESTIMATED = 4;
	public static final int SUSPECTED = 5;
	
	public static Range dependentAND(Range a, Range b) {
		if (a != null && b != null)
			return min(a, b);
		else if (a == null)
			return a;
		else
			return b;
	}

	public static Range dependentOR(Range a, Range b) {
		if (a != null && b != null)
			return max(a, b);
		else if (a == null)
			return b;
		else
			return a;

	}

	public static Range divide(Range a, Range b) {

		if ((a != null) && (b != null)) {
			Range result = new Range();
			result.setHigh((a.getHigh()) / (b.getHigh()));
			result.setHighBest((a.getHighBest()) / (b.getHighBest()));
			result.setLow((a.getLow()) / (b.getLow()));
			result.setLowBest((a.getLowBest()) / (b.getLowBest()));
			return result;
		}

		else {
			return null;
		}
	}

	// BEGIN STATIC MATH FUNCTIONS ON RANGES
	public static Range dt(Range a, double dt) {
		if (dt >= 0 && dt <= 1 && a != null) {
			Range result = a;
			double high = result.getHigh();
			double low = result.getLow();
			double lowBest = result.getLowBest();
			double highBest = result.getHighBest();

			// ONLY ONE GUESS
			if (low == high) {
				return result;
			} else {
				double slopeLeft = (lowBest - low);
				double slopeRight = (high - highBest);
				double newLow = lowBest - ((slopeLeft) * (1 - dt));
				double newHigh = highBest + ((slopeRight * (1 - dt)));
				result.setHigh(newHigh);
				result.setLow(newLow);
			}

			return result;
		} else
			return null;
	}

	private static Range env(Range a, Range b) {
		double al = a.getLow();
		double ar = a.getHigh();
		double alb = a.getLowBest();
		double arb = a.getHighBest();
		double bl = b.getLow();
		double br = b.getHigh();
		double blb = b.getLowBest();
		double brb = b.getHighBest();

		Range result = new Range();
		result.setLow(Math.min(al, bl));
		result.setLowBest(Math.min(alb, blb));
		result.setHigh(Math.max(ar, br));
		result.setHighBest(Math.max(arb, brb));

		return result;
	}

	public static Range equals(Range a, double constant) {
		if (a != null) {
			double high = a.getHigh();
			double low = a.getLow();
			if ((high == constant) && (low == constant) && (a.getHighBest() == constant)
					&& (a.getLowBest() == constant)) {
				Range result = new Range("1");
				return result;
			} else if ((constant >= low) && (constant <= high)) {
				Range result = new Range("0-1");
				return result;
			} else {
				Range result = new Range("0");
				return result;
			}

		} else {
			return a;
		}
	}

	public static Range greaterthan(Range a, double constant) {
		if (a != null) {
			Range result = new Range();

			if (a.getLow() > constant) {
				result.setHigh(1);
				result.setLow(1);
				result.setHighBest(1);
				result.setLowBest(1);
			}

			else if (constant >= a.getHigh()) {
				result.setHigh(0);
				result.setLow(0);
				result.setHighBest(0);
				result.setLowBest(0);
			}

			else {
				result.setHigh(1);
				result.setLow(0);
				if (a.getLowBest() > constant) {
					result.setHighBest(1);
					result.setLowBest(1);
				} else if (constant >= a.getHighBest()) {
					result.setHighBest(0);
					result.setLowBest(0);
				} else {
					result.setHighBest(1);
					result.setLowBest(0);
				}
			}

			return result;
		} else
			return a;
	}

	public static Range greaterthanequal(Range a, double constant) {
		if (a != null) {
			Range result = new Range();

			if (a.getLow() >= constant) {
				result.setHigh(1);
				result.setLow(1);
				result.setHighBest(1);
				result.setLowBest(1);
			}

			else if (constant > a.getHigh()) {
				result.setHigh(0);
				result.setLow(0);
				result.setHighBest(0);
				result.setLowBest(0);
			}

			else {
				result.setHigh(1);
				result.setLow(0);
				if (a.getLowBest() >= constant) {
					result.setHighBest(1);
					result.setLowBest(1);
				} else if (constant > a.getHighBest()) {
					result.setHighBest(0);
					result.setLowBest(0);
				} else {
					result.setHighBest(1);
					result.setLowBest(0);
				}
			}

			return result;
		} else
			return a;
	}

	public static Range independentAND(Range a, Range b) {

		if (a != null && b != null) {
			Range cross = mult(a, b);
			Range min = min(a, b);
			Range result = env(cross, min);

			return result;
		} else if (a == null)
			return a;
		else
			return b;
	}

	public static Range independentOR(Range a, Range b) {
		Range result = null;
		if (a != null && b != null) {
			Range max = max(a, b);
			Range newA = subtract(1, a);
			Range newB = subtract(1, b);
			Range aCrossb = mult(newA, newB);
			Range finalb = subtract(1, aCrossb);
			result = env(max, finalb);

		} else if (a == null) {
			result = b;
		} else {
			result = a;
		}
		return result;
	}

	public static boolean isConstant(Range a, double constant) {
		if (a != null && a.getHigh() == constant && a.getHighBest() == constant && a.getLow() == constant
				&& a.getLowBest() == constant)
			return true;
		else
			return false;
	}

	public static Range isFalse(Range a) {
		if (a != null) {
			Range result = new Range();
			result.setHigh(1 - a.getLow());
			result.setHighBest(1 - a.getLowBest());
			result.setLow(1 - a.getHigh());
			result.setLowBest(1 - a.getHighBest());
			return result;
		} else
			return a;
	}

	public static Range lessthan(Range a, double constant) {

		if (a != null) {
			Range result = new Range();

			if (a.getHigh() < constant) {
				result.setHigh(1);
				result.setLow(1);
				result.setHighBest(1);
				result.setLowBest(1);
			}

			else if (constant <= a.getLow()) {
				result.setHigh(0);
				result.setLow(0);
				result.setHighBest(0);
				result.setLowBest(0);
			}

			else {
				result.setHigh(1);
				result.setLow(0);
				if (a.getHighBest() < constant) {
					result.setHighBest(1);
					result.setLowBest(1);
				} else if (constant <= a.getLowBest()) {
					result.setHighBest(0);
					result.setLowBest(0);
				} else {
					result.setHighBest(1);
					result.setLowBest(0);
				}
			}

			return result;
		} else
			return a;
	}

	public static Range lessthanequal(Range a, double constant) {

		if (a != null) {
			Range result = new Range();

			if (a.getHigh() <= constant) {
				result.setHigh(1);
				result.setLow(1);
				result.setHighBest(1);
				result.setLowBest(1);
			}

			else if (constant < a.getLow()) {
				result.setHigh(0);
				result.setLow(0);
				result.setHighBest(0);
				result.setLowBest(0);
			}

			else {
				result.setHigh(1);
				result.setLow(0);
				if (a.getHighBest() <= constant) {
					result.setHighBest(1);
					result.setLowBest(1);
				} else if (constant < a.getLowBest()) {
					result.setHighBest(0);
					result.setLowBest(0);
				} else {
					result.setHighBest(1);
					result.setLowBest(0);
				}
			}

			return result;
		} else
			return a;
	}

	private static Range max(Range a, Range b) {
		double al = a.getLow();
		double ar = a.getHigh();
		double alb = a.getLowBest();
		double arb = a.getHighBest();
		double bl = b.getLow();
		double br = b.getHigh();
		double blb = b.getLowBest();
		double brb = b.getHighBest();

		Range max = new Range();
		max.setLow(Math.max(al, bl));
		max.setHigh(Math.max(ar, br));
		max.setLowBest(Math.max(alb, blb));
		max.setHighBest(Math.max(arb, brb));
		max.setType(a.getType());

		return max;
	}

	private static Range min(Range a, Range b) {
		double al = a.getLow();
		double ar = a.getHigh();
		double alb = a.getLowBest();
		double arb = a.getHighBest();
		double bl = b.getLow();
		double br = b.getHigh();
		double blb = b.getLowBest();
		double brb = b.getHighBest();

		Range min = new Range();
		min.setLow(Math.min(al, bl));
		min.setHigh(Math.min(ar, br));
		min.setLowBest(Math.min(alb, blb));
		min.setHighBest(Math.min(arb, brb));
		min.setType(a.getType());

		return min;
	}

	private static Range mult(Range a, Range b) {
		double al = a.getLow();
		double ar = a.getHigh();
		double alb = a.getLowBest();
		double arb = a.getHighBest();
		double bl = b.getLow();
		double br = b.getHigh();
		double blb = b.getLowBest();
		double brb = b.getHighBest();

		Range mult = new Range();
		mult.setLow(al * bl);
		mult.setHigh(ar * br);
		mult.setLowBest(alb * blb);
		mult.setHighBest(arb * brb);

		return mult;
	}

	private static Range subtract(int constant, Range a) {
		double al = a.getLow();
		double ar = a.getHigh();
		double alb = a.getLowBest();
		double arb = a.getHighBest();

		Range newA = new Range();
		newA.setLow(1 - al);
		newA.setHigh(1 - ar);
		newA.setLowBest(1 - alb);
		newA.setHighBest(1 - arb);

		return newA;
	}
	
	public static Range qualify(Range range, Integer... qualifiers) {
		return range != null && range.qualifies(qualifiers) ? range : null;
	}

	public static String toString(Range a) {
		if (a == null)
			return "null";
		return "" + a.getLow() + "-" + a.getHigh() + "," + a.getLowBest() + "-" + a.getHighBest();
	}

	private double low;

	private double high;

	private double lowBest;

	private double highBest;

	private String original;

	private String type;
	
	private Integer qualifier;

	public Range() {
	}

	public Range(String original) {
		this.original = original;
		parseRange(original);
	}
	
	public void setQualifier(Integer qualifier) {
		this.qualifier = qualifier;
	}
	
	public boolean qualifies(Integer... qualifiers) {
		if (qualifier == null)
			return false;
		
		for (Integer current : qualifiers)
			if (current.intValue() == qualifier.intValue())
				return true;
		
		return false;
	}

	public double getHigh() {
		return high;
	}

	public double getHighBest() {
		return highBest;
	}

	public double getLow() {
		return low;
	}

	public double getLowBest() {
		return lowBest;
	}

	public String getOriginal() {
		return original;
	}

	public String getType() {
		return type;
	}

	private void makeRange(String range) {
		String[] hilow = range.split("-");
		if (hilow.length == 2) {
			this.low = Double.valueOf(hilow[0]).doubleValue();
			this.high = Double.valueOf(hilow[1]).doubleValue();
		} else {
			this.high = Double.valueOf(hilow[0]).doubleValue();
			this.low = Double.valueOf(hilow[0]).doubleValue();
		}
	}

	private void makeType() {
		if ((this.low <= 1) && (this.high <= 1)) {
			this.type = "boolean";
		} else {
			this.type = "number";
		}
	}

	private void parseRange(String original) {
		String[] values = original.split(",");

		try {
			// IF NOT GIVEN BEST GUESS
			if (values.length == 1) {
				makeRange(values[0]);
				this.lowBest = this.low;
				this.highBest = this.high;

			}

			// IF GIVEN RANGE FOR BEST GUESS
			else if (original.matches(".+-.+,.+-.+")) {
				makeRange(values[0]);
				String[] bestGuess = values[1].split("-");
				this.lowBest = Double.valueOf(bestGuess[0]).doubleValue();
				this.highBest = Double.valueOf(bestGuess[1]).doubleValue();
				makeType();
			}

			// IF NOT GIVEN RANGE FOR BEST GUESS
			else {
				makeRange(values[0]);
				this.lowBest = Double.valueOf(values[1]).doubleValue();
				this.highBest = this.lowBest;
			}

			makeType();

		} catch (Exception e) {
			Debug.println(e);
		}

	}

	public void setHigh(double high) {
		this.high = high;
	}

	public void setHighBest(double highBest) {
		this.highBest = highBest;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public void setLowBest(double lowBest) {
		this.lowBest = lowBest;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return "Range type " + type + " low = " + low + ", lowBest = " + lowBest + ", highbest = "+ highBest + ", high = " + high;
	}

}
