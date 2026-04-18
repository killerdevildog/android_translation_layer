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
/*
 * Rewritten as pure java implementation for Android Translation Layer
 */

package android.database;

import android.content.res.Resources;
import android.database.sqlite.SQLiteClosable;
import android.database.sqlite.SQLiteException;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * A buffer containing multiple cursor rows.
 * <p>
 * A {@link CursorWindow} is read-write when initially created and used locally.
 * When sent to a remote process (by writing it to a {@link Parcel}), the remote process
 * receives a read-only view of the cursor window.  Typically the cursor window
 * will be allocated by the producer, filled with data, and then sent to the
 * consumer for reading.
 * </p>
 */
public class CursorWindow extends SQLiteClosable implements Parcelable {
	// This static member will be evaluated when first used.
	private static int sCursorWindowSize = -1;

	private int startPos;
	private final String name;
	private int numColumns;
	private List<Object[]> rows = new ArrayList<>();
	private boolean all_references_released = false;

	/**
	 * Creates a new empty cursor window and gives it a name.
	 * <p>
	 * The cursor initially has no rows or columns.  Call {@link #setNumColumns(int)} to
	 * set the number of columns before adding any rows to the cursor.
	 * </p>
	 *
	 * @param name The name of the cursor window, or null if none.
	 */
	public CursorWindow(String name) {
		this(name, getCursorWindowSize());
	}

	/**
	 * Creates a new empty cursor window and gives it a name.
	 * <p>
	 * The cursor initially has no rows or columns.  Call {@link #setNumColumns(int)} to
	 * set the number of columns before adding any rows to the cursor.
	 * </p>
	 *
	 * @param name The name of the cursor window, or null if none.
	 * @param windowSizeBytes Size of cursor window in bytes.
	 * @throws IllegalArgumentException if {@code windowSizeBytes} is less than 0
	 * @throws AssertionError if created window pointer is 0
	 * <p><strong>Note:</strong> Memory is dynamically allocated as data rows are added to the
	 * window. Depending on the amount of data stored, the actual amount of memory allocated can be
	 * lower than specified size, but cannot exceed it.
	 */
	public CursorWindow(String name, long windowSizeBytes) {
		if (windowSizeBytes < 0) {
			throw new IllegalArgumentException("Window size cannot be less than 0");
		}
		startPos = 0;
		this.name = name != null && name.length() != 0 ? name : "<unnamed>";
	}

	/**
	 * Creates a new empty cursor window.
	 * <p>
	 * The cursor initially has no rows or columns.  Call {@link #setNumColumns(int)} to
	 * set the number of columns before adding any rows to the cursor.
	 * </p>
	 *
	 * @param localWindow True if this window will be used in this process only,
	 * false if it might be sent to another processes.  This argument is ignored.
	 *
	 * @deprecated There is no longer a distinction between local and remote
	 * cursor windows.  Use the {@link #CursorWindow(String)} constructor instead.
	 */
	@Deprecated
	public CursorWindow(boolean localWindow) {
		this((String)null);
	}

	/**
	 * Gets the name of this cursor window, never null.
	 * @hide
	 */
	public String getName() {
		return name;
	}

	/**
	 * Clears out the existing contents of the window, making it safe to reuse
	 * for new data.
	 * <p>
	 * The start position ({@link #getStartPosition()}), number of rows ({@link #getNumRows()}),
	 * and number of columns in the cursor are all reset to zero.
	 * </p>
	 */
	public void clear() {
		startPos = 0;
		rows.clear();
	}

	/**
	 * Gets the start position of this cursor window.
	 * <p>
	 * The start position is the zero-based index of the first row that this window contains
	 * relative to the entire result set of the {@link Cursor}.
	 * </p>
	 *
	 * @return The zero-based start position.
	 */
	public int getStartPosition() {
		return startPos;
	}

	/**
	 * Sets the start position of this cursor window.
	 * <p>
	 * The start position is the zero-based index of the first row that this window contains
	 * relative to the entire result set of the {@link Cursor}.
	 * </p>
	 *
	 * @param pos The new zero-based start position.
	 */
	public void setStartPosition(int pos) {
		startPos = pos;
	}

	/**
	 * Gets the number of rows in this window.
	 *
	 * @return The number of rows in this cursor window.
	 */
	public int getNumRows() {
		return rows.size();
	}

