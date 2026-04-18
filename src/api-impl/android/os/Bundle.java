/*
 * Copyright (C) 2007 The Android Open Source Project
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

package android.os;

import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A mapping from String values to various Parcelable types.
 *
 */
public final class Bundle extends BaseBundle implements Cloneable, Parcelable {
	static final boolean DEBUG = false;
	public static final Bundle EMPTY;

	static final int BUNDLE_MAGIC = 0x4C444E42; // 'B' 'N' 'D' 'L'

	static {
		EMPTY = new Bundle();
		EMPTY.mMap = ArrayMap.EMPTY;
	}

	/*
	 * If mParcelledData is non-null, then mMap will be null and the
	 * data are stored as a Parcel containing a Bundle.  When the data
	 * are unparcelled, mParcelledData willbe set to null.
	 */
	/* package */
	private boolean mHasFds = false;
	private boolean mFdsKnown = true;
	private boolean mAllowFds = true;

	/**
	 * The ClassLoader used when unparcelling data from mParcelledData.
	 */
	private ClassLoader mClassLoader;

	/**
	 * Constructs a new, empty Bundle.
	 */
	public Bundle() {
		super();
		mClassLoader = getClass().getClassLoader();
	}

	/**
	 * Constructs a new, empty Bundle that uses a specific ClassLoader for
	 * instantiating Parcelable and Serializable objects.
	 *
	 * @param loader An explicit ClassLoader to use when instantiating objects
	 * inside of the Bundle.
	 */
	public Bundle(ClassLoader loader) {
		mMap = new ArrayMap<String, Object>();
		mClassLoader = loader;
	}

	/**
	 * Constructs a new, empty Bundle sized to hold the given number of
	 * elements. The Bundle will grow as needed.
	 *
	 * @param capacity the initial capacity of the Bundle
	 */
	public Bundle(int capacity) {
		mMap = new ArrayMap<String, Object>(capacity);
		mClassLoader = getClass().getClassLoader();
	}

	/**
	 * Constructs a Bundle containing a copy of the mappings from the given
	 * Bundle.
	 *
	 * @param b a Bundle to be copied.
	 */
	public Bundle(Bundle b) {

		mMap = new ArrayMap<String, Object>(b.mMap);

		mHasFds = b.mHasFds;
		mFdsKnown = b.mFdsKnown;
		mClassLoader = b.mClassLoader;
	}

	public Bundle(PersistableBundle b) {
		mMap = new ArrayMap<String, Object>(b.mMap);
		mClassLoader = getClass().getClassLoader();
	}

	/**
	 * Make a Bundle for a single key/value pair.
	 *
	 * @hide
	 */
	public static Bundle forPair(String key, String value) {
		// TODO: optimize this case.
		Bundle b = new Bundle(1);
		b.putString(key, value);
		return b;
	}

	/**
	 * TODO: optimize this later (getting just the value part of a Bundle
	 * with a single pair) once Bundle.forPair() above is implemented
	 * with a special single-value Map implementation/serialization.
	 *
	 * Note: value in single-pair Bundle may be null.
	 *
	 * @hide
	 */
	public String getPairValue() {
		int size = mMap.size();
		if (size > 1) {
			Log.w(TAG, "getPairValue() used on Bundle with multiple pairs.");
		}
		if (size == 0) {
			return null;
		}
		Object o = mMap.valueAt(0);
		try {
			return (String)o;
		} catch (ClassCastException e) {
			typeWarning("getPairValue()", o, "String", e);
			return null;
		}
	}

	/**
	 * Changes the ClassLoader this Bundle uses when instantiating objects.
	 *
	 * @param loader An explicit ClassLoader to use when instantiating objects
	 * inside of the Bundle.
	 */
	public void setClassLoader(ClassLoader loader) {
		mClassLoader = loader;
	}

	/**
	 * Return the ClassLoader currently associated with this Bundle.
	 */
	public ClassLoader getClassLoader() {
		return mClassLoader;
	}

	/**
	 * @hide
	 */
	public boolean setAllowFds(boolean allowFds) {
		boolean orig = mAllowFds;
		mAllowFds = allowFds;
		return orig;
	}

