package com.solertium.util.portable;

import java.util.Stack;

public class PortableCalculator {
	
	private static final int MAX_DECIMAL_PLACES = 2;
	private static final boolean roundUp = true;
	
	private static boolean applyPrecisionLast = true;
	
	public static double add(double... values) {
		double result = 0;
		if (applyPrecisionLast) {
			for (double value : values)
				result += (value);
			result = format(result);
		}
		else {
			for (double value : values) {
				result += format(value);
				result = format(result);
			}
		}
		return result;
	}
	
	public static String addAsString(double... values) {
		return toString(add(values));
	}
	
	public static double multiply(double... values) {
		if (values.length == 0)
			return 0;
		if (values.length == 1)
			return values[1];
		
		double result = values[0];
		if (applyPrecisionLast) {
			for (int i = 1; i < values.length; i++)
				result *= values[i];
			result = format(result);
		}
		else {
			for (int i = 1; i < values.length; i++) {
				result *= format(values[i]);
				result = format(result);
			}
		}
		return result;
	}
	
	public static String multiplyAsString(double... values) {
		return toString(multiply(values));
	}
	
	public static double subtract(double... values) {
		if (values.length == 0)
			return 0;
		if (values.length == 1)
			return 1;
		
		double result = values[0];
		if (applyPrecisionLast) {
			for (int i = 1; i < values.length; i++)
				result -= values[i];
			result = format(result);
		}
		else {
			for (int i = 1; i < values.length; i++) {
				result -= format(values[i]);
				result = format(result);
			}
		}
		return result;
	}
	
	public static String subtractAsString(double... values) {
		return toString(subtract(values));
	}
	
	public static double divide(double... values) {
		if (values.length == 0)
			return 0;
		if (values.length == 1)
			return values[1];
		
		double result = values[0];
		if (applyPrecisionLast) {
			for (int i = 1; i < values.length; i++)
				result /= values[i];
			result = format(result);
		}
		else {
			for (int i = 1; i < values.length; i++) {
				result /= format(values[i]);
				result = format(result);
			}
		}
		return result;
	}
	
	public static String divideAsString(double... values) {
		return toString(divide(values));
	}

	private static double format(double value) {
		String d = toString(value);
		StringBuilder b = new StringBuilder();
		boolean foundDecimal = false;
		int afterDecimal = 0;
		
		boolean mustRound = false;
		
		char[] a = d.toCharArray();
		for (int i = 0; i < a.length; i++) {
			char c = a[i];
			if (!foundDecimal) {
				if (Character.isDigit(c))
					b.append(c);
				else if (c == '-')
					b.append(c);
				else if (c == '.') {
					foundDecimal = true;
					b.append(c);
				}
			}
			else if (Character.isDigit(c)) {
				if (afterDecimal < MAX_DECIMAL_PLACES) {
					if ((afterDecimal + 1) < MAX_DECIMAL_PLACES)
						b.append(c);
					else if (roundUp){
						char next;
						try {
							next = a[i+1];
						} catch (IndexOutOfBoundsException e) {
							b.append(c);
							break;
						}
						if (Character.isDigit(next)) {
							int val = Integer.parseInt(String.valueOf(next));
							
							if (val >= 5) {
								int rnd = Integer.parseInt(String.valueOf(c))+1;
								if (rnd == 10) {
									mustRound = true;
									b.append(c);
								}
								else
									b.append(rnd);
							}
							else
								b.append(c);
						}
						else
							b.append(c);
						break;
					}
					else
						break;
					afterDecimal++;
				}
				else
					break;
			}
		}
		
		if (mustRound) {
			char[] r = b.toString().toCharArray();
			
			Stack<Character> s = new Stack<Character>();
			
			for (int i = r.length-1; i >= 0; i--) {
				if (mustRound && Character.isDigit(r[i])) {
					int c = Integer.parseInt(Character.toString(r[i]));
					if (c+1 == 10)
						s.push('0');
					else {
						s.push(Character.valueOf(Integer.toString(c+1).charAt(0)));
						mustRound = false;
					}
						
				}
				else {
					s.push(r[i]);
				}
			}
			
			StringBuilder rounding = new StringBuilder();
			while (!s.isEmpty())
				rounding.append(s.pop());
			
			return Double.parseDouble(rounding.toString());
		}
		else
			return Double.parseDouble(b.toString());
	}
	
	public static String toString(double value) {
		String raw = Double.toString(value);
		String result;
		int index;
		if ((index = raw.indexOf('E')) == -1)
			result = raw;
		else {
			int amt = Integer.parseInt(raw.substring(index+1));
			if (amt < 0) {
				amt = Math.abs(amt);
				StringBuilder b = new StringBuilder();
				b.append('0');
				b.append('.');
				for (int i = 1; i < amt; i++)
					b.append('0');
				for (char c : raw.substring(0, index).toCharArray())
					if (c != '.')
						b.append(c);
				result = b.toString();
			}
			else {
				StringBuilder b = new StringBuilder();
				for (char c : raw.substring(0, index).toCharArray())
					if (c != '.')
						b.append(c);
				for (int i = 1; i < amt; i++)
					b.append('0');
				result = b.toString();
			}
		}
		
		String[] split = result.split("\\.");
		if (split.length == 1)
			return split[0] + ".00";
		else if (split.length == 2 && split[1].length() < 2) {
			StringBuilder r = new StringBuilder();
			r.append(split[0]);
			r.append('.');
			r.append(split[1]);
			r.append('0');
			
			return r.toString();
		}
		else
			return result;
	}
	
}

