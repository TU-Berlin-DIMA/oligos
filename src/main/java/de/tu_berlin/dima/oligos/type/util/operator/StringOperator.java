package de.tu_berlin.dima.oligos.type.util.operator;

import java.sql.SQLException;
import java.util.Arrays;

public class StringOperator extends AbstractOperator<String>{

	/**
	 * Increment a string by incrementing its last character in Unicode code point
	 * 
	 * @param		value		String to be incremented
	 * @return	incremented String
	 */
	@Override
	public String increment(String value) throws SQLException {
		if (value.length() == 0 || value == null) return value;
		char lastChar = value.charAt(value.length()-1);
		int cp = String.valueOf(lastChar).codePointAt(0);
		lastChar = (char) ++cp;
		return value.substring(0, value.length()-1)+lastChar;
	}

	/**
	 * Decrement a string by decrementing its last character in Unicode code point
	 * 
	 * @param		value		String to be decremented
	 * @return	decremented String
	 */
	@Override
	public String decrement(String value) throws SQLException {
		if (value.length() == 0 || value == null) return value;
		char lastChar = value.charAt(value.length()-1);
		int cp = String.valueOf(lastChar).codePointAt(0);
		lastChar = (char) --cp;
		return value.substring(0, value.length()-1)+lastChar;
	}

	/**
	 * Range for strings corresponds here to the sum of the character-wise 
	 * unicode distance. The shorter string is right-padded with blanks. 
	 * 
	 * @param 	String 	first string
	 * @param 	String 	second string
	 * @return 	long 		character-wise unicode difference
	 */
	@Override	
	public long range(String val1, String val2) {
		if (val1.compareTo(val2) == 0) return 0;
		int diffLen = val1.length() - val2.length();
		if (diffLen > 0) {
			String val2_new = padRight(val2, diffLen);
			return countDist(val1, val2_new);
		}
		else if (diffLen < 0) {
			String val1_new = padRight(val1, Math.abs(diffLen));
			return countDist(val1_new, val2);
		}
		else 
			return countDist(val1, val2);	
	}
	
	/**
	 * Sum up the Unicode code point distances for each position.
	 * 
	 * @param val1		first string
	 * @param val2		second string
	 * @return 				
	 */
	private long countDist(String val1, String val2){
		long dist = 0;
		for (int i = 0; i < val1.length(); i++)
			dist += Math.abs(String.valueOf(val1.charAt(i)).codePointAt(0) - String.valueOf(val2.charAt(i)).codePointAt(0));
		return dist;
	}
	
	/**
	 * Append <n> blanks to the right.
	 * 
	 * @param s		string to pad
	 * @param n		number of blanks
	 * @return	padded string
	 */
	private String padRight(String s, int n){
		StringBuffer str = new StringBuffer(s);
		char[] ch = new char[n];
		Arrays.fill(ch, ' ');
		str.append(ch);
		return str.toString();
	}

}
