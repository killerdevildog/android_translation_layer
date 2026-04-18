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

package android.content.res;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
// import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.icu.text.PluralRules;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pools.SynchronizedPool;
import android.util.Slog;
import android.util.TypedValue;
import android.util.Xml;
import com.android.internal.util.GrowingArrayUtils;
import com.android.internal.util.XmlUtils;
// import android.view.DisplayAdjustments;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class Movie {}

/**
 * Class for accessing an application's resources.  This sits on top of the
 * asset manager of the application (accessible through {@link #getAssets}) and
 * provides a high-level API for getting typed data from the assets.
 *
 * <p>The Android resource system keeps track of all non-code assets associated with an
 * application. You can use this class to access your application's resources. You can generally
 * acquire the {@link android.content.res.Resources} instance associated with your application
 * with {@link android.content.Context#getResources getResources()}.</p>
 *
 * <p>The Android SDK tools compile your application's resources into the application binary
 * at build time.  To use a resource, you must install it correctly in the source tree (inside
 * your project's {@code res/} directory) and build your application.  As part of the build
 * process, the SDK tools generate symbols for each resource, which you can use in your application
 * code to access the resources.</p>
 *
 * <p>Using application resources makes it easy to update various characteristics of your
 * application without modifying code, and&mdash;by providing sets of alternative
 * resources&mdash;enables you to optimize your application for a variety of device configurations
 * (such as for different languages and screen sizes). This is an important aspect of developing
 * Android applications that are compatible on different types of devices.</p>
 *
 * <p>For more information about using resources, see the documentation about <a
 * href="{@docRoot}guide/topics/resources/index.html">Application Resources</a>.</p>
 */
public class Resources {
	static final String TAG = "Resources";
	private static final boolean DEBUG_LOAD = false;
	private static final boolean DEBUG_CONFIG = false;
	private static final boolean DEBUG_ATTRIBUTES_CACHE = false;
	private static final boolean TRACE_FOR_PRELOAD = false;
	private static final boolean TRACE_FOR_MISS_PRELOAD = false;

	private static final int ID_OTHER = 0x01000004;

	private static final Object sSync = new Object();
	/*package*/ static Resources mSystem = null;

	// Information about preloaded resources.  Note that they are not
	// protected by a lock, because while preloading in zygote we are all
	// single-threaded, and after that these are immutable.
	private static final LongSparseArray<Drawable.ConstantState>[] sPreloadedDrawables;
	private static final LongSparseArray<Drawable.ConstantState> sPreloadedColorDrawables = new LongSparseArray<Drawable.ConstantState>();
	private static final LongSparseArray<android.content.res.ConstantState<ComplexColor>> sPreloadedComplexColors = new LongSparseArray<>();

	// Pool of TypedArrays targeted to this Resources object.
	final SynchronizedPool<TypedArray> mTypedArrayPool = new SynchronizedPool<>(5);

	private static boolean sPreloaded;
	private static int sPreloadedDensity;

	// These are protected by mAccessLock.

	/*package*/ final Object mAccessLock = new Object();
	/*package*/ final Configuration mTmpConfig = new Configuration();
	/*package*/ TypedValue mTmpValue = new TypedValue();
	/*package*/ Object mDrawableCache = new HashMap<Long, WeakReference<Drawable.ConstantState>>(0);
	private final ConfigurationBoundResourceCache<ComplexColor> mComplexColorCache = new ConfigurationBoundResourceCache<>(this);
	/*package*/ final LongSparseArray<WeakReference<Drawable.ConstantState>> mColorDrawableCache = new LongSparseArray<WeakReference<Drawable.ConstantState>>(0);
	/*package*/ boolean mPreloading;

	/*package*/ TypedArray mCachedStyledAttributes = null;
	RuntimeException mLastRetrievedAttrs = null;

	private int mLastCachedXmlBlockIndex = -1;
	private final int[] mCachedXmlBlockIds = {0, 0, 0, 0};
	private final XmlBlock[] mCachedXmlBlocks = new XmlBlock[4];

	/*package*/ final AssetManager mAssets;
	private final Configuration mConfiguration = new Configuration();
	/*package*/ final DisplayMetrics mMetrics = new DisplayMetrics();
	private PluralRules mPluralRule;

	private CompatibilityInfo mCompatibilityInfo = CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO;
	private WeakReference<IBinder> mToken;

	static {
		sPreloadedDrawables = new LongSparseArray[2];
		sPreloadedDrawables[0] = new LongSparseArray<Drawable.ConstantState>();
		sPreloadedDrawables[1] = new LongSparseArray<Drawable.ConstantState>();
	}

	/**
	 * @hide
	 */
	public static int selectDefaultTheme(int curTheme, int targetSdkVersion) {
		return selectSystemTheme(curTheme, targetSdkVersion,
		                         com.android.internal.R.style.Theme,
		                         com.android.internal.R.style.Theme_Holo,
		                         com.android.internal.R.style.Theme_DeviceDefault);
	}

	/**
	 * @hide
	 */
	public static int selectSystemTheme(int curTheme, int targetSdkVersion,
	                                    int orig, int holo, int deviceDefault) {
		if (curTheme != 0) {
			return curTheme;
		}
		if (targetSdkVersion < Build.VERSION_CODES.HONEYCOMB) {
			return orig;
		}
		if (targetSdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return holo;
		}
		return deviceDefault;
	}

	/**
	 * This exception is thrown by the resource APIs when a requested resource
	 * can not be found.
	 */
	public static class NotFoundException extends RuntimeException {
		public NotFoundException() {
		}

		public NotFoundException(String name) {
			super(name);
		}
	}

	/**
	 * Create a new Resources object on top of an existing set of assets in an
	 * AssetManager.
	 *
	 * @param assets Previously created AssetManager.
	 * @param metrics Current display metrics to consider when
	 *                selecting/computing resource values.
	 * @param config Desired device configuration to consider when
	 *               selecting/computing resource values (optional).
	 */
	public Resources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
		this(assets, metrics, config, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
	}

	/**
	 * Creates a new Resources object with CompatibilityInfo.
	 *
	 * @param assets Previously created AssetManager.
	 * @param metrics Current display metrics to consider when
	 *                selecting/computing resource values.
	 * @param config Desired device configuration to consider when
	 *               selecting/computing resource values (optional).
	 * @param compatInfo this resource's compatibility info. Must not be null.
	 * @param token The Activity token for determining stack affiliation. Usually null.
	 * @hide
	 */
	public Resources(AssetManager assets, DisplayMetrics metrics, Configuration config,
	                 CompatibilityInfo compatInfo, IBinder token) {
		mAssets = assets;
		mMetrics.setToDefaults();
		if (compatInfo != null) {
			mCompatibilityInfo = compatInfo;
		}
		mToken = new WeakReference<IBinder>(token);
		updateConfiguration(config, metrics);
		//        assets.ensureStringBlocks();
	}

	public void applyPackageQuirks(int minSdkVersion) {
		// Apps that target >= 23 expect mDrawableCache to be a ThemedResourceCache while other apps expect a Map
		if (Build.VERSION.SDK_INT >= 23 || minSdkVersion >= 23) {
			mDrawableCache = new ThemedResourceCache<Drawable>() {
				@Override
				protected boolean shouldInvalidateEntry(Drawable entry, int configChanges) {
					// TODO Auto-generated method stub
					throw new UnsupportedOperationException("Unimplemented method 'shouldInvalidateEntry'");
				}
			};
		}
	}

	/**
	 * Return a global shared Resources object that provides access to only
	 * system resources (no application resources), and is not configured for
	 * the current screen (can not use dimension units, does not change based
	 * on orientation, etc).
	 */
	public static Resources getSystem() {
		return Context.this_application.getResources();
		//synchronized (sSync) {
		//	Resources ret = mSystem;
		//	if (ret == null) {
		//		ret = new Resources();
		//		mSystem = ret;
		//	}
		//
		//	return ret;
		//}
	}

	/**
	 * Return the string value associated with a particular resource ID.  The
	 * returned object will be a String if this is a plain string; it will be
	 * some other type of CharSequence if it is styled.
	 * {@more}
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return CharSequence The string data associated with the resource, plus
	 *         possibly styled text information.
	 */
	public CharSequence getText(int id) throws NotFoundException {
		CharSequence res = mAssets.getResourceText(id);
		if (res != null) {
			return res;
		}
		throw new NotFoundException("String resource ID #0x" + Integer.toHexString(id));
	}

