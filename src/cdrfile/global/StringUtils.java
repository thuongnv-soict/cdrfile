package cdrfile.global;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.util.Vector;

public class StringUtils extends Object {
	public static String stringValueFromVector(Vector anArray, char aChar) {
		StringBuffer buff = new StringBuffer();

		if (anArray != null) {
			buff.append(anArray.elementAt(0));

			if (buff.length() > 0) {
				int i, max;

				max = anArray.size();

				for (i = 1; i < max; i++) {
					if (aChar != '\0')
						buff.append(aChar);
					buff.append(anArray.elementAt(i));
				}
			}
		}

		return buff.toString();
	}

	public static String join(String[] strings, String delimiter) {
		StringBuffer sb = new StringBuffer();
		int stringCount = 0;
		int i;

		if (strings != null)
			stringCount = strings.length;

		if (delimiter == null) {
			delimiter = " ";
		}

		for (i = 0; i < stringCount; i++) {
			sb.append(strings[i]);
			if (i + 1 < stringCount) {
				sb.append(delimiter);
			}
		}

		return sb.toString();
	}

	/**
	 * Joins an array of strings into a single string separated by a space
	 * 
	 * @param strings
	 *            an array of strings to join
	 * @return The joined strings.
	 */
	public static String join(String[] strings) {
		return StringUtils.join(strings, " ");
	}

	public static Vector vectorFromString(String str, String delimiter) {
		Vector dynaArr = new Vector();
		int curdex = 0;
		int lastdex = 0;
		int dellength = 0;
		int strlen = 0;

		if (str != null)
			strlen = str.length();

		if (delimiter == null) {
			delimiter = " ";
		}

		dellength = delimiter.length();

		if (strlen == 0) {
			return null;
		}
		try {
			while (lastdex != -1) {
				lastdex = str.indexOf(delimiter, curdex);
				if (lastdex == -1) {
					dynaArr.addElement(str.substring(curdex, strlen));
				} else {
					dynaArr.addElement(str.substring(curdex, lastdex));
				}
				curdex = lastdex + dellength;
			}
		} catch (StringIndexOutOfBoundsException ex) {
			dynaArr.addElement(str.substring(curdex, strlen));
		}

		return dynaArr;
	}

	public static String[] split(String str) {
		return StringUtils.split(str, " ");
	}

	public static String[] split(String str, String delimiter) {
		Vector dynaArr;
		String[] strings;
		int i, max = 0;

		dynaArr = vectorFromString(str, delimiter);

		max = dynaArr.size();

		strings = new String[max];

		for (i = 0; i < max; i++) {
			strings[i] = (String) dynaArr.elementAt(i);
		}

		return strings;
	}

	public static Vector splitLines(String str) {
		Vector retVal = new Vector();
		char c;
		int i, max;
		StringBuffer curLine = new StringBuffer();

		if (str == null)
			return retVal;

		max = str.length();

		for (i = 0; i < max; i++) {
			c = str.charAt(i);

			if ((c == '\n') || (c == '\r')) {
				if (curLine.length() > 0)
					retVal.addElement(curLine.toString());

				curLine.setLength(0);
			} else {
				curLine.append(c);
			}
		}

		if (curLine.length() > 0)
			retVal.addElement(curLine.toString());

		return retVal;
	}

	private static void printElements(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			System.out.println(strings[i]);
		}
	}

	public static String removeTags(String s) {
		StringBuffer buffer = new StringBuffer();
		String retVal = s;
		int i, max;
		boolean inTag;
		char c;

		if (s == null)
			return s;

		if (s.indexOf("<") >= 0) {
			inTag = false;

			max = s.length();

			for (i = 0; i < max; i++) {
				c = s.charAt(i);

				if (c == '<')
					inTag = true;
				else if (c == '>')
					inTag = false;
				else if (!inTag) {
					if (c == '\n')
						buffer.append(' ');
					else if (c == '\t')
						buffer.append(' ');
					else if (c != '\r')
						buffer.append(c);
				}
			}

			retVal = buffer.toString();
		}

		return retVal;
	}

	final static String octalDigits = "01234567";
	final static String hexDigits = "0123456789abcdefABCDEF";
	final static String escChars = "\n\t\b\r\f\\\'\"";
	final static String unescChars = "ntbrf\\'\"";

	public static String unescape(String cstring) {
		if (cstring == null)
			return cstring;

		int len = cstring.length();
		StringBuffer sb = new StringBuffer(len);
		int val;
		int unesc;

		for (int i = 0; i < len; i++) {
			char ch = cstring.charAt(i);

			if (ch == '\\') {
				i++;
				ch = cstring.charAt(i);

				if (ch >= '0' && ch <= '7') {
					val = 0;

					for (int j = i; j - i < 3
							&& octalDigits.indexOf(ch = cstring.charAt(j)) != -1; j++) {
						val = val * 8 + (((int) ch) - '0');
					}

					ch = (char) val;
					i += 3 - 1;
				} else if (ch == 'u') {
					i++;
					val = 0;

					for (int j = i; j - i < 4; j++) {
						ch = cstring.charAt(j);

						if (hexDigits.indexOf(ch) == -1) {
							return null;
						}

						val *= 16;

						if (Character.isDigit(ch))
							val += (((int) ch) - '0');
						else if (Character.isLowerCase(ch))
							val += (((int) ch) - 'a');
						else
							val += (((int) ch) - 'A');
					}

					i += 4 - 1;
					ch = (char) val;
				} else if ((unesc = unescChars.indexOf(ch)) != -1) {
					ch = escChars.charAt(unesc);
				} else {
					// leave it
				}
			}

			sb.append(ch); // usually have some translated character to append
			// now
		}

		return sb.toString();
	}

	public static String escape(String raw) {
		if (raw == null)
			return raw;

		int max = raw.length();
		StringBuffer sb = new StringBuffer(max * 2);
		int unesc;
		int len;
		String hex;

		for (int i = 0; i < max; i++) {
			char ch = raw.charAt(i);
			int ich = (int) ch;

			if ((unesc = escChars.indexOf(ch)) != -1) {
				sb.append('\\');
				sb.append(unescChars.charAt(unesc));
			} else if (ch < ' ' || ich >= 0x7f /* || ich>0xff */) { // not
				// printable
				// or
				// Unicode
				sb.append("\\u");

				hex = Integer.toHexString(ich);
				len = hex.length();

				for (int j = len; j < 4; j++)
					sb.append('0');

				sb.append(hex);
			} else {
				sb.append(ch);
			}
		}

		return sb.toString();
	}

	public static String htmlEncode(String val) {
		StringBuffer buf = new StringBuffer(val.length() + 8);
		char c;

		for (int i = 0; i < val.length(); i++) {
			c = val.charAt(i);

			switch (c) {
			case '<':
				buf.append("&lt;");
				break;
			case '>':
				buf.append("&gt;");
				break;
			case '&':
				buf.append("&amp;");
				break;
			case '\"':
				buf.append("&quot;");
				break;
			default:
				buf.append(c);
				break;
			}
		}

		return buf.toString();
	}

	public static void main(String[] args) {

		String[] strings;

		strings = StringUtils.split("This is a test to see what is going on ");
		printElements(strings);

		System.out.println(StringUtils.join(strings));

		strings = StringUtils.split("This is a test::This is another:: space",
				"::");
		printElements(strings);

		System.out.println(StringUtils.join(strings, "::"));

		System.out.println(StringUtils.escape("\n\t\b\r\'\""));
	}

}
