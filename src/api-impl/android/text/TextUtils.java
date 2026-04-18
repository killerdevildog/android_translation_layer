/*
 * most of this file:
 *
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.text;

import com.android.internal.util.ArrayUtils;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

public class TextUtils {
	public static int getLayoutDirectionFromLocale(Locale locale) {
		return 0 /*LTR*/; // FIXME
	}

	/* split */

	private static String[] EMPTY_STRING_ARRAY = new String[] {};

	public static String[] split(String text, String expression) {
		if (text.length() == 0) {
			return EMPTY_STRING_ARRAY;
		} else {
			return text.split(expression, -1);
		}
	}

	public static String[] split(String text, Pattern pattern) {
		if (text.length() == 0) {
			return EMPTY_STRING_ARRAY;
		} else {
			return pattern.split(text, -1);
		}
	}

	/* join */

	public static String join(CharSequence delimiter, Object[] tokens) {
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for (Object token : tokens) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(token);
		}
		return sb.toString();
	}

	public static String join(CharSequence delimiter, Iterable tokens) {
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for (Object token : tokens) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(token);
		}
		return sb.toString();
	}

	public static int indexOf(CharSequence s, char ch) {
		return indexOf(s, ch, 0);
	}

	public static int indexOf(CharSequence s, char ch, int start) {
		Class<? extends CharSequence> c = s.getClass();

		if (c == String.class)
			return ((String)s).indexOf(ch, start);

		return indexOf(s, ch, start, s.length());
	}

	public static int indexOf(CharSequence s, char ch, int start, int end) {
		Class<? extends CharSequence> c = s.getClass();

		if (s instanceof GetChars || c == StringBuffer.class
		    || c == StringBuilder.class || c == String.class) {
			final int INDEX_INCREMENT = 500;
			char[] temp = obtain(INDEX_INCREMENT);

			while (start < end) {
				int segend = start + INDEX_INCREMENT;
				if (segend > end)
					segend = end;

				getChars(s, start, segend, temp, 0);

				int count = segend - start;
				for (int i = 0; i < count; i++) {
					if (temp[i] == ch) {
						recycle(temp);
						return i + start;
					}
				}

				start = segend;
			}

			recycle(temp);
			return -1;
		}

		for (int i = start; i < end; i++)
			if (s.charAt(i) == ch)
				return i;

		return -1;
	}

	public static int lastIndexOf(CharSequence s, char ch) {
		return lastIndexOf(s, ch, s.length() - 1);
	}

	public static int lastIndexOf(CharSequence s, char ch, int last) {
		Class<? extends CharSequence> c = s.getClass();

		if (c == String.class)
			return ((String)s).lastIndexOf(ch, last);

		return lastIndexOf(s, ch, 0, last);
	}

	public static int lastIndexOf(CharSequence s, char ch,
	                              int start, int last) {
		if (last < 0)
			return -1;
		if (last >= s.length())
			last = s.length() - 1;

		int end = last + 1;

		Class<? extends CharSequence> c = s.getClass();

		if (s instanceof GetChars || c == StringBuffer.class
		    || c == StringBuilder.class || c == String.class) {
			final int INDEX_INCREMENT = 500;
			char[] temp = obtain(INDEX_INCREMENT);

			while (start < end) {
				int segstart = end - INDEX_INCREMENT;
				if (segstart < start)
					segstart = start;

				getChars(s, segstart, end, temp, 0);

				int count = end - segstart;
				for (int i = count - 1; i >= 0; i--) {
					if (temp[i] == ch) {
						recycle(temp);
						return i + segstart;
					}
				}

				end = segstart;
			}

			recycle(temp);
			return -1;
		}

		for (int i = end - 1; i >= start; i--)
			if (s.charAt(i) == ch)
				return i;

		return -1;
	}

	public static int indexOf(CharSequence s, CharSequence needle) {
		return indexOf(s, needle, 0, s.length());
	}

	public static int indexOf(CharSequence s, CharSequence needle, int start) {
		return indexOf(s, needle, start, s.length());
	}

	public static int indexOf(CharSequence s, CharSequence needle,
	                          int start, int end) {
		int nlen = needle.length();
		if (nlen == 0)
			return start;

		char c = needle.charAt(0);

		for (;;) {
			start = indexOf(s, c, start);
			if (start > end - nlen) {
				break;
			}

			if (start < 0) {
				return -1;
			}

			if (regionMatches(s, start, needle, 0, nlen)) {
				return start;
			}

			start++;
		}
		return -1;
	}

	public static boolean regionMatches(CharSequence one, int toffset,
	                                    CharSequence two, int ooffset,
	                                    int len) {
		int tempLen = 2 * len;
		if (tempLen < len) {
			// Integer overflow; len is unreasonably large
			throw new IndexOutOfBoundsException();
		}
		char[] temp = obtain(tempLen);

		getChars(one, toffset, toffset + len, temp, 0);
		getChars(two, ooffset, ooffset + len, temp, len);

		boolean match = true;
		for (int i = 0; i < len; i++) {
			if (temp[i] != temp[i + len]) {
				match = false;
				break;
			}
		}

		recycle(temp);
		return match;
	}

	/* package */ static char[] obtain(int len) {
		char[] buf;

		synchronized (sLock) {
			buf = sTemp;
			sTemp = null;
		}

		if (buf == null || buf.length < len)
			buf = ArrayUtils.newUnpaddedCharArray(len);

		return buf;
	}

	/* package */ static void recycle(char[] temp) {
		if (temp.length > 1000)
			return;

		synchronized (sLock) {
			sTemp = temp;
		}
	}
	// end of unchanged from android source

	public static CharSequence join(Iterable<CharSequence> list) {
		final CharSequence delimiter = ","; // ????
		return join(delimiter, list);
	}

	public static boolean isEmpty(CharSequence str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Returns true if a and b are equal, including if they are both null.
	 * <p><i>Note: In platform versions 1.1 and earlier, this method only worked well if
	 * both the arguments were instances of String.</i></p>
	 * @param a first CharSequence to check
	 * @param b second CharSequence to check
	 * @return true if a and b are equal
	 */
	public static boolean equals(CharSequence a, CharSequence b) {
		if (a == b)
			return true;
		int length;
		if (a != null && b != null && (length = a.length()) == b.length()) {
			if (a instanceof String && b instanceof String) {
				return a.equals(b);
			} else {
				for (int i = 0; i < length; i++) {
					if (a.charAt(i) != b.charAt(i))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	public enum TruncateAt {
		START,
		MIDDLE,
		END,
		MARQUEE,
		END_SMALL
	}

	/**
	 * Returns the original text if it fits in the specified width
	 * given the properties of the specified Paint,
	 * or, if it does not fit, a truncated
	 * copy with ellipsis character added at the specified edge or center.
	 */
	public static CharSequence ellipsize(CharSequence text, TextPaint p, float avail, TruncateAt where) {
		return text;
	}

	public static void getChars(CharSequence s, int start, int end, char[] dest, int destoff) {
		Class<? extends CharSequence> c = s.getClass();
		if (c == String.class)
			((String)s).getChars(start, end, dest, destoff);
		else if (c == StringBuffer.class)
			((StringBuffer)s).getChars(start, end, dest, destoff);
		else if (c == StringBuilder.class)
			((StringBuilder)s).getChars(start, end, dest, destoff);
		else if (s instanceof GetChars)
			((GetChars)s).getChars(start, end, dest, destoff);
		else {
			for (int i = start; i < end; i++)
				dest[destoff++] = s.charAt(i);
		}
	}

	public static int getCapsMode(CharSequence cs, int off, int reqModes) {
		return 0;
	}

	private static Object sLock = new Object();

	private static char[] sTemp = null;

	public static int getTrimmedLength(CharSequence s) {
		return s.toString().trim().length();
	}

	public static String htmlEncode(String s) {
		return s;
	}

	public static CharSequence concat(CharSequence[] array) {
		StringBuilder sb = new StringBuilder();
		for (CharSequence cs : array) {
			sb.append(cs);
		}
		return sb;
	}

	public static String substring(CharSequence s, int start, int end) {
		return s.subSequence(start, end).toString();
	}

	public static boolean isDigitsOnly(CharSequence str) {
		final int len = str.length();
		for (int cp, i = 0; i < len; i += Character.charCount(cp)) {
			cp = Character.codePointAt(str, i);
			if (!Character.isDigit(cp)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * An interface for splitting strings according to rules that are opaque to the user of this
	 * interface. This also has less overhead than split, which uses regular expressions and
	 * allocates an array to hold the results.
	 *
	 * <p>The most efficient way to use this class is:
	 *
	 * <pre>
	 * // Once
	 * TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(delimiter);
	 *
	 * // Once per string to split
	 * splitter.setString(string);
	 * for (String s : splitter) {
	 *     ...
	 * }
	 * </pre>
	 */
	public interface StringSplitter extends Iterable<String> {
		public void setString(String string);
	}

	/**
	 * A simple string splitter.
	 *
	 * <p>If the final character in the string to split is the delimiter then no empty string will
	 * be returned for the empty string after that delimeter. That is, splitting <tt>"a,b,"</tt> on
	 * comma will return <tt>"a", "b"</tt>, not <tt>"a", "b", ""</tt>.
	 */
	public static class SimpleStringSplitter implements StringSplitter, Iterator<String> {
		private String mString;
		private char mDelimiter;
		private int mPosition;
		private int mLength;

		/**
		 * Initializes the splitter. setString may be called later.
		 * @param delimiter the delimeter on which to split
		 */
		public SimpleStringSplitter(char delimiter) {
			mDelimiter = delimiter;
		}

		/**
		 * Sets the string to split
		 * @param string the string to split
		 */
		public void setString(String string) {
			mString = string;
			mPosition = 0;
			mLength = mString.length();
		}

		public Iterator<String> iterator() {
			return this;
		}

		public boolean hasNext() {
			return mPosition < mLength;
		}

		public String next() {
			int end = mString.indexOf(mDelimiter, mPosition);
			if (end == -1) {
				end = mLength;
			}
			String nextString = mString.substring(mPosition, end);
			mPosition = end + 1; // Skip the delimiter.
			return nextString;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
