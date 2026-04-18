/*
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

import java.text.BreakIterator;

/**
 * Utility class for manipulating cursors and selections in CharSequences.
 * A cursor is a selection where the start and end are at the same offset.
 */
public class Selection {
	private Selection() { /* cannot be instantiated */ }

	/*
	 * Retrieving the selection
	 */

	/**
	 * Return the offset of the selection anchor or cursor, or -1 if
	 * there is no selection or cursor.
	 */
	public static final int getSelectionStart(CharSequence text) {
		if (text instanceof Spanned)
			return ((Spanned)text).getSpanStart(SELECTION_START);
		else
			return -1;
	}

	/**
	 * Return the offset of the selection edge or cursor, or -1 if
	 * there is no selection or cursor.
	 */
	public static final int getSelectionEnd(CharSequence text) {
		if (text instanceof Spanned)
			return ((Spanned)text).getSpanStart(SELECTION_END);
		else
			return -1;
	}
	/*
	 * Setting the selection
	 */
	// private static int pin(int value, int min, int max) {
	//     return value < min ? 0 : (value > max ? max : value);
	// }

	/**
	 * Set the selection anchor to <code>start</code> and the selection edge
	 * to <code>stop</code>.
	 */
	public static void setSelection(Spannable text, int start, int stop) {
		// int len = text.length();
		// start = pin(start, 0, len);  XXX remove unless we really need it
		// stop = pin(stop, 0, len);
		int ostart = getSelectionStart(text);
		int oend = getSelectionEnd(text);
		if (ostart != start || oend != stop) {
			text.setSpan(SELECTION_START, start, start,
			             Spanned.SPAN_POINT_POINT | Spanned.SPAN_INTERMEDIATE);
			text.setSpan(SELECTION_END, stop, stop,
			             Spanned.SPAN_POINT_POINT);
		}
	}

	/**
	 * Move the cursor to offset <code>index</code>.
	 */
	public static final void setSelection(Spannable text, int index) {
		setSelection(text, index, index);
	}

	/**
	 * Select the entire text.
	 */
	public static final void selectAll(Spannable text) {
		setSelection(text, 0, text.length());
	}

	/**
	 * Move the selection edge to offset <code>index</code>.
	 */
	public static final void extendSelection(Spannable text, int index) {
		if (text.getSpanStart(SELECTION_END) != index)
			text.setSpan(SELECTION_END, index, index, Spanned.SPAN_POINT_POINT);
	}

	/**
	 * Remove the selection or cursor, if any, from the text.
	 */
	public static final void removeSelection(Spannable text) {
		text.removeSpan(SELECTION_START);
		text.removeSpan(SELECTION_END);
	}

	/** {@hide} */
	public static interface PositionIterator {
		public static final int DONE = BreakIterator.DONE;
		public int preceding(int position);
		public int following(int position);
	}

	/** {@hide} */
	public static boolean moveToPreceding(
	    Spannable text, PositionIterator iter, boolean extendSelection) {
		final int offset = iter.preceding(getSelectionEnd(text));
		if (offset != PositionIterator.DONE) {
			if (extendSelection) {
				extendSelection(text, offset);
			} else {
				setSelection(text, offset);
			}
		}
		return true;
	}

	/** {@hide} */
	public static boolean moveToFollowing(
	    Spannable text, PositionIterator iter, boolean extendSelection) {
		final int offset = iter.following(getSelectionEnd(text));
		if (offset != PositionIterator.DONE) {
			if (extendSelection) {
				extendSelection(text, offset);
			} else {
				setSelection(text, offset);
			}
		}
		return true;
	}

	private static final class START implements NoCopySpan {}
	private static final class END implements NoCopySpan {}

	/*
	 * Public constants
	 */
	public static final Object SELECTION_START = new START();
	public static final Object SELECTION_END = new END();
}