	/**
	 * Clones the current Bundle. The internal map is cloned, but the keys and
	 * values to which it refers are copied by reference.
	 */
	@Override
	public Object clone() {
		return new Bundle(this);
	}

	/**
	 * @hide
	 */
	public boolean isParcelled() {
		return false;
	}

	/**
	 * Removes all elements from the mapping of this Bundle.
	 */
	public void clear() {
		mMap.clear();
		mHasFds = false;
		mFdsKnown = true;
	}

	/**
	 * Inserts all mappings from the given Bundle into this Bundle.
	 *
	 * @param map a Bundle
	 */
	public void putAll(Bundle map) {
		mMap.putAll(map.mMap);

		// fd state is now known if and only if both bundles already knew
		mHasFds |= map.mHasFds;
		mFdsKnown = mFdsKnown && map.mFdsKnown;
	}

	/**
	 * Reports whether the bundle contains any parcelled file descriptors.
	 */
	public boolean hasFileDescriptors() { /*
	     if (!mFdsKnown) {
		 boolean fdFound = false;    // keep going until we find one or run out of data


		 // It's been unparcelled, so we need to walk the map
		 for (int i=mMap.size()-1; i>=0; i--) {
		     Object obj = mMap.valueAt(i);
		     if (obj instanceof Parcelable) {
			 if ((((Parcelable)obj).describeContents()
				 & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
			     fdFound = true;
			     break;
			 }
		     } else if (obj instanceof Parcelable[]) {
			 Parcelable[] array = (Parcelable[]) obj;
			 for (int n = array.length - 1; n >= 0; n--) {
			     if ((array[n].describeContents()
				     & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
				 fdFound = true;
				 break;
			     }
			 }
		     } else if (obj instanceof SparseArray) {
			 SparseArray<? extends Parcelable> array =
				 (SparseArray<? extends Parcelable>) obj;
			 for (int n = array.size() - 1; n >= 0; n--) {
			     if ((array.get(n).describeContents()
				     & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0) {
				 fdFound = true;
				 break;
			     }
			 }
		     } else if (obj instanceof ArrayList) {
			 ArrayList array = (ArrayList) obj;
			 // an ArrayList here might contain either Strings or
			 // Parcelables; only look inside for Parcelables
			 if ((array.size() > 0)
				 && (array.get(0) instanceof Parcelable)) {
			     for (int n = array.size() - 1; n >= 0; n--) {
				 Parcelable p = (Parcelable) array.get(n);
				 if (p != null && ((p.describeContents()
					 & Parcelable.CONTENTS_FILE_DESCRIPTOR) != 0)) {
				     fdFound = true;
				     break;
				 }
			     }
			 }
		     }
		 }

		 mHasFds = fdFound;
		 mFdsKnown = true;
	     }
	     return mHasFds;*/
		return false;
	}