	/**
	 * Returns the character sequence necessary for grammatically correct pluralization
	 * of the given resource ID for the given quantity.
	 * Note that the character sequence is selected based solely on grammatical necessity,
	 * and that such rules differ between languages. Do not assume you know which string
	 * will be returned for a given quantity. See
	 * <a href="{@docRoot}guide/topics/resources/string-resource.html#Plurals">String Resources</a>
	 * for more detail.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 * @param quantity The number used to get the correct string for the current language's
	 *           plural rules.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return CharSequence The string data associated with the resource, plus
	 *         possibly styled text information.
	 */
	public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
		PluralRules rule = getPluralRule();
		CharSequence res = mAssets.getResourceBagText(id,
		                                              attrForQuantityCode(rule.select(quantity)));
		if (res != null) {
			return res;
		}
		res = mAssets.getResourceBagText(id, ID_OTHER);
		if (res != null) {
			return res;
		}
		throw new NotFoundException("Plural resource ID #0x" + Integer.toHexString(id) + " quantity=" + quantity + " item=" + rule.select(quantity));
	}

	private PluralRules getPluralRule() {
		synchronized (sSync) {
			if (mPluralRule == null) {
				mPluralRule = PluralRules.forLocale(mConfiguration.locale);
			}
			return mPluralRule;
		}
	}

	private static int attrForQuantityCode(String quantityCode) {
		switch (quantityCode) {
			case PluralRules.KEYWORD_ZERO:
				return 0x01000005;
			case PluralRules.KEYWORD_ONE:
				return 0x01000006;
			case PluralRules.KEYWORD_TWO:
				return 0x01000007;
			case PluralRules.KEYWORD_FEW:
				return 0x01000008;
			case PluralRules.KEYWORD_MANY:
				return 0x01000009;
			default:
				return ID_OTHER;
		}
	}

	private static String stringForQuantityCode(String quantityCode) {
		switch (quantityCode) {
			case PluralRules.KEYWORD_ZERO:
				return "zero";
			case PluralRules.KEYWORD_ONE:
				return "one";
			case PluralRules.KEYWORD_TWO:
				return "two";
			case PluralRules.KEYWORD_FEW:
				return "few";
			case PluralRules.KEYWORD_MANY:
				return "many";
			default:
				return "other";
		}
	}

	/**
	 * Return the string value associated with a particular resource ID.  It
	 * will be stripped of any styled text information.
	 * {@more}
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return String The string data associated with the resource,
	 * stripped of styled text information.
	 */
	public String getString(int id) throws NotFoundException {
		CharSequence res = getText(id);
		if (res != null) {
			return res.toString();
		}
		throw new NotFoundException("String resource ID #0x" + Integer.toHexString(id));
	}

	/**
	 * Return the string value associated with a particular resource ID,
	 * substituting the format arguments as defined in {@link java.util.Formatter}
	 * and {@link java.lang.String#format}. It will be stripped of any styled text
	 * information.
	 * {@more}
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @param formatArgs The format arguments that will be used for substitution.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return String The string data associated with the resource,
	 * stripped of styled text information.
	 */
	public String getString(int id, Object... formatArgs) throws NotFoundException {
		String raw = getString(id);
		return String.format(mConfiguration.locale, raw, formatArgs);
	}

	/**
	 * Formats the string necessary for grammatically correct pluralization
	 * of the given resource ID for the given quantity, using the given arguments.
	 * Note that the string is selected based solely on grammatical necessity,
	 * and that such rules differ between languages. Do not assume you know which string
	 * will be returned for a given quantity. See
	 * <a href="{@docRoot}guide/topics/resources/string-resource.html#Plurals">String Resources</a>
	 * for more detail.
	 *
	 * <p>Substitution of format arguments works as if using
	 * {@link java.util.Formatter} and {@link java.lang.String#format}.
	 * The resulting string will be stripped of any styled text information.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 * @param quantity The number used to get the correct string for the current language's
	 *           plural rules.
	 * @param formatArgs The format arguments that will be used for substitution.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return String The string data associated with the resource,
	 * stripped of styled text information.
	 */
	public String getQuantityString(int id, int quantity, Object... formatArgs)
	    throws NotFoundException {
		String raw = getQuantityText(id, quantity).toString();
		return String.format(mConfiguration.locale, raw, formatArgs);
	}

	/**
	 * Returns the string necessary for grammatically correct pluralization
	 * of the given resource ID for the given quantity.
	 * Note that the string is selected based solely on grammatical necessity,
	 * and that such rules differ between languages. Do not assume you know which string
	 * will be returned for a given quantity. See
	 * <a href="{@docRoot}guide/topics/resources/string-resource.html#Plurals">String Resources</a>
	 * for more detail.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 * @param quantity The number used to get the correct string for the current language's
	 *           plural rules.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return String The string data associated with the resource,
	 * stripped of styled text information.
	 */
	public String getQuantityString(int id, int quantity) throws NotFoundException {
		return getQuantityText(id, quantity).toString();
	}

	/**
	 * Return the string value associated with a particular resource ID.  The
	 * returned object will be a String if this is a plain string; it will be
	 * some other type of CharSequence if it is styled.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @param def The default CharSequence to return.
	 *
	 * @return CharSequence The string data associated with the resource, plus
	 *         possibly styled text information, or def if id is 0 or not found.
	 */
	public CharSequence getText(int id, CharSequence def) {
		CharSequence res = id != 0 ? mAssets.getResourceText(id) : null;
		return res != null ? res : def;
	}

	/**
	 * Return the styled text array associated with a particular resource ID.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return The styled text array associated with the resource.
	 */
	public CharSequence[] getTextArray(int id) throws NotFoundException {
		CharSequence[] res = mAssets.getResourceTextArray(id);
		if (res != null) {
			return res;
		}
		throw new NotFoundException("Text array resource ID #0x" + Integer.toHexString(id));
	}

	/**
	 * Return the string array associated with a particular resource ID.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return The string array associated with the resource.
	 */
	public String[] getStringArray(int id) throws NotFoundException {
		String[] res = mAssets.getResourceStringArray(id);
		if (res != null) {
			return res;
		}
		throw new NotFoundException("String array resource ID #0x" + Integer.toHexString(id));
	}

	/**
	 * Return the int array associated with a particular resource ID.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return The int array associated with the resource.
	 */
	public int[] getIntArray(int id) throws NotFoundException {
		int[] res = mAssets.getArrayIntResource(id);
		if (res != null) {
			return res;
		}
		throw new NotFoundException("Int array resource ID #0x" + Integer.toHexString(id));
	}

	/**
	 * Return an array of heterogeneous values.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return Returns a TypedArray holding an array of the array values.
	 * Be sure to call {@link TypedArray#recycle() TypedArray.recycle()}
	 * when done with it.
	 */
	public TypedArray obtainTypedArray(int id) throws NotFoundException {
		int len = mAssets.getArraySize(id);
		if (len < 0) {
			throw new NotFoundException("Array resource ID #0x" + Integer.toHexString(id));
		}

		TypedArray array = TypedArray.obtain(this, len);
		array.mLength = mAssets.retrieveArray(id, array.mData);
		array.mIndices[0] = 0;

		return array;
	}

	/**
	 * Retrieve a dimensional for a particular resource ID.  Unit
	 * conversions are based on the current {@link DisplayMetrics} associated
	 * with the resources.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @return Resource dimension value multiplied by the appropriate
	 * metric.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @see #getDimensionPixelOffset
	 * @see #getDimensionPixelSize
	 */
	public float getDimension(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			TypedValue value = mTmpValue;
			if (value == null) {
				mTmpValue = value = new TypedValue();
			}
			getValue(id, value, true);
			if (value.type == TypedValue.TYPE_DIMENSION) {
				return TypedValue.complexToDimension(value.data, mMetrics);
			}
			throw new NotFoundException(
			    "Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
		}
	}

	/**
	 * Retrieve a dimensional for a particular resource ID for use
	 * as an offset in raw pixels.  This is the same as
	 * {@link #getDimension}, except the returned value is converted to
	 * integer pixels for you.  An offset conversion involves simply
	 * truncating the base value to an integer.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @return Resource dimension value multiplied by the appropriate
	 * metric and truncated to integer pixels.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @see #getDimension
	 * @see #getDimensionPixelSize
	 */
	public int getDimensionPixelOffset(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			TypedValue value = mTmpValue;
			if (value == null) {
				mTmpValue = value = new TypedValue();
			}
			getValue(id, value, true);
			if (value.type == TypedValue.TYPE_DIMENSION) {
				return TypedValue.complexToDimensionPixelOffset(
				    value.data, mMetrics);
			}
			throw new NotFoundException(
			    "Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
		}
	}

	/**
	 * Retrieve a dimensional for a particular resource ID for use
	 * as a size in raw pixels.  This is the same as
	 * {@link #getDimension}, except the returned value is converted to
	 * integer pixels for use as a size.  A size conversion involves
	 * rounding the base value, and ensuring that a non-zero base value
	 * is at least one pixel in size.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @return Resource dimension value multiplied by the appropriate
	 * metric and truncated to integer pixels.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @see #getDimension
	 * @see #getDimensionPixelOffset
	 */
	public int getDimensionPixelSize(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			TypedValue value = mTmpValue;
			if (value == null) {
				mTmpValue = value = new TypedValue();
			}
			getValue(id, value, true);
			if (value.type == TypedValue.TYPE_DIMENSION) {
				return TypedValue.complexToDimensionPixelSize(
				    value.data, mMetrics);
			}
			throw new NotFoundException(
			    "Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
		}
	}

	/**
	 * Retrieve a fractional unit for a particular resource ID.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 * @param base The base value of this fraction.  In other words, a
	 *             standard fraction is multiplied by this value.
	 * @param pbase The parent base value of this fraction.  In other
	 *             words, a parent fraction (nn%p) is multiplied by this
	 *             value.
	 *
	 * @return Attribute fractional value multiplied by the appropriate
	 * base value.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 */
	public float getFraction(int id, int base, int pbase) {
		synchronized (mAccessLock) {
			TypedValue value = mTmpValue;
			if (value == null) {
				mTmpValue = value = new TypedValue();
			}
			getValue(id, value, true);
			if (value.type == TypedValue.TYPE_FRACTION) {
				return TypedValue.complexToFraction(value.data, base, pbase);
			}
			throw new NotFoundException(
			    "Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
		}
	}

	/**
	 * Return a drawable object associated with a particular resource ID.
	 * Various types of objects will be returned depending on the underlying
	 * resource -- for example, a solid color, PNG image, scalable image, etc.
	 * The Drawable API hides these implementation details.
	 *
	 * <p class="note"><strong>Note:</strong> Prior to
	 * {@link android.os.Build.VERSION_CODES#JELLY_BEAN}, this function
	 * would not correctly retrieve the final configuration density when
	 * the resource ID passed here is an alias to another Drawable resource.
	 * This means that if the density configuration of the alias resource
	 * is different than the actual resource, the density of the returned
	 * Drawable would be incorrect, resulting in bad scaling.  To work
	 * around this, you can instead retrieve the Drawable through
	 * {@link TypedArray#getDrawable TypedArray.getDrawable}.  Use
	 * {@link android.content.Context#obtainStyledAttributes(int[])
	 * Context.obtainStyledAttributes} with
	 * an array containing the resource ID of interest to create the TypedArray.</p>
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return Drawable An object that can be used to draw this resource.
	 */
	public Drawable getDrawable(int id) throws NotFoundException {
		return getDrawable(id, null);
	}

	public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
		TypedValue value;
		synchronized (mAccessLock) {
			value = mTmpValue;
			if (value == null) {
				value = new TypedValue();
			} else {
				mTmpValue = null;
			}
			getValue(id, value, true);
		}
		Drawable res = null;
		try {
			res = loadDrawable(value, id, theme);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		synchronized (mAccessLock) {
			if (mTmpValue == null) {
				mTmpValue = value;
			}
		}
		return res;
	}

	/**
	 * Return a drawable object associated with a particular resource ID for the
	 * given screen density in DPI. This will set the drawable's density to be
	 * the device's density multiplied by the ratio of actual drawable density
	 * to requested density. This allows the drawable to be scaled up to the
	 * correct size if needed. Various types of objects will be returned
	 * depending on the underlying resource -- for example, a solid color, PNG
	 * image, scalable image, etc. The Drawable API hides these implementation
	 * details.
	 *
	 * @param id The desired resource identifier, as generated by the aapt tool.
	 *            This integer encodes the package, type, and resource entry.
	 *            The value 0 is an invalid identifier.
	 * @param density the desired screen density indicated by the resource as
	 *            found in {@link DisplayMetrics}.
	 * @throws NotFoundException Throws NotFoundException if the given ID does
	 *             not exist.
	 * @return Drawable An object that can be used to draw this resource.
	 */
	public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
		TypedValue value;
		synchronized (mAccessLock) {
			value = mTmpValue;
			if (value == null) {
				value = new TypedValue();
			} else {
				mTmpValue = null;
			}
			getValueForDensity(id, density, value, true);

			/*
			 * Pretend the requested density is actually the display density. If
			 * the drawable returned is not the requested density, then force it
			 * to be scaled later by dividing its density by the ratio of
			 * requested density to actual device density. Drawables that have
			 * undefined density or no density don't need to be handled here.
			 */
			if (value.density > 0 && value.density != TypedValue.DENSITY_NONE) {
				if (value.density == density) {
					value.density = mMetrics.densityDpi;
				} else {
					value.density = (value.density * mMetrics.densityDpi) / density;
				}
			}
		}

		Drawable res = loadDrawable(value, id);
		synchronized (mAccessLock) {
			if (mTmpValue == null) {
				mTmpValue = value;
			}
		}
		return res;
	}

	/**
	 * Return a movie object associated with the particular resource ID.
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 */
	public Movie getMovie(int id) throws NotFoundException { /*
	     InputStream is = openRawResource(id);
	     Movie movie = Movie.decodeStream(is);
	     try {
		 is.close();
	     }
	     catch (java.io.IOException e) {
		 // don't care, since the return value is valid
	     }
	     return movie;
	 */
		return null;
	}

	/**
	 * Return a color integer associated with a particular resource ID.
	 * If the resource holds a complex
	 * {@link android.content.res.ColorStateList}, then the default color from
	 * the set is returned.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return Returns a single color value in the form 0xAARRGGBB.
	 */
	public int getColor(int id) throws NotFoundException {
		return getColor(id, null);
	}

	public int getColor(int id, Theme theme) throws NotFoundException {
		TypedValue value;
		synchronized (mAccessLock) {
			value = mTmpValue;
			if (value == null) {
				value = new TypedValue();
			}
			getValue(id, value, true);
			if (value.type >= TypedValue.TYPE_FIRST_INT && value.type <= TypedValue.TYPE_LAST_INT) {
				mTmpValue = value;
				return value.data;
			} else if (value.type != TypedValue.TYPE_STRING) {
				throw new NotFoundException(
				    "Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
			}
			mTmpValue = null;
		}
		ColorStateList csl = loadColorStateList(value, id, theme);
		synchronized (mAccessLock) {
			if (mTmpValue == null) {
				mTmpValue = value;
			}
		}
		return csl.getDefaultColor();
	}

	/**
	 * Return a color state list associated with a particular resource ID.  The
	 * resource may contain either a single raw color value, or a complex
	 * {@link android.content.res.ColorStateList} holding multiple possible colors.
	 *
	 * @param id The desired resource identifier of a {@link ColorStateList},
	 *        as generated by the aapt tool. This integer encodes the package, type, and resource
	 *        entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return Returns a ColorStateList object containing either a single
	 * solid color or multiple colors that can be selected based on a state.
	 */
	public ColorStateList getColorStateList(int id) throws NotFoundException {
		return getColorStateList(id, null);
	}

	/* FIXME use the theme */
	public ColorStateList getColorStateList(int id, Theme theme) throws NotFoundException {
		TypedValue value;
		synchronized (mAccessLock) {
			value = mTmpValue;
			if (value == null) {
				value = new TypedValue();
			} else {
				mTmpValue = null;
			}
			getValue(id, value, true);
		}
		ColorStateList res = loadColorStateList(value, id, theme);
		synchronized (mAccessLock) {
			if (mTmpValue == null) {
				mTmpValue = value;
			}
		}
		return res;
	}

	/**
	 * Return a boolean associated with a particular resource ID.  This can be
	 * used with any integral resource value, and will return true if it is
	 * non-zero.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return Returns the boolean value contained in the resource.
	 */
	public boolean getBoolean(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			TypedValue value = mTmpValue;
			if (value == null) {
				mTmpValue = value = new TypedValue();
			}
			getValue(id, value, true);
			if (value.type >= TypedValue.TYPE_FIRST_INT && value.type <= TypedValue.TYPE_LAST_INT) {
				return value.data != 0;
			}
			throw new NotFoundException(
			    "Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
		}
	}

	/**
	 * Return an integer associated with a particular resource ID.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return Returns the integer value contained in the resource.
	 */
	public int getInteger(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			TypedValue value = mTmpValue;
			if (value == null) {
				mTmpValue = value = new TypedValue();
			}
			getValue(id, value, true);
			if (value.type >= TypedValue.TYPE_FIRST_INT && value.type <= TypedValue.TYPE_LAST_INT) {
				return value.data;
			}
			throw new NotFoundException(
			    "Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
		}
	}

	/**
	 * Return an XmlResourceParser through which you can read a view layout
	 * description for the given resource ID.  This parser has limited
	 * functionality -- in particular, you can't change its input, and only
	 * the high-level events are available.
	 *
	 * <p>This function is really a simple wrapper for calling
	 * {@link #getXml} with a layout resource.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return A new parser object through which you can read
	 *         the XML data.
	 *
	 * @see #getXml
	 */
	public XmlResourceParser getLayout(int id) throws NotFoundException {
		return loadXmlResourceParser(id, "layout");
	}

	/**
	 * Return an XmlResourceParser through which you can read an animation
	 * description for the given resource ID.  This parser has limited
	 * functionality -- in particular, you can't change its input, and only
	 * the high-level events are available.
	 *
	 * <p>This function is really a simple wrapper for calling
	 * {@link #getXml} with an animation resource.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return A new parser object through which you can read
	 *         the XML data.
	 *
	 * @see #getXml
	 */
	public XmlResourceParser getAnimation(int id) throws NotFoundException {
		return loadXmlResourceParser(id, "anim");
	}

	/**
	 * Return an XmlResourceParser through which you can read a generic XML
	 * resource for the given resource ID.
	 *
	 * <p>The XmlPullParser implementation returned here has some limited
	 * functionality.  In particular, you can't change its input, and only
	 * high-level parsing events are available (since the document was
	 * pre-parsed for you at build time, which involved merging text and
	 * stripping comments).
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @return A new parser object through which you can read
	 *         the XML data.
	 *
	 * @see android.util.AttributeSet
	 */
	public XmlResourceParser getXml(int id) throws NotFoundException {
		return loadXmlResourceParser(id, "xml");
	}

	/**
	 * Open a data stream for reading a raw resource.  This can only be used
	 * with resources whose value is the name of an asset files -- that is, it can be
	 * used to open drawable, sound, and raw resources; it will fail on string
	 * and color resources.
	 *
	 * @param id The resource identifier to open, as generated by the appt
	 *           tool.
	 *
	 * @return InputStream Access to the resource data.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 */
	public InputStream openRawResource(int id) throws NotFoundException {
		TypedValue value;
		synchronized (mAccessLock) {
			value = mTmpValue;
			if (value == null) {
				value = new TypedValue();
			} else {
				mTmpValue = null;
			}
		}
		InputStream res = openRawResource(id, value);
		synchronized (mAccessLock) {
			if (mTmpValue == null) {
				mTmpValue = value;
			}
		}
		return res;
	}

	/**
	 * Open a data stream for reading a raw resource.  This can only be used
	 * with resources whose value is the name of an asset file -- that is, it can be
	 * used to open drawable, sound, and raw resources; it will fail on string
	 * and color resources.
	 *
	 * @param id The resource identifier to open, as generated by the appt tool.
	 * @param value The TypedValue object to hold the resource information.
	 *
	 * @return InputStream Access to the resource data.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 */
	public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
		getValue(id, value, true);

		try {
			return mAssets.openNonAsset(value.assetCookie, value.string.toString(),
			                            AssetManager.ACCESS_STREAMING);
		} catch (Exception e) {
			NotFoundException rnf = new NotFoundException("File " + value.string.toString() + " from drawable resource ID #0x" + Integer.toHexString(id));
			rnf.initCause(e);
			throw rnf;
		}
	}

	/**
	 * Open a file descriptor for reading a raw resource.  This can only be used
	 * with resources whose value is the name of an asset files -- that is, it can be
	 * used to open drawable, sound, and raw resources; it will fail on string
	 * and color resources.
	 *
	 * <p>This function only works for resources that are stored in the package
	 * as uncompressed data, which typically includes things like mp3 files
	 * and png images.
	 *
	 * @param id The resource identifier to open, as generated by the appt
	 *           tool.
	 *
	 * @return AssetFileDescriptor A new file descriptor you can use to read
	 * the resource.  This includes the file descriptor itself, as well as the
	 * offset and length of data where the resource appears in the file.  A
	 * null is returned if the file exists but is compressed.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 */
	public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
		TypedValue value;
		synchronized (mAccessLock) {
			value = mTmpValue;
			if (value == null) {
				value = new TypedValue();
			} else {
				mTmpValue = null;
			}
			getValue(id, value, true);
		}
		try {
			return mAssets.openNonAssetFd(
			    value.assetCookie, value.string.toString());
		} catch (Exception e) {
			NotFoundException rnf = new NotFoundException(
			    "File " + value.string.toString() + " from drawable resource ID #0x" + Integer.toHexString(id));
			rnf.initCause(e);
			throw rnf;
		} finally {
			synchronized (mAccessLock) {
				if (mTmpValue == null) {
					mTmpValue = value;
				}
			}
		}
	}

	/**
	 * Return the raw data associated with a particular resource ID.
	 *
	 * @param id The desired resource identifier, as generated by the aapt
	 *           tool. This integer encodes the package, type, and resource
	 *           entry. The value 0 is an invalid identifier.
	 * @param outValue Object in which to place the resource data.
	 * @param resolveRefs If true, a resource that is a reference to another
	 *                    resource will be followed so that you receive the
	 *                    actual final resource data.  If false, the TypedValue
	 *                    will be filled in with the reference itself.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 */
	public void getValue(int id, TypedValue outValue, boolean resolveRefs)
	    throws NotFoundException {
		boolean found = mAssets.getResourceValue(id, 0, outValue, resolveRefs);
		if (found) {
			return;
		}
		throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
	}

	/**
	 * Get the raw value associated with a resource with associated density.
	 *
	 * @param id resource identifier
	 * @param density density in DPI
	 * @param resolveRefs If true, a resource that is a reference to another
	 *            resource will be followed so that you receive the actual final
	 *            resource data. If false, the TypedValue will be filled in with
	 *            the reference itself.
	 * @throws NotFoundException Throws NotFoundException if the given ID does
	 *             not exist.
	 * @see #getValue(String, TypedValue, boolean)
	 */
	public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
	    throws NotFoundException {
		boolean found = mAssets.getResourceValue(id, density, outValue, resolveRefs);
		if (found) {
			return;
		}
		throw new NotFoundException("Resource ID #0x" + Integer.toHexString(id));
	}

	/**
	 * Return the raw data associated with a particular resource ID.
	 * See getIdentifier() for information on how names are mapped to resource
	 * IDs, and getString(int) for information on how string resources are
	 * retrieved.
	 *
	 * <p>Note: use of this function is discouraged.  It is much more
	 * efficient to retrieve resources by identifier than by name.
	 *
	 * @param name The name of the desired resource.  This is passed to
	 *             getIdentifier() with a default type of "string".
	 * @param outValue Object in which to place the resource data.
	 * @param resolveRefs If true, a resource that is a reference to another
	 *                    resource will be followed so that you receive the
	 *                    actual final resource data.  If false, the TypedValue
	 *                    will be filled in with the reference itself.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 */
	public void getValue(String name, TypedValue outValue, boolean resolveRefs)
	    throws NotFoundException {
		int id = getIdentifier(name, "string", null);
		if (id != 0) {
			getValue(id, outValue, resolveRefs);
			return;
		}
		throw new NotFoundException("String resource name " + name);
	}

	/**
	 * This class holds the current attribute values for a particular theme.
	 * In other words, a Theme is a set of values for resource attributes;
	 * these are used in conjunction with {@link TypedArray}
	 * to resolve the final value for an attribute.
	 *
	 * <p>The Theme's attributes come into play in two ways: (1) a styled
	 * attribute can explicit reference a value in the theme through the
	 * "?themeAttribute" syntax; (2) if no value has been defined for a
	 * particular styled attribute, as a last resort we will try to find that
	 * attribute's value in the Theme.
	 *
	 * <p>You will normally use the {@link #obtainStyledAttributes} APIs to
	 * retrieve XML attributes with style and theme information applied.
	 */
	public final class Theme {
		private long theme;

		private boolean debug = false;

		/**
		 * Place new attribute values into the theme.  The style resource
		 * specified by <var>resid</var> will be retrieved from this Theme's
		 * resources, its values placed into the Theme object.
		 *
		 * <p>The semantics of this function depends on the <var>force</var>
		 * argument:  If false, only values that are not already defined in
		 * the theme will be copied from the system resource; otherwise, if
		 * any of the style's attributes are already defined in the theme, the
		 * current values in the theme will be overwritten.
		 *
		 * @param resid The resource ID of a style resource from which to
		 *              obtain attribute values.
		 * @param force If true, values in the style resource will always be
		 *              used in the theme; otherwise, they will only be used
		 *              if not already defined in the theme.
		 */
		public void applyStyle(int resid, boolean force) {
			if (debug) {
				System.out.println("[" + theme + "] applyStyle(" + resid + ", " + force + ") called");
				try {
					throw new Exception("[stack trace]");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			mAssets.applyThemeStyle(theme, resid, force);
		}

		/**
		 * Set this theme to hold the same contents as the theme
		 * <var>other</var>.  If both of these themes are from the same
		 * Resources object, they will be identical after this function
		 * returns.  If they are from different Resources, only the resources
		 * they have in common will be set in this theme.
		 *
		 * @param other The existing Theme to copy from.
		 */
		public void setTo(Theme other) {
			getAssets().copyTheme(this.theme, other.theme);
		}

		/**
		 * Return a TypedArray holding the values defined by
		 * <var>Theme</var> which are listed in <var>attrs</var>.
		 *
		 * <p>Be sure to call {@link TypedArray#recycle() TypedArray.recycle()} when you are done
		 * with the array.
		 *
		 * @param attrs The desired attributes.
		 *
		 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
		 *
		 * @return Returns a TypedArray holding an array of the attribute values.
		 * Be sure to call {@link TypedArray#recycle() TypedArray.recycle()}
		 * when done with it.
		 *
		 * @see Resources#obtainAttributes
		 * @see #obtainStyledAttributes(int, int[])
		 * @see #obtainStyledAttributes(AttributeSet, int[], int, int)
		 */
		public TypedArray obtainStyledAttributes(int[] attrs) {
			return obtainStyledAttributes(null, attrs, 0, 0);
		}

		/**
		 * Return a TypedArray holding the values defined by the style
		 * resource <var>resid</var> which are listed in <var>attrs</var>.
		 *
		 * <p>Be sure to call {@link TypedArray#recycle() TypedArray.recycle()} when you are done
		 * with the array.
		 *
		 * @param resid The desired style resource.
		 * @param attrs The desired attributes in the style.
		 *
		 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
		 *
		 * @return Returns a TypedArray holding an array of the attribute values.
		 * Be sure to call {@link TypedArray#recycle() TypedArray.recycle()}
		 * when done with it.
		 *
		 * @see Resources#obtainAttributes
		 * @see #obtainStyledAttributes(int[])
		 * @see #obtainStyledAttributes(AttributeSet, int[], int, int)
		 */
		public TypedArray obtainStyledAttributes(int resid, int[] attrs)
		    throws NotFoundException {
			return obtainStyledAttributes(null, attrs, 0, resid);
		}

		/**
		 * Return a TypedArray holding the attribute values in
		 * <var>set</var>
		 * that are listed in <var>attrs</var>.  In addition, if the given
		 * AttributeSet specifies a style class (through the "style" attribute),
		 * that style will be applied on top of the base attributes it defines.
		 *
		 * <p>Be sure to call {@link TypedArray#recycle() TypedArray.recycle()} when you are done
		 * with the array.
		 *
		 * <p>When determining the final value of a particular attribute, there
		 * are four inputs that come into play:</p>
		 *
		 * <ol>
		 *     <li> Any attribute values in the given AttributeSet.
		 *     <li> The style resource specified in the AttributeSet (named
		 *     "style").
		 *     <li> The default style specified by <var>defStyleAttr</var> and
		 *     <var>defStyleRes</var>
		 *     <li> The base values in this theme.
		 * </ol>
		 *
		 * <p>Each of these inputs is considered in-order, with the first listed
		 * taking precedence over the following ones.  In other words, if in the
		 * AttributeSet you have supplied <code>&lt;Button
		 * textColor="#ff000000"&gt;</code>, then the button's text will
		 * <em>always</em> be black, regardless of what is specified in any of
		 * the styles.
		 *
		 * @param set The base set of attribute values.  May be null.
		 * @param attrs The desired attributes to be retrieved.
		 * @param defStyleAttr An attribute in the current theme that contains a
		 *                     reference to a style resource that supplies
		 *                     defaults values for the TypedArray.  Can be
		 *                     0 to not look for defaults.
		 * @param defStyleRes A resource identifier of a style resource that
		 *                    supplies default values for the TypedArray,
		 *                    used only if defStyleAttr is 0 or can not be found
		 *                    in the theme.  Can be 0 to not look for defaults.
		 *
		 * @return Returns a TypedArray holding an array of the attribute values.
		 * Be sure to call {@link TypedArray#recycle() TypedArray.recycle()}
		 * when done with it.
		 *
		 * @see Resources#obtainAttributes
		 * @see #obtainStyledAttributes(int[])
		 * @see #obtainStyledAttributes(int, int[])
		 */
		public TypedArray obtainStyledAttributes(AttributeSet set,
		                                         int[] attrs, int defStyleAttr, int defStyleRes) {
			int len = attrs.length;
			TypedArray array = TypedArray.obtain(getResources(), len);

			// XXX note that for now we only work with compiled XML files.
			// To support generic XML files we will need to manually parse
			// out the attributes from the XML file (applying type information
			// contained in the resources and such).
			XmlResourceParser parser = (XmlResourceParser)set;
			getAssets().applyStyle(theme, set != null ? ((XmlBlock.Parser)set).mParseState : 0,
			                       defStyleAttr, defStyleRes,
			                       attrs, attrs.length, array.mDataAddress, array.mIndicesAddress);
			array.mRsrcs = attrs;
			array.mXml = parser;
			array.mTheme = this;

			if (debug && set != null) {
				int[] data = array.mData;

				System.out.println("[" + theme + "] in obtainStyledAttributes:");
				System.out.println("Attributes:");
				String s = "  Attrs:";
				int i;
				for (i = 0; i < set.getAttributeCount(); i++) {
					s = s + " " + set.getAttributeName(i);
					int id = set.getAttributeNameResource(i);
					if (id != 0) {
						s = s + "(0x" + Integer.toHexString(id) + ")";
					}
					s = s + "=" + set.getAttributeValue(i);
				}
				System.out.println(s);
				s = "  Found: \n";
				TypedValue value = new TypedValue();
				for (i = 0; i < attrs.length; i++) {
					int d = i * AssetManager.STYLE_NUM_ENTRIES;
					value.type = data[d + AssetManager.STYLE_TYPE];
					value.data = data[d + AssetManager.STYLE_DATA];
					value.assetCookie = data[d + AssetManager.STYLE_ASSET_COOKIE];
					value.resourceId = data[d + AssetManager.STYLE_RESOURCE_ID];
					String name;
					try {
						name = getResourceEntryName(attrs[i]);
					} catch (Exception e) {
						name = "???";
					}
					s = s + "         [" + i + "] 0x" + Integer.toHexString(attrs[i]) + " (" + name + ")"
					  + " = " + value + "\n";
				}
				System.out.println(s);
			}
			return array;
		}

		/**
		 * Retrieve the values for a set of attributes in the Theme. The
		 * contents of the typed array are ultimately filled in by
		 * {@link Resources#getValue}.
		 *
		 * @param values The base set of attribute values, must be equal in
		 *               length to {@code attrs}. All values must be of type
		 *               {@link TypedValue#TYPE_ATTRIBUTE}.
		 * @param attrs The desired attributes to be retrieved.
		 * @return Returns a TypedArray holding an array of the attribute
		 *         values. Be sure to call {@link TypedArray#recycle()}
		 *         when done with it.
		 * @hide
		 */
		@NonNull
		public TypedArray resolveAttributes(@NonNull int[] values, @NonNull int[] attrs) {
			final int len = attrs.length;
			if (values == null || len != values.length) {
				throw new IllegalArgumentException(
				    "Base attribute values must the same length as attrs");
			}

			final TypedArray array = TypedArray.obtain(Resources.this, len);
			getAssets().resolveAttrs(theme, 0, 0, values, attrs, array.mData, array.mIndices);
			array.mTheme = this;
			array.mXml = null;

			return array;
		}

		/**
		 * Retrieve the value of an attribute in the Theme.  The contents of
		 * <var>outValue</var> are ultimately filled in by
		 * {@link Resources#getValue}.
		 *
		 * @param resid The resource identifier of the desired theme
		 *              attribute.
		 * @param outValue Filled in with the ultimate resource value supplied
		 *                 by the attribute.
		 * @param resolveRefs If true, resource references will be walked; if
		 *                    false, <var>outValue</var> may be a
		 *                    TYPE_REFERENCE.  In either case, it will never
		 *                    be a TYPE_ATTRIBUTE.
		 *
		 * @return boolean Returns true if the attribute was found and
		 *         <var>outValue</var> is valid, else false.
		 */
		public boolean resolveAttribute(int resid, TypedValue outValue,
		                                boolean resolveRefs) {
			boolean got = mAssets.getThemeValue(theme, resid, outValue, resolveRefs);
			if (debug) {
				System.out.println(
				    "resolveAttribute #" + Integer.toHexString(resid) + " got=" + got + ", type=0x" + Integer.toHexString(outValue.type) + ", data=0x" + Integer.toHexString(outValue.data));
			}
			return got;
		}

		/**
		 * Print contents of this theme out to the log.  For debugging only.
		 *
		 * @param priority The log priority to use.
		 * @param tag The log tag to use.
		 * @param prefix Text to prefix each line printed.
		 */
		public void dump(int priority, String tag, String prefix) {
			AssetManager.dumpTheme(theme, priority, tag, prefix);
		}

		protected void finalize() throws Throwable {
			super.finalize();
			mAssets.releaseTheme(theme);
		}

		private final ThemeKey mKey = new ThemeKey();

		/*package*/ Theme() {
			mAssets = Resources.this.mAssets;
			theme = mAssets.createTheme();
		}

		public Resources getResources() {
			return Resources.this;
		}

		/*package*/ ThemeKey getKey() {
			return mKey;
		}

		private final AssetManager mAssets;

		public void rebase() {}

		public Drawable getDrawable(int resId) {
			return Resources.this.getDrawable(resId, this);
		}
	}

	static class ThemeKey implements Cloneable {
		int[] mResId;
		boolean[] mForce;
		int mCount;

		private int mHashCode = 0;

		public void append(int resId, boolean force) {
			if (mResId == null) {
				mResId = new int[4];
			}

			if (mForce == null) {
				mForce = new boolean[4];
			}

			mResId = GrowingArrayUtils.append(mResId, mCount, resId);
			mForce = GrowingArrayUtils.append(mForce, mCount, force);
			mCount++;

			mHashCode = 31 * (31 * mHashCode + resId) + (force ? 1 : 0);
		}

		/**
		 * Sets up this key as a deep copy of another key.
		 *
		 * @param other the key to deep copy into this key
		 */
		public void setTo(ThemeKey other) {
			mResId = other.mResId == null ? null : other.mResId.clone();
			mForce = other.mForce == null ? null : other.mForce.clone();
			mCount = other.mCount;
		}

		@Override
		public int hashCode() {
			return mHashCode;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (o == null || getClass() != o.getClass() || hashCode() != o.hashCode()) {
				return false;
			}

			final ThemeKey t = (ThemeKey)o;
			if (mCount != t.mCount) {
				return false;
			}

			final int N = mCount;
			for (int i = 0; i < N; i++) {
				if (mResId[i] != t.mResId[i] || mForce[i] != t.mForce[i]) {
					return false;
				}
			}

			return true;
		}

		/**
		 * @return a shallow copy of this key
		 */
		@Override
		public ThemeKey clone() {
			final ThemeKey other = new ThemeKey();
			other.mResId = mResId;
			other.mForce = mForce;
			other.mCount = mCount;
			other.mHashCode = mHashCode;
			return other;
		}
	}

	/**
	 * Generate a new Theme object for this set of Resources.  It initially
	 * starts out empty.
	 *
	 * @return Theme The newly created Theme container.
	 */
	public final Theme newTheme() {
		return new Theme();
	}

	/**
	 * Retrieve a set of basic attribute values from an AttributeSet, not
	 * performing styling of them using a theme and/or style resources.
	 *
	 * @param set The current attribute values to retrieve.
	 * @param attrs The specific attributes to be retrieved.
	 * @return Returns a TypedArray holding an array of the attribute values.
	 * Be sure to call {@link TypedArray#recycle() TypedArray.recycle()}
	 * when done with it.
	 *
	 * @see Theme#obtainStyledAttributes(AttributeSet, int[], int, int)
	 */
	public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
		int len = attrs.length;
		TypedArray array = TypedArray.obtain(this, len);

		// XXX note that for now we only work with compiled XML files.
		// To support generic XML files we will need to manually parse
		// out the attributes from the XML file (applying type information
		// contained in the resources and such).
		XmlBlock.Parser parser = (XmlBlock.Parser)set;
		getAssets().retrieveAttributes(parser.mParseState, attrs, attrs.length, array.mDataAddress, array.mIndicesAddress);

		array.mXml = parser;

		return array;
	}

	/**
	 * Store the newly updated configuration.
	 */
	public void updateConfiguration(Configuration config,
	                                DisplayMetrics metrics) {
		updateConfiguration(config, metrics, null);
	}

	/**
	 * @hide
	 */
	public void updateConfiguration(Configuration config,
	                                DisplayMetrics metrics, CompatibilityInfo compat) {
		synchronized (mAccessLock) {
			if (DEBUG_CONFIG) {
				Slog.i(TAG, "**** Updating config of " + this + ": old config is " + mConfiguration + " old compat is " + mCompatibilityInfo);
				Slog.i(TAG, "**** Updating config of " + this + ": new config is " + config + " new compat is " + compat);
			}
			if (compat != null) {
				mCompatibilityInfo = compat;
			}
			if (metrics != null) {
				mMetrics.setTo(metrics);
			}
			// NOTE: We should re-arrange this code to create a Display
			// with the CompatibilityInfo that is used everywhere we deal
			// with the display in relation to this app, rather than
			// doing the conversion here.  This impl should be okay because
			// we make sure to return a compatible display in the places
			// where there are public APIs to retrieve the display...  but
			// it would be cleaner and more maintainble to just be
			// consistently dealing with a compatible display everywhere in
			// the framework.
			mCompatibilityInfo.applyToDisplayMetrics(mMetrics);

			int configChanges = 0xfffffff;
			if (config != null) {
				mTmpConfig.setTo(config);
				int density = config.densityDpi;
				if (density == Configuration.DENSITY_DPI_UNDEFINED) {
					density = mMetrics.noncompatDensityDpi;
				}

				mCompatibilityInfo.applyToConfiguration(density, mTmpConfig);

				if (mTmpConfig.locale == null) {
					mTmpConfig.locale = Locale.getDefault();
					mTmpConfig.setLayoutDirection(mTmpConfig.locale);
				}
				configChanges = mConfiguration.updateFrom(mTmpConfig);
				configChanges = ActivityInfo.activityInfoConfigToNative(configChanges);
			}
			if (mConfiguration.locale == null) {
				mConfiguration.locale = Locale.getDefault();
				mConfiguration.setLayoutDirection(mConfiguration.locale);
			}
			if (mConfiguration.densityDpi != Configuration.DENSITY_DPI_UNDEFINED) {
				mMetrics.densityDpi = mConfiguration.densityDpi;
				mMetrics.density = mConfiguration.densityDpi * DisplayMetrics.DENSITY_DEFAULT_SCALE;
			}
			mMetrics.scaledDensity = mMetrics.density * mConfiguration.fontScale;

			String locale = null;
			if (mConfiguration.locale != null) {
				locale = mConfiguration.locale.getLanguage();
				if (mConfiguration.locale.getCountry() != null) {
					locale += "-" + mConfiguration.locale.getCountry();
				}
			}
			int width, height;
			if (mMetrics.widthPixels >= mMetrics.heightPixels) {
				width = mMetrics.widthPixels;
				height = mMetrics.heightPixels;
			} else {
				// noinspection SuspiciousNameCombination
				width = mMetrics.heightPixels;
				// noinspection SuspiciousNameCombination
				height = mMetrics.widthPixels;
			}
			int keyboardHidden = mConfiguration.keyboardHidden;
			if (keyboardHidden == Configuration.KEYBOARDHIDDEN_NO && mConfiguration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
				keyboardHidden = Configuration.KEYBOARDHIDDEN_SOFT;
			}
			mAssets.setConfiguration(mConfiguration.mcc, mConfiguration.mnc,
			                         locale, mConfiguration.orientation,
			                         mConfiguration.touchscreen,
			                         mConfiguration.densityDpi, mConfiguration.keyboard,
			                         keyboardHidden, mConfiguration.navigation, width, height,
			                         mConfiguration.smallestScreenWidthDp,
			                         mConfiguration.screenWidthDp, mConfiguration.screenHeightDp,
			                         mConfiguration.screenLayout, mConfiguration.uiMode,
			                         Build.VERSION.RESOURCES_SDK_INT);

			if (DEBUG_CONFIG) {
				Slog.i(TAG, "**** Updating config of " + this + ": final config is " + mConfiguration + " final compat is " + mCompatibilityInfo);
			}

			//            clearDrawableCacheLocked(mDrawableCache, configChanges);
			//            clearDrawableCacheLocked(mColorDrawableCache, configChanges);

			mComplexColorCache.onConfigurationChange(configChanges);

			flushLayoutCache();
		}
		synchronized (sSync) {
			if (mPluralRule != null) {
				mPluralRule = PluralRules.forLocale(config.locale);
			}
		}
	}

	private void clearDrawableCacheLocked(
	    LongSparseArray<WeakReference<ConstantState>> cache,
	    int configChanges) {
		/*int N = cache.size();
		if (DEBUG_CONFIG) {
			Log.d(TAG, "Cleaning up drawables config changes: 0x"
			           + Integer.toHexString(configChanges));
		}
		for (int i = 0; i < N; i++) {
			WeakReference<Drawable.ConstantState> ref = cache.valueAt(i);
			if (ref != null) {
				Drawable.ConstantState cs = ref.get();
				if (cs != null) {
					if (Configuration.needNewResources(
						configChanges, cs.getChangingConfigurations())) {
						if (DEBUG_CONFIG) {
							Log.d(TAG, "FLUSHING #0x"
							           + Long.toHexString(mDrawableCache.keyAt(i))
							               + " / " + cs + " with changes: 0x"
							               + Integer.toHexString(cs.getChangingConfigurations()));
						}
						cache.setValueAt(i, null);
					} else if (DEBUG_CONFIG) {
						Log.d(TAG, "(Keeping #0x"
						           + Long.toHexString(cache.keyAt(i))
						               + " / " + cs + " with changes: 0x"
						               + Integer.toHexString(cs.getChangingConfigurations())
						               + ")");
					}
				}
			}
		}*/
	}

	/**
	 * Update the system resources configuration if they have previously
	 * been initialized.
	 *
	 * @hide
	 */
	public static void updateSystemConfiguration(Configuration config, DisplayMetrics metrics,
	                                             CompatibilityInfo compat) {
		if (mSystem != null) {
			mSystem.updateConfiguration(config, metrics, compat);
			// Log.i(TAG, "Updated system resources " + mSystem
			//         + ": " + mSystem.getConfiguration());
		}
	}

	/**
	 * Return the current display metrics that are in effect for this resource
	 * object.  The returned object should be treated as read-only.
	 *
	 * @return The resource's current display metrics.
	 */
	public DisplayMetrics getDisplayMetrics() {
		if (DEBUG_CONFIG)
			Slog.v(TAG, "Returning DisplayMetrics: " + mMetrics.widthPixels + "x" + mMetrics.heightPixels + " " + mMetrics.density);
		return mMetrics;
	}

	/**
	 * Return the current configuration that is in effect for this resource
	 * object.  The returned object should be treated as read-only.
	 *
	 * @return The resource's current configuration.
	 */
	public Configuration getConfiguration() {
		return mConfiguration;
	}

	/**
	 * Return the compatibility mode information for the application.
	 * The returned object should be treated as read-only.
	 *
	 * @return compatibility info.
	 * @hide
	 */
	public CompatibilityInfo getCompatibilityInfo() {
		return mCompatibilityInfo;
	}

	/**
	 * This is just for testing.
	 * @hide
	 */
	public void setCompatibilityInfo(CompatibilityInfo ci) {
		if (ci != null) {
			mCompatibilityInfo = ci;
			updateConfiguration(mConfiguration, mMetrics);
		}
	}

	/**
	 * Return a resource identifier for the given resource name.  A fully
	 * qualified resource name is of the form "package:type/entry".  The first
	 * two components (package and type) are optional if defType and
	 * defPackage, respectively, are specified here.
	 *
	 * <p>Note: use of this function is discouraged.  It is much more
	 * efficient to retrieve resources by identifier than by name.
	 *
	 * @param name The name of the desired resource.
	 * @param defType Optional default resource type to find, if "type/" is
	 *                not included in the name.  Can be null to require an
	 *                explicit type.
	 * @param defPackage Optional default package to find, if "package:" is
	 *                   not included in the name.  Can be null to require an
	 *                   explicit package.
	 *
	 * @return int The associated resource identifier.  Returns 0 if no such
	 *         resource was found.  (0 is not a valid resource ID.)
	 */
	public int getIdentifier(String name, String defType, String defPackage) {
		if (name == null) {
			throw new NullPointerException("name is null");
		}
		try {
			return Integer.parseInt(name);
		} catch (Exception e) {
			// Ignore
		}
		return mAssets.getResourceIdentifier(name, defType, defPackage);
	}

	/**
	 * Return true if given resource identifier includes a package.
	 *
	 * @hide
	 */
	public static boolean resourceHasPackage(int resid) {
		return (resid >>> 24) != 0;
	}

	/**
	 * Return the full name for a given resource identifier.  This name is
	 * a single string of the form "package:type/entry".
	 *
	 * @param resid The resource identifier whose name is to be retrieved.
	 *
	 * @return A string holding the name of the resource.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @see #getResourcePackageName
	 * @see #getResourceTypeName
	 * @see #getResourceEntryName
	 */
	public String getResourceName(int resid) throws NotFoundException {
		String str = mAssets.getResourceName(resid);
		if (str != null)
			return str;
		throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
	}

	/**
	 * Return the package name for a given resource identifier.
	 *
	 * @param resid The resource identifier whose package name is to be
	 * retrieved.
	 *
	 * @return A string holding the package name of the resource.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @see #getResourceName
	 */
	public String getResourcePackageName(int resid) throws NotFoundException {
		String str = mAssets.getResourcePackageName(resid);
		if (str != null)
			return str;
		throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
	}

	/**
	 * Return the type name for a given resource identifier.
	 *
	 * @param resid The resource identifier whose type name is to be
	 * retrieved.
	 *
	 * @return A string holding the type name of the resource.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @see #getResourceName
	 */
	public String getResourceTypeName(int resid) throws NotFoundException {
		String str = mAssets.getResourceTypeName(resid);
		if (str != null)
			return str;
		throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
	}

	/**
	 * Return the entry name for a given resource identifier.
	 *
	 * @param resid The resource identifier whose entry name is to be
	 * retrieved.
	 *
	 * @return A string holding the entry name of the resource.
	 *
	 * @throws NotFoundException Throws NotFoundException if the given ID does not exist.
	 *
	 * @see #getResourceName
	 */
	public String getResourceEntryName(int resid) throws NotFoundException {
		String str = mAssets.getResourceEntryName(resid);
		if (str != null)
			return str;
		throw new NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(resid));
	}

	/**
	 * Parse a series of {@link android.R.styleable#Extra &lt;extra&gt;} tags from
	 * an XML file.  You call this when you are at the parent tag of the
	 * extra tags, and it will return once all of the child tags have been parsed.
	 * This will call {@link #parseBundleExtra} for each extra tag encountered.
	 *
	 * @param parser The parser from which to retrieve the extras.
	 * @param outBundle A Bundle in which to place all parsed extras.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
	    throws XmlPullParserException, IOException {
		int outerDepth = parser.getDepth();
		int type;
		while ((type = parser.next()) != XmlPullParser.END_DOCUMENT && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
			if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
				continue;
			}

			String nodeName = parser.getName();
			if (nodeName.equals("extra")) {
				parseBundleExtra("extra", parser, outBundle);
				XmlUtils.skipCurrentTag(parser);

			} else {
				XmlUtils.skipCurrentTag(parser);
			}
		}
	}

	/**
	 * Parse a name/value pair out of an XML tag holding that data.  The
	 * AttributeSet must be holding the data defined by
	 * {@link android.R.styleable#Extra}.  The following value types are supported:
	 * <ul>
	 * <li> {@link TypedValue#TYPE_STRING}:
	 * {@link Bundle#putCharSequence Bundle.putCharSequence()}
	 * <li> {@link TypedValue#TYPE_INT_BOOLEAN}:
	 * {@link Bundle#putCharSequence Bundle.putBoolean()}
	 * <li> {@link TypedValue#TYPE_FIRST_INT}-{@link TypedValue#TYPE_LAST_INT}:
	 * {@link Bundle#putCharSequence Bundle.putBoolean()}
	 * <li> {@link TypedValue#TYPE_FLOAT}:
	 * {@link Bundle#putCharSequence Bundle.putFloat()}
	 * </ul>
	 *
	 * @param tagName The name of the tag these attributes come from; this is
	 * only used for reporting error messages.
	 * @param attrs The attributes from which to retrieve the name/value pair.
	 * @param outBundle The Bundle in which to place the parsed value.
	 * @throws XmlPullParserException If the attributes are not valid.
	 */
	public void parseBundleExtra(String tagName, AttributeSet attrs,
	                             Bundle outBundle) throws XmlPullParserException {
		TypedArray sa = obtainAttributes(attrs,
		                                 com.android.internal.R.styleable.Extra);

		String name = sa.getString(
		    com.android.internal.R.styleable.Extra_name);
		if (name == null) {
			sa.recycle();
			throw new XmlPullParserException("<" + tagName + "> requires an android:name attribute at " + attrs.getPositionDescription());
		}

		TypedValue v = sa.peekValue(
		    com.android.internal.R.styleable.Extra_value);
		if (v != null) {
			if (v.type == TypedValue.TYPE_STRING) {
				CharSequence cs = v.coerceToString();
				outBundle.putCharSequence(name, cs);
			} else if (v.type == TypedValue.TYPE_INT_BOOLEAN) {
				outBundle.putBoolean(name, v.data != 0);
			} else if (v.type >= TypedValue.TYPE_FIRST_INT && v.type <= TypedValue.TYPE_LAST_INT) {
				outBundle.putInt(name, v.data);
			} else if (v.type == TypedValue.TYPE_FLOAT) {
				outBundle.putFloat(name, v.getFloat());
			} else {
				sa.recycle();
				throw new XmlPullParserException("<" + tagName + "> only supports string, integer, float, color, and boolean at " + attrs.getPositionDescription());
			}
		} else {
			sa.recycle();
			throw new XmlPullParserException("<" + tagName + "> requires an android:value or android:resource attribute at " + attrs.getPositionDescription());
		}

		sa.recycle();
	}

	/**
	 * Retrieve underlying AssetManager storage for these resources.
	 */
	public final AssetManager getAssets() {
		return mAssets;
	}

	/**
	 * Call this to remove all cached loaded layout resources from the
	 * Resources object.  Only intended for use with performance testing
	 * tools.
	 */
	public final void flushLayoutCache() {
		synchronized (mCachedXmlBlockIds) {
			// First see if this block is in our cache.
			final int num = mCachedXmlBlockIds.length;
			for (int i = 0; i < num; i++) {
				mCachedXmlBlockIds[i] = -0;
				XmlBlock oldBlock = mCachedXmlBlocks[i];
				if (oldBlock != null) {
					oldBlock.close();
				}
				mCachedXmlBlocks[i] = null;
			}
		}
	}

	/**
	 * Start preloading of resource data using this Resources object.  Only
	 * for use by the zygote process for loading common system resources.
	 * {@hide}
	 */
	public final void startPreloading() {
		synchronized (sSync) {
			if (sPreloaded) {
				throw new IllegalStateException("Resources already preloaded");
			}
			sPreloaded = true;
			mPreloading = true;
			sPreloadedDensity = DisplayMetrics.DENSITY_DEVICE;
			mConfiguration.densityDpi = sPreloadedDensity;
			updateConfiguration(null, null);
		}
	}

	/**
	 * Called by zygote when it is done preloading resources, to change back
	 * to normal Resources operation.
	 */
	public final void finishPreloading() {
		if (mPreloading) {
			mPreloading = false;
			flushLayoutCache();
		}
	}

	/**
	 * @hide
	 */
	public LongSparseArray<Drawable.ConstantState> getPreloadedDrawables() {
		return sPreloadedDrawables[0];
	}

	private boolean verifyPreloadConfig(int changingConfigurations, int allowVarying,
	                                    int resourceId, String name) {
		// We allow preloading of resources even if they vary by font scale (which
		// doesn't impact resource selection) or density (which we handle specially by
		// simply turning off all preloading), as well as any other configs specified
		// by the caller.
		if (((changingConfigurations & ~(ActivityInfo.CONFIG_FONT_SCALE | ActivityInfo.CONFIG_DENSITY)) & ~allowVarying) != 0) {
			String resName;
			try {
				resName = getResourceName(resourceId);
			} catch (NotFoundException e) {
				resName = "?";
			}
			Log.w(TAG, "Preloaded " + name + " resource #0x" + Integer.toHexString(resourceId) + " (" + resName + ") that varies with configuration!!");
			return false;
		}
		if (TRACE_FOR_PRELOAD) {
			String resName;
			try {
				resName = getResourceName(resourceId);
			} catch (NotFoundException e) {
				resName = "?";
			}
			Log.w(TAG, "Preloading " + name + " resource #0x" + Integer.toHexString(resourceId) + " (" + resName + ")");
		}
		return true;
	}

	static private final int LAYOUT_DIR_CONFIG = ActivityInfo.activityInfoConfigToNative(
	    ActivityInfo.CONFIG_LAYOUT_DIRECTION);

	/*package*/ Drawable loadDrawable(TypedValue value, int id) throws NotFoundException {
		return loadDrawable(value, id, null);
	}

	/*package*/ Drawable loadDrawable(TypedValue value, int id, Theme theme) throws NotFoundException {

		/*if (TRACE_FOR_PRELOAD) {
			// Log only framework resources
			if ((id >>> 24) == 0x1) {
				final String name = getResourceName(id);
				if (name != null)
					android.util.Log.d("PreloadDrawable", name);
			}
		}*/

		boolean isColorDrawable = false;
		if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
		    && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			isColorDrawable = true;
		}
		final long key = isColorDrawable ? value.data : (((long)value.assetCookie) << 32) | value.data;

		Drawable dr = null;
		/*Drawable dr = getCachedDrawable(isColorDrawable ? mColorDrawableCache : mDrawableCache, key);

		if (dr != null) {
			return dr;
		}
		Drawable.ConstantState cs;
		if (isColorDrawable) {
			cs = sPreloadedColorDrawables.get(key);
		} else {
			cs = sPreloadedDrawables[mConfiguration.getLayoutDirection()].get(key);
		}
		if (cs != null) {
			dr = cs.newDrawable(this);
		} else*/
		{
			if (isColorDrawable) {
				dr = new ColorDrawable(value.data);
			}

			if (dr == null) {
				if (value.string == null) {
					Log.w(TAG,
					      "Resource is not a Drawable (color or path): " + value);
					return null;
				}

				String file = value.string.toString();

				if (TRACE_FOR_MISS_PRELOAD) {
					// Log only framework resources
					if ((id >>> 24) == 0x1) {
						final String name = getResourceName(id);
						if (name != null)
							android.util.Log.d(TAG, "Loading framework drawable #"
							                        + Integer.toHexString(id) + ": " + name
							                        + " at " + file);
					}
				}

				if (DEBUG_LOAD)
					Log.v(TAG, "Loading drawable for cookie "
					           + value.assetCookie + ": " + file);

				if (file.endsWith(".xml")) {
					Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, file);
					try {
						XmlResourceParser rp = loadXmlResourceParser(
						    file, id, value.assetCookie, "drawable");
						dr = Drawable.createFromXml(this, rp, theme);
						rp.close();
					} catch (Exception e) {
						Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
						NotFoundException rnf = new NotFoundException(
						    "File " + file + " from drawable resource ID #0x"
						    + Integer.toHexString(id));
						rnf.initCause(e);
						throw rnf;
					}
					Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);

				} else {
					Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, file);
					try {
						InputStream is = mAssets.openNonAsset(
						    value.assetCookie, file, AssetManager.ACCESS_STREAMING);
						//                System.out.println("Opened file " + file + ": " + is);
						dr = Drawable.createFromResourceStream(this, value, is, file, null);
						is.close();
						//                System.out.println("Created stream: " + dr);
					} catch (Exception e) {
						Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
						NotFoundException rnf = new NotFoundException(
						    "File " + file + " from drawable resource ID #0x"
						    + Integer.toHexString(id));
						rnf.initCause(e);
						throw rnf;
					}
					Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
				}
			}
		}

		if (dr != null) {
			dr.setChangingConfigurations(value.changingConfigurations);
			/*cs = dr.getConstantState();
			if (cs != null) {
				if (mPreloading) {
					final int changingConfigs = cs.getChangingConfigurations();
					if (isColorDrawable) {
						if (verifyPreloadConfig(changingConfigs, 0, value.resourceId,
						                        "drawable")) {
							sPreloadedColorDrawables.put(key, cs);
						}
					} else {
						if (verifyPreloadConfig(changingConfigs,
						                        LAYOUT_DIR_CONFIG, value.resourceId, "drawable")) {
							if ((changingConfigs & LAYOUT_DIR_CONFIG) == 0) {
								// If this resource does not vary based on layout direction,
								// we can put it in all of the preload maps.
								sPreloadedDrawables[0].put(key, cs);
								sPreloadedDrawables[1].put(key, cs);
							} else {
								// Otherwise, only in the layout dir we loaded it for.
								final LongSparseArray<Drawable.ConstantState> preloads = sPreloadedDrawables[mConfiguration.getLayoutDirection()];
								preloads.put(key, cs);
							}
						}
					}
				} else {
					synchronized (mAccessLock) {
						//Log.i(TAG, "Saving cached drawable @ #" +
						//        Integer.toHexString(key.intValue())
						//        + " in " + this + ": " + cs);
						if (isColorDrawable) {
							mColorDrawableCache.put(key, new WeakReference<Drawable.ConstantState>(cs));
						} else {
							mDrawableCache.put(key, new WeakReference<Drawable.ConstantState>(cs));
						}
					}
				}
			}*/
		}

		return dr;
	}

	private Drawable getCachedDrawable(
	    LongSparseArray<WeakReference<ConstantState>> drawableCache,
	    long key) { /*
	 synchronized (mAccessLock) {
	     WeakReference<Drawable.ConstantState> wr = drawableCache.get(key);
	     if (wr != null) {   // we have the key
		 Drawable.ConstantState entry = wr.get();
		 if (entry != null) {
		     //Log.i(TAG, "Returning cached drawable @ #" +
		     //        Integer.toHexString(((Integer)key).intValue())
		     //        + " in " + this + ": " + entry);
		     return entry.newDrawable(this);
		 }
		 else {  // our entry has been purged
		     drawableCache.delete(key);
		 }
	     }
	 }*/
		return null;
	}

	/**
	 * Given the value and id, we can get the XML filename as in value.data, based on that, we
	 * first try to load CSL from the cache. If not found, try to get from the constant state.
	 * Last, parse the XML and generate the CSL.
	 */
	private ComplexColor loadComplexColorFromName(Theme theme, TypedValue value, int id) {
		final long key = (((long)value.assetCookie) << 32) | value.data;
		final ConfigurationBoundResourceCache<ComplexColor> cache = mComplexColorCache;
		ComplexColor complexColor = cache.getInstance(key, theme);
		if (complexColor != null) {
			return complexColor;
		}

		final android.content.res.ConstantState<ComplexColor> factory = sPreloadedComplexColors.get(key);

		if (factory != null) {
			complexColor = factory.newInstance(this, theme);
		}
		if (complexColor == null) {
			complexColor = loadComplexColorForCookie(value, id, theme);
		}

		if (complexColor != null) {
			if (mPreloading) {
				if (verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId, "color")) {
					sPreloadedComplexColors.put(key, complexColor.getConstantState());
				}
			} else {
				cache.put(key, theme, complexColor.getConstantState());
			}
		}
		return complexColor;
	}

	@Nullable
	public ComplexColor loadComplexColor(@NonNull TypedValue value, int id, Theme theme) {
		if (TRACE_FOR_PRELOAD) {
			// Log only framework resources
			if ((id >>> 24) == 0x1) {
				final String name = getResourceName(id);
				if (name != null)
					android.util.Log.d("loadComplexColor", name);
			}
		}

		final long key = (((long)value.assetCookie) << 32) | value.data;

		// Handle inline color definitions.
		if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			return getColorStateListFromInt(value, key);
		}

		final String file = value.string.toString();

		ComplexColor complexColor;
		if (file.endsWith(".xml")) {
			try {
				complexColor = loadComplexColorFromName(theme, value, id);
			} catch (Exception e) {
				final NotFoundException rnf = new NotFoundException(
				    "File " + file + " from complex color resource ID #0x" + Integer.toHexString(id));
				rnf.initCause(e);
				throw rnf;
			}
		} else {
			throw new NotFoundException(
			    "File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
		}

		return complexColor;
	}

	@Nullable
	ColorStateList loadColorStateList(TypedValue value, int id, Theme theme)
	    throws NotFoundException {
		if (TRACE_FOR_PRELOAD) {
			// Log only framework resources
			if ((id >>> 24) == 0x1) {
				final String name = getResourceName(id);
				if (name != null)
					android.util.Log.d("PreloadColorStateList", name);
			}
		}

		final long key = (((long)value.assetCookie) << 32) | value.data;

		// Handle inline color definitions.
		if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			return getColorStateListFromInt(value, key);
		}

		ComplexColor complexColor = loadComplexColorFromName(theme, value, id);
		if (complexColor != null && complexColor instanceof ColorStateList) {
			return (ColorStateList)complexColor;
		}

		throw new NotFoundException(
		    "Can't find ColorStateList from drawable resource ID #0x" + Integer.toHexString(id));
	}

	@NonNull
	private ColorStateList getColorStateListFromInt(@NonNull TypedValue value, long key) {
		ColorStateList csl;
		final android.content.res.ConstantState<ComplexColor> factory =
		    sPreloadedComplexColors.get(key);
		if (factory != null) {
			return (ColorStateList)factory.newInstance();
		}

		csl = ColorStateList.valueOf(value.data);

		if (mPreloading) {
			if (verifyPreloadConfig(value.changingConfigurations, 0, value.resourceId,
			                        "color")) {
				sPreloadedComplexColors.put(key, csl.getConstantState());
			}
		}

		return csl;
	}

	/**
	 * Load a ComplexColor based on the XML file content. The result can be a GradientColor or
	 * ColorStateList. Note that pure color will be wrapped into a ColorStateList.
	 *
	 * We deferred the parser creation to this function b/c we need to differentiate b/t gradient
	 * and selector tag.
	 *
	 * @return a ComplexColor (GradientColor or ColorStateList) based on the XML file content.
	 */
	@Nullable
	private ComplexColor loadComplexColorForCookie(TypedValue value, int id, Theme theme) {
		if (value.string == null) {
			throw new UnsupportedOperationException(
			    "Can't convert to ComplexColor: type=0x" + value.type);
		}

		final String file = value.string.toString();

		if (TRACE_FOR_MISS_PRELOAD) {
			// Log only framework resources
			if ((id >>> 24) == 0x1) {
				final String name = getResourceName(id);
				if (name != null) {
					Log.d(TAG, "Loading framework ComplexColor #" + Integer.toHexString(id) + ": " + name + " at " + file);
				}
			}
		}

		if (DEBUG_LOAD) {
			Log.v(TAG, "Loading ComplexColor for cookie " + value.assetCookie + ": " + file);
		}

		ComplexColor complexColor = null;

		Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, file);
		if (file.endsWith(".xml")) {
			try {
				final XmlResourceParser parser = loadXmlResourceParser(
				    file, id, value.assetCookie, "ComplexColor");

				final AttributeSet attrs = Xml.asAttributeSet(parser);
				int type;
				while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
					// Seek parser to start tag.
				}
				if (type != XmlPullParser.START_TAG) {
					throw new XmlPullParserException("No start tag found");
				}

				final String name = parser.getName();
				if (name.equals("gradient")) {
					complexColor = GradientColor.createFromXmlInner(this, parser, attrs, theme);
				} else if (name.equals("selector")) {
					complexColor = ColorStateList.createFromXmlInner(this, parser, attrs, theme);
				}
				parser.close();
			} catch (Exception e) {
				Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
				final NotFoundException rnf = new NotFoundException(
				    "File " + file + " from ComplexColor resource ID #0x" + Integer.toHexString(id));
				rnf.initCause(e);
				throw rnf;
			}
		} else {
			Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
			throw new NotFoundException(
			    "File " + file + " from drawable resource ID #0x" + Integer.toHexString(id) + ": .xml extension required");
		}
		Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);

		return complexColor;
	}

	/*package*/ XmlResourceParser loadXmlResourceParser(int id, String type)
	    throws NotFoundException {
		synchronized (mAccessLock) {
			TypedValue value = mTmpValue;
			if (value == null) {
				mTmpValue = value = new TypedValue();
			}
			getValue(id, value, true);
			if (value.type == TypedValue.TYPE_STRING) {
				return loadXmlResourceParser(value.string.toString(), id,
				                             value.assetCookie, type);
			}
			throw new NotFoundException(
			    "Resource ID #0x" + Integer.toHexString(id) + " type #0x" + Integer.toHexString(value.type) + " is not valid");
		}
	}

	/*package*/ XmlResourceParser loadXmlResourceParser(String file, int id,
	                                                    int assetCookie, String type) throws NotFoundException {
		try {
			return mAssets.openXmlResourceParser(assetCookie, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		/*if (id != 0) {
			try {
				// These may be compiled...
				synchronized (mCachedXmlBlockIds) {
					// First see if this block is in our cache.
					final int num = mCachedXmlBlockIds.length;
					for (int i=0; i<num; i++) {
						if (mCachedXmlBlockIds[i] == id) {
							//System.out.println("**** REUSING XML BLOCK!  id="
							//                   + id + ", index=" + i);
							return mCachedXmlBlocks[i].newParser();
						}
					}

					// Not in the cache, create a new block and put it at
					// the next slot in the cache.
					XmlBlock block = mAssets.openXmlBlockAsset(
					assetCookie, file);
					if (block != null) {
						int pos = mLastCachedXmlBlockIndex+1;
						if (pos >= num) pos = 0;
						mLastCachedXmlBlockIndex = pos;
						XmlBlock oldBlock = mCachedXmlBlocks[pos];
						if (oldBlock != null) {
						oldBlock.close();
						}
						mCachedXmlBlockIds[pos] = id;
						mCachedXmlBlocks[pos] = block;
						//System.out.println("**** CACHING NEW XML BLOCK!  id="
						//                   + id + ", index=" + pos);
						return block.newParser();
					}
				}
			} catch (Exception e) {
				NotFoundException rnf = new NotFoundException("File " + file +
					                                      " from xml type " + type +
					                                      " resource ID #0x" + Integer.toHexString(id));
				rnf.initCause(e);
				throw rnf;
			}
		}

		throw new NotFoundException(
			"File " + file + " from xml type " + type + " resource ID #0x"
			+ Integer.toHexString(id));*/
	}

	/**
	* Obtains styled attributes from the theme, if available, or unstyled
	* resources if the theme is null.
	*
	* @hide
	*/
	public static TypedArray obtainAttributes(
	    Resources res, Theme theme, AttributeSet set, int[] attrs) {
		if (theme == null) {
			return res.obtainAttributes(set, attrs);
		}

		return theme.obtainStyledAttributes(set, attrs, 0, 0);
	}
}
