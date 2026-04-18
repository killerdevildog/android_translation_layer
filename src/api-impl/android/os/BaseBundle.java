package android.os;

import android.util.ArrayMap;
import android.util.Log;
import java.util.Set;

public class BaseBundle {
	protected static final String TAG = "Bundle";
	static final boolean DEBUG = false;

	// Invariant - exactly one of mMap / mParcelledData will be null
	// (except inside a call to unparcel)

	/* package */ ArrayMap<String, Object> mMap;

	public BaseBundle() {
		mMap = new ArrayMap<String, Object>();
	}

	// Log a message if the value was non-null but not of the expected type
	void typeWarning(String key, Object value, String className,
	                 Object defaultValue, ClassCastException e) {
		StringBuilder sb = new StringBuilder();
		sb.append("Key ");
		sb.append(key);
		sb.append(" expected ");
		sb.append(className);
		sb.append(" but value was a ");
		sb.append(value.getClass().getName());
		sb.append(".  The default value ");
		sb.append(defaultValue);
		sb.append(" was returned.");
		Log.w(TAG, sb.toString());
		Log.w(TAG, "Attempt to cast generated internal exception:", e);
	}

	void typeWarning(String key, Object value, String className,
	                 ClassCastException e) {
		typeWarning(key, value, className, "<null>", e);
	}

	/**
	 * Returns true if the mapping of this Bundle is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return mMap.isEmpty();
	}

	/**
	 * Returns a Set containing the Strings used as keys in this Bundle.
	 *
	 * @return a Set of String keys
	 */
	public Set<String> keySet() {
		return mMap.keySet();
	}

	/**
	 * Returns true if the given key is contained in the mapping
	 * of this Bundle.
	 *
	 * @param key a String key
	 * @return true if the key is part of the mapping, false otherwise
	 */
	public boolean containsKey(String key) {
		return mMap.containsKey(key);
	}

	/**
	 * Removes any entry with the given key from the mapping of this Bundle.
	 *
	 * @param key a String key
	 */
	public void remove(String key) {
		mMap.remove(key);
	}

	/**
	 * Returns the entry with the given key as an object.
	 *
	 * @param key a String key
	 * @return an Object, or null
	 */
	public Object get(String key) {
		return mMap.get(key);
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a String value, or null
	 */
	public String getString(String key) {
		final Object o = mMap.get(key);
		try {
			return (String)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "String", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String, or null
	 * @param defaultValue Value to return if key does not exist
	 * @return the String value associated with the given key, or defaultValue
	 *     if no valid String object is currently mapped to that key.
	 */
	public String getString(String key, String defaultValue) {
		final String s = getString(key);
		return (s == null) ? defaultValue : s;
	}

	/**
	 * Returns the value associated with the given key, or 0L if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @return a long value
	 */
	public long getLong(String key) {
		return getLong(key, 0L);
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @param defaultValue Value to return if key does not exist
	 * @return a long value
	 */
	public long getLong(String key, long defaultValue) {
		Object o = mMap.get(key);
		if (o == null) {
			return defaultValue;
		}
		try {
			return (Long)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Long", defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * Inserts a long value into the mapping of this Bundle, replacing
	 * any existing value for the given key.
	 *
	 * @param key a String, or null
	 * @param value a long
	 */
	public void putLong(String key, long value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts an int value into the mapping of this Bundle, replacing
	 * any existing value for the given key.
	 *
	 * @param key a String, or null
	 * @param value an int, or null
	 */
	public void putInt(String key, int value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a String value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a String, or null
	 */
	public void putString(String key, String value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts an int array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value an int array object, or null
	 */
	public void putIntArray(String key, int[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a long array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a long array object, or null
	 */
	public void putLongArray(String key, long[] value) {
		mMap.put(key, value);
	}

	/**
	 * Inserts a String array value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a String array object, or null
	 */
	public void putStringArray(String key, String[] value) {
		mMap.put(key, value);
	}

	/**
	 * Returns the number of mappings contained in this Bundle.
	 *
	 * @return the number of mappings as an int.
	 */
	public int size() {
		return mMap.size();
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @param defaultValue Value to return if key does not exist
	 * @return a boolean value
	 */
	public boolean getBoolean(String key, boolean defaultValue) {
		Object o = mMap.get(key);
		System.out.println("bundle.getBoolean(" + key + ", " + defaultValue + ") called");
		/* the default for this is very scummy */
		if (key.equals("com.facebook.sdk.AutoLogAppEventsEnabled")) {
			return false;
		}
		if (o == null) {
			return defaultValue;
		}
		try {
			return (Boolean)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Boolean", defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * Returns the value associated with the given key, or false if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @return a boolean value
	 */
	public boolean getBoolean(String key) {
		if (DEBUG)
			Log.d(TAG, "Getting boolean in " + Integer.toHexString(System.identityHashCode(this)));
		return getBoolean(key, false);
	}

	/**
	 * Inserts a Boolean value into the mapping of this Bundle, replacing
	 * any existing value for the given key.  Either key or value may be null.
	 *
	 * @param key a String, or null
	 * @param value a Boolean, or null
	 */
	public void putBoolean(String key, boolean value) {
		mMap.put(key, value);
	}

	/**
	 * Returns the value associated with the given key, or defaultValue if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @param defaultValue Value to return if key does not exist
	 * @return an int value
	 */
	public int getInt(String key, int defaultValue) {
		Object o = mMap.get(key);
		if (o == null) {
			return defaultValue;
		}
		try {
			return (Integer)o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "Integer", defaultValue, e);
			return defaultValue;
		}
	}

	/**
	 * Returns the value associated with the given key, or 0 if
	 * no mapping of the desired type exists for the given key.
	 *
	 * @param key a String
	 * @return an int value
	 */
	public int getInt(String key) {
		return getInt(key, 0);
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a long[] value, or null
	 */
	public long[] getLongArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (long[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "long[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return a String[] value, or null
	 */
	public String[] getStringArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (String[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "String[]", e);
			return null;
		}
	}

	/**
	 * Returns the value associated with the given key, or null if
	 * no mapping of the desired type exists for the given key or a null
	 * value is explicitly associated with the key.
	 *
	 * @param key a String, or null
	 * @return an int[] value, or null
	 */
	public int[] getIntArray(String key) {
		Object o = mMap.get(key);
		if (o == null) {
			return null;
		}
		try {
			return (int[])o;
		} catch (ClassCastException e) {
			typeWarning(key, o, "int[]", e);
			return null;
		}
	}
}