	/**
	 * Sets the number of columns in this window.
	 * <p>
	 * This method must be called before any rows are added to the window, otherwise
	 * it will fail to set the number of columns if it differs from the current number
	 * of columns.
	 * </p>
	 *
	 * @param columnNum The new number of columns.
	 * @return True if successful.
	 */
	public boolean setNumColumns(int columnNum) {
		if (all_references_released)
			throw new IllegalStateException("CursorWindow has no more references");
		if (columnNum < 0)
			return false;
		this.numColumns = columnNum;
		return true;
	}

	/**
	 * Allocates a new row at the end of this cursor window.
	 *
	 * @return True if successful, false if the cursor window is out of memory.
	 */
	public boolean allocRow() {
		rows.add(new Object[numColumns]);
		return true;
	}

	/**
	 * Frees the last row in this cursor window.
	 */
	public void freeLastRow() {
		rows.remove(rows.size() - 1);
	}

	/**
	 * Returns true if the field at the specified row and column index
	 * has type {@link Cursor#FIELD_TYPE_NULL}.
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if the field has type {@link Cursor#FIELD_TYPE_NULL}.
	 * @deprecated Use {@link #getType(int, int)} instead.
	 */
	@Deprecated
	public boolean isNull(int row, int column) {
		return getType(row, column) == Cursor.FIELD_TYPE_NULL;
	}

	/**
	 * Returns true if the field at the specified row and column index
	 * has type {@link Cursor#FIELD_TYPE_BLOB} or {@link Cursor#FIELD_TYPE_NULL}.
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if the field has type {@link Cursor#FIELD_TYPE_BLOB} or
	 * {@link Cursor#FIELD_TYPE_NULL}.
	 * @deprecated Use {@link #getType(int, int)} instead.
	 */
	@Deprecated
	public boolean isBlob(int row, int column) {
		int type = getType(row, column);
		return type == Cursor.FIELD_TYPE_BLOB || type == Cursor.FIELD_TYPE_NULL;
	}

	/**
	 * Returns true if the field at the specified row and column index
	 * has type {@link Cursor#FIELD_TYPE_INTEGER}.
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if the field has type {@link Cursor#FIELD_TYPE_INTEGER}.
	 * @deprecated Use {@link #getType(int, int)} instead.
	 */
	@Deprecated
	public boolean isLong(int row, int column) {
		return getType(row, column) == Cursor.FIELD_TYPE_INTEGER;
	}

	/**
	 * Returns true if the field at the specified row and column index
	 * has type {@link Cursor#FIELD_TYPE_FLOAT}.
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if the field has type {@link Cursor#FIELD_TYPE_FLOAT}.
	 * @deprecated Use {@link #getType(int, int)} instead.
	 */
	@Deprecated
	public boolean isFloat(int row, int column) {
		return getType(row, column) == Cursor.FIELD_TYPE_FLOAT;
	}

	/**
	 * Returns true if the field at the specified row and column index
	 * has type {@link Cursor#FIELD_TYPE_STRING} or {@link Cursor#FIELD_TYPE_NULL}.
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if the field has type {@link Cursor#FIELD_TYPE_STRING}
	 * or {@link Cursor#FIELD_TYPE_NULL}.
	 * @deprecated Use {@link #getType(int, int)} instead.
	 */
	@Deprecated
	public boolean isString(int row, int column) {
		int type = getType(row, column);
		return type == Cursor.FIELD_TYPE_STRING || type == Cursor.FIELD_TYPE_NULL;
	}

	/**
	 * Returns the type of the field at the specified row and column index.
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return The field type.
	 */
	public int getType(int row, int column) {
		int row_idx = row - startPos;
		if (row_idx < 0 || row_idx >= rows.size())
			return Cursor.FIELD_TYPE_NULL;
		Object value = rows.get(row_idx)[column];
		if (value instanceof String) {
			return Cursor.FIELD_TYPE_STRING;
		} else if (value instanceof Long) {
			return Cursor.FIELD_TYPE_INTEGER;
		} else if (value instanceof Double) {
			return Cursor.FIELD_TYPE_FLOAT;
		} else if (value instanceof byte[]) {
			return Cursor.FIELD_TYPE_BLOB;
		} else {
			return Cursor.FIELD_TYPE_NULL;
		}
	}