	/**
	 * Inserts a byte value into the mapping of this Bundle, replacing
	 * any existing value for the given key.
	 *
	 * @param key a String, or null
	 * @param value a byte
	 */
	public void putByte(String key, byte value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a char value into the mapping of this Bundle, replacing
	 * any existing value for the given key.
	 *
	 * @param key a String, or null
	 * @param value a char, or null
	 */
	public void putChar(String key, char value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a short value into the mapping of this Bundle, replacing
	 * any existing value for the given key.
	 *
	 * @param key a String, or null
	 * @param value a short
	 */
	public void putShort(String key, short value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a float value into the mapping of this Bundle, replacing
	 * any existing value for the given key.
	 *
	 * @param key a String, or null
	 * @param value a float
	 */
	public void putFloat(String key, float value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a double value into the mapping of this Bundle, replacing
	 * any existing value for the given key.
	 *
	 * @param key a String, or null
	 * @param value a double
	 */
	public void putDouble(String key, double value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a CharSequence value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a CharSequence, or null
	 */
	public void putCharSequence(String key, CharSequence value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a Parcelable value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a Parcelable object, or null
	 */
	public void putParcelable(String key, Parcelable value) {
		mMap.put(key, value);
		mFdsKnown = false;
	}

	/**
	 * Inserts an array of Parcelable values into the mapping of this Bundle,
	 * replacing any existing value for the given key.  Either key or value may
	 * be null.
	 *
	 * @param key a String, or null
	 * @param value an array of Parcelable objects, or null
	 */
	public void putParcelableArray(String key, Parcelable[] value) {
		mMap.put(key, value);
		mFdsKnown = false;
	}

	/**
	 * Inserts a List of Parcelable values into the mapping of this Bundle,
	 * replacing any existing value for the given key.  Either key or value may
	 * be null.
	 *
	 * @param key a String, or null
	 * @param value an ArrayList of Parcelable objects, or null
	 */
	public void putParcelableArrayList(String key,
	                                   ArrayList<? extends Parcelable> value) {
		mMap.put(key, value);
		mFdsKnown = false;
	}

	/**
	 * {@hide}
	 */
	public void putParcelableList(String key, List<? extends Parcelable> value) {
		mMap.put(key, value);
		mFdsKnown = false;
	}

	/**
	 * Inserts a SparceArray of Parcelable values into the mapping of this
	 * Bundle, replacing any existing value for the given key.  Either key
	 * or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a SparseArray of Parcelable objects, or null
	 */
	public void putSparseParcelableArray(String key,
	                                     SparseArray<? extends Parcelable> value) {
		mMap.put(key, value);
		mFdsKnown = false;
	}

	/**
	 * Inserts an ArrayList<Integer> value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value an ArrayList<Integer> object, or null
	 */
	public void putIntegerArrayList(String key, ArrayList<Integer> value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts an ArrayList<String> value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value an ArrayList<String> object, or null
	 */
	public void putStringArrayList(String key, ArrayList<String> value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts an ArrayList<CharSequence> value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value an ArrayList<CharSequence> object, or null
	 */
	public void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a Serializable value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a Serializable object, or null
	 */
	public void putSerializable(String key, Serializable value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a boolean array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a boolean array object, or null
	 */
	public void putBooleanArray(String key, boolean[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a byte array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a byte array object, or null
	 */
	public void putByteArray(String key, byte[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a short array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a short array object, or null
	 */
	public void putShortArray(String key, short[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a char array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a char array object, or null
	 */
	public void putCharArray(String key, char[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a float array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a float array object, or null
	 */
	public void putFloatArray(String key, float[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a double array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a double array object, or null
	 */
	public void putDoubleArray(String key, double[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a CharSequence array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a CharSequence array object, or null
	 */
	public void putCharSequenceArray(String key, CharSequence[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a Bundle value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a Bundle object, or null
	 */
	public void putBundle(String key, Bundle value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts an {@link IBinder} value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * <p class="note">You should be very careful when using this function.  In many
	 * places where Bundles are used (such as inside of Intent objects), the Bundle
	 * can live longer inside of another process than the process that had originally
	 * created it.  In that case, the IBinder you supply here will become invalid
	 * when your process goes away, and no longer usable, even if a new process is
	 * created for you later on.</p>
	 *
	 * @param key a String, or null
	 * @param value an IBinder object, or null
	 */
	public void putBinder(String key, IBinder value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts an IBinder value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value an IBinder object, or null
	 *
	 * @deprecated
	 * @hide This is the old name of the function.
	 */
	@Deprecated
	public void putIBinder(String key, IBinder value) {
		mMap.put(key, value);
	}

	/**
	 * Returns the value associated with the given key, or (byte) 0 if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @return a byte value
	 */
	public byte getByte(String key) {
		return getByte(key, (byte)0);
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @param defaultValue Value to return if key does not exist
	 * @return a byte value
	 */
	public Byte getByte(String key, byte defaultValue) {
		Object o = mMap.get(key);
		if (o == null) {
			return defaultValue;
		}
		try {
			return (Byte)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Byte", defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * Returns the value associated with the given key, or (char) 0 if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @return a char value
	 */
	public char getChar(String key) {
		return getChar(key, (char)0);
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @param defaultValue Value to return if key does not exist
	 * @return a char value
	 */
	public char getChar(String key, char defaultValue) {
		Object o = mMap.get(key);
		if (o == null) {
			return defaultValue;
		}
		try {
			return (Character)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Character", defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * Returns the value associated with the given key, or (short) 0 if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @return a short value
	 */
	public short getShort(String key) {
		return getShort(key, (short)0);
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @param defaultValue Value to return if key does not exist
	 * @return a short value
	 */
	public short getShort(String key, short defaultValue) {
		Object o = mMap.get(key);
		if (o == null) {
			return defaultValue;
		}
		try {
			return (Short)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Short", defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * Returns the value associated with the given key, or 0.0f if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @return a float value
	 */
	public float getFloat(String key) {
		return getFloat(key, 0.0f);
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @param defaultValue Value to return if key does not exist
	 * @return a float value
	 */
	public float getFloat(String key, float defaultValue) {
		Object o = mMap.get(key);
		if (o == null) {
			return defaultValue;
		}
		try {
			return (Float)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Float", defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * Returns the value associated with the given key, or 0.0 if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @return a double value
	 */
	public double getDouble(String key) {
		return getDouble(key, 0.0);
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @param defaultValue Value to return if key does not exist
	 * @return a double value
	 */
	public double getDouble(String key, double defaultValue) {
		Object o = mMap.get(key);
		if (o == null) {
			return defaultValue;
		}
		try {
			return (Double)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Double", defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a CharSequence value, or null
	 */
	public CharSequence getCharSequence(String key) {
		final Object o = mMap.get(key);
		try {
			return (CharSequence)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "CharSequence", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String, or null
	 * @param defaultValue Value to return if key does not exist
	 * @return the CharSequence value associated with the given key, or defaultValue
	 *     if no valid CharSequence object is currently mapped to that key.
	 */
	public CharSequence getCharSequence(String key, CharSequence defaultValue) {
		final CharSequence cs = getCharSequence(key);
		return (cs == null) ? defaultValue : cs;
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a Bundle value, or null
	 */
	public Bundle getBundle(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (Bundle)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Bundle", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a Parcelable value, or null
	 */
	public <T extends Parcelable> T getParcelable(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (T)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Parcelable", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a Parcelable[] value, or null
	 */
	public Parcelable[] getParcelableArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (Parcelable[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Parcelable[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return an ArrayList<T> value, or null
	 */
	public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (ArrayList<T>)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "ArrayList", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 *
	 * @return a SparseArray of T values, or null
	 */
	public <T extends Parcelable> SparseArray<T> getSparseParcelableArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (SparseArray<T>)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "SparseArray", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a Serializable value, or null
	 */
	public Serializable getSerializable(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (Serializable)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Serializable", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return an ArrayList<String> value, or null
	 */
	public ArrayList<Integer> getIntegerArrayList(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (ArrayList<Integer>)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "ArrayList<Integer>", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return an ArrayList<String> value, or null
	 */
	public ArrayList<String> getStringArrayList(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (ArrayList<String>)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "ArrayList<String>", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return an ArrayList<CharSequence> value, or null
	 */
	public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (ArrayList<CharSequence>)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "ArrayList<CharSequence>", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a boolean[] value, or null
	 */
	public boolean[] getBooleanArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (boolean[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "byte[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a byte[] value, or null
	 */
	public byte[] getByteArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (byte[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "byte[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a short[] value, or null
	 */
	public short[] getShortArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (short[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "short[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a char[] value, or null
	 */
	public char[] getCharArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (char[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "char[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a float[] value, or null
	 */
	public float[] getFloatArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (float[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "float[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a double[] value, or null
	 */
	public double[] getDoubleArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (double[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "double[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a CharSequence[] value, or null
	 */
	public CharSequence[] getCharSequenceArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (CharSequence[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "CharSequence[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return an IBinder value, or null
	 */
	public IBinder getBinder(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (IBinder)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "IBinder", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return an IBinder value, or null
	 *
	 * @deprecated
	 * @hide This is the old name of the function.
	 */
	@Deprecated
	public IBinder getIBinder(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (IBinder)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "IBinder", e);
			return null;
		}
	}

	/**
	 * Report the nature of this Parcelable's contents
	 */
	public int describeContents() {
		int mask = 0;
		/*       if (hasFileDescriptors()) {
			   mask |= Parcelable.CONTENTS_FILE_DESCRIPTOR;
		       }*/
		return mask;
	}

	public void readFromParcel(Parcel in) throws ReflectiveOperationException {
		in.readMap(mMap, getClassLoader());
	}

	@Override
	public synchronized String toString() {
		return "Bundle[" + mMap.toString() + "]";
	}
}