	/**
	 * Gets the value of the field at the specified row and column index as a byte array.
	 * <p>
	 * The result is determined as follows:
	 * <ul>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_NULL}, then the result
	 * is <code>null</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_BLOB}, then the result
	 * is the blob value.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_STRING}, then the result
	 * is the array of bytes that make up the internal representation of the
	 * string value.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_INTEGER} or
	 * {@link Cursor#FIELD_TYPE_FLOAT}, then a {@link SQLiteException} is thrown.</li>
	 * </ul>
	 * </p>
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return The value of the field as a byte array.
	 */
	public byte[] getBlob(int row, int column) {
		if (row - startPos < 0)
			throw new IllegalStateException("Row index out of range");
		Object value = rows.get(row - startPos)[column];
		if (value instanceof byte[] || value == null) {
			return (byte[])value;
		} else {
			throw new SQLiteException("Blob value expected");
		}
	}

	/**
	 * Gets the value of the field at the specified row and column index as a string.
	 * <p>
	 * The result is determined as follows:
	 * <ul>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_NULL}, then the result
	 * is <code>null</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_STRING}, then the result
	 * is the string value.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_INTEGER}, then the result
	 * is a string representation of the integer in decimal, obtained by formatting the
	 * value with the <code>printf</code> family of functions using
	 * format specifier <code>%lld</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_FLOAT}, then the result
	 * is a string representation of the floating-point value in decimal, obtained by
	 * formatting the value with the <code>printf</code> family of functions using
	 * format specifier <code>%g</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_BLOB}, then a
	 * {@link SQLiteException} is thrown.</li>
	 * </ul>
	 * </p>
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return The value of the field as a string.
	 */
	public String getString(int row, int column) {
		Object value = rows.get(row - startPos)[column];
		if (value == null) {
			return null;
		} else {
			return String.valueOf(value);
		}
	}

	/**
	 * Copies the text of the field at the specified row and column index into
	 * a {@link CharArrayBuffer}.
	 * <p>
	 * The buffer is populated as follows:
	 * <ul>
	 * <li>If the buffer is too small for the value to be copied, then it is
	 * automatically resized.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_NULL}, then the buffer
	 * is set to an empty string.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_STRING}, then the buffer
	 * is set to the contents of the string.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_INTEGER}, then the buffer
	 * is set to a string representation of the integer in decimal, obtained by formatting the
	 * value with the <code>printf</code> family of functions using
	 * format specifier <code>%lld</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_FLOAT}, then the buffer is
	 * set to a string representation of the floating-point value in decimal, obtained by
	 * formatting the value with the <code>printf</code> family of functions using
	 * format specifier <code>%g</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_BLOB}, then a
	 * {@link SQLiteException} is thrown.</li>
	 * </ul>
	 * </p>
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @param buffer The {@link CharArrayBuffer} to hold the string.  It is automatically
	 * resized if the requested string is larger than the buffer's current capacity.
	  */
	public void copyStringToBuffer(int row, int column, CharArrayBuffer buffer) {
		if (buffer == null) {
			throw new IllegalArgumentException("CharArrayBuffer should not be null");
		}
		String result = String.valueOf(rows.get(row - startPos)[column]);
		if (buffer.data == null || buffer.data.length < result.length())
			buffer.data = new char[Math.max(64, result.length())];
		result.getChars(0, result.length(), buffer.data, 0);
		buffer.sizeCopied = result.length();
	}

	/**
	 * Gets the value of the field at the specified row and column index as a <code>long</code>.
	 * <p>
	 * The result is determined as follows:
	 * <ul>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_NULL}, then the result
	 * is <code>0L</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_STRING}, then the result
	 * is the value obtained by parsing the string value with <code>strtoll</code>.
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_INTEGER}, then the result
	 * is the <code>long</code> value.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_FLOAT}, then the result
	 * is the floating-point value converted to a <code>long</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_BLOB}, then a
	 * {@link SQLiteException} is thrown.</li>
	 * </ul>
	 * </p>
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return The value of the field as a <code>long</code>.
	 */
	public long getLong(int row, int column) {
		long result = 0L;
		Object object = rows.get(row - startPos)[column];
		if (object instanceof Long)
			result = (Long)object;
		else if (object instanceof String)
			try {
				result = Long.parseLong((String)object);
			} catch (NumberFormatException e) {
				result = 0L;
			}
		else if (object instanceof Double)
			result = ((Double)object).longValue();
		else if (object == null)
			result = 0L;
		else
			throw new SQLiteException("Unexpected object type for getLong: " + object.getClass().getName());
		return result;
	}

	/**
	 * Gets the value of the field at the specified row and column index as a
	 * <code>double</code>.
	 * <p>
	 * The result is determined as follows:
	 * <ul>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_NULL}, then the result
	 * is <code>0.0</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_STRING}, then the result
	 * is the value obtained by parsing the string value with <code>strtod</code>.
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_INTEGER}, then the result
	 * is the integer value converted to a <code>double</code>.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_FLOAT}, then the result
	 * is the <code>double</code> value.</li>
	 * <li>If the field is of type {@link Cursor#FIELD_TYPE_BLOB}, then a
	 * {@link SQLiteException} is thrown.</li>
	 * </ul>
	 * </p>
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return The value of the field as a <code>double</code>.
	 */
	public double getDouble(int row, int column) {
		Object value = rows.get(row - startPos)[column];
		if (value instanceof Double)
			return (Double)value;
		else if (value instanceof String)
			try {
				return Double.parseDouble((String)value);
			} catch (NumberFormatException e) {
				return 0.0;
			}
		else if (value instanceof Long)
			return ((Long)value).doubleValue();
		else if (value == null)
			return 0.0;
		else
			throw new SQLiteException("Unexpected object type for getDouble: " + value.getClass().getName());
	}

	/**
	 * Gets the value of the field at the specified row and column index as a
	 * <code>short</code>.
	 * <p>
	 * The result is determined by invoking {@link #getLong} and converting the
	 * result to <code>short</code>.
	 * </p>
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return The value of the field as a <code>short</code>.
	 */
	public short getShort(int row, int column) {
		return (short)getLong(row, column);
	}

	/**
	 * Gets the value of the field at the specified row and column index as an
	 * <code>int</code>.
	 * <p>
	 * The result is determined by invoking {@link #getLong} and converting the
	 * result to <code>int</code>.
	 * </p>
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return The value of the field as an <code>int</code>.
	 */
	public int getInt(int row, int column) {
		return (int)getLong(row, column);
	}

	/**
	 * Gets the value of the field at the specified row and column index as a
	 * <code>float</code>.
	 * <p>
	 * The result is determined by invoking {@link #getDouble} and converting the
	 * result to <code>float</code>.
	 * </p>
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return The value of the field as an <code>float</code>.
	 */
	public float getFloat(int row, int column) {
		return (float)getDouble(row, column);
	}

	/**
	 * Copies a byte array into the field at the specified row and column index.
	 *
	 * @param value The value to store.
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if successful.
	 */
	public boolean putBlob(byte[] value, int row, int column) {
		rows.get(row - startPos)[column] = value;
		return true;
	}

	/**
	 * Copies a string into the field at the specified row and column index.
	 *
	 * @param value The value to store.
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if successful.
	 */
	public boolean putString(String value, int row, int column) {
		int row_idx = row - startPos;
		if (row_idx < 0 || row_idx >= rows.size())
			return false;
		Object[] row_array = rows.get(row_idx);
		if (column < 0 || column >= row_array.length)
			return false;
		row_array[column] = value;
		return true;
	}

	/**
	 * Puts a long integer into the field at the specified row and column index.
	 *
	 * @param value The value to store.
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if successful.
	 */
	public boolean putLong(long value, int row, int column) {
		rows.get(row - startPos)[column] = value;
		return true;
	}

	/**
	 * Puts a double-precision floating point value into the field at the
	 * specified row and column index.
	 *
	 * @param value The value to store.
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if successful.
	 */
	public boolean putDouble(double value, int row, int column) {
		rows.get(row - startPos)[column] = value;
		return true;
	}

	/**
	 * Puts a null value into the field at the specified row and column index.
	 *
	 * @param row The zero-based row index.
	 * @param column The zero-based column index.
	 * @return True if successful.
	 */
	public boolean putNull(int row, int column) {
		rows.get(row - startPos)[column] = null;
		return true;
	}

	public int describeContents() {
		return 0;
	}

	@Override
	protected void onAllReferencesReleased() {
		all_references_released = true;
	}

	private static int getCursorWindowSize() {
		if (sCursorWindowSize < 0) {
			// The cursor window size. resource xml file specifies the value in kB.
			// convert it to bytes here by multiplying with 1024.
			sCursorWindowSize = Resources.getSystem().getInteger(
						com.android.internal.R.integer.config_cursorWindowSize)
			                  * 1024;
		}
		return sCursorWindowSize;
	}

	@Override
	public String toString() {
		return getName() + " {" + rows + "}";
	}
}
