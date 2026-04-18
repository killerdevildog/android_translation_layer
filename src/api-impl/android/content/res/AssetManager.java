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

import android.content.Context;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.util.TypedValue;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Provides access to an application's raw asset files; see {@link Resources}
 * for the way most applications will want to retrieve their resource data.
 * This class presents a lower-level API that allows you to open and read raw
 * files that have been bundled with the application as a simple stream of
 * bytes.
 */
public final class AssetManager {
	/* modes used when opening an asset */

	/**
	 * Mode for {@link #open(String, int)}: no specific information about how
	 * data will be accessed.
	 */
	public static final int ACCESS_UNKNOWN = 0;
	/**
	 * Mode for {@link #open(String, int)}: Read chunks, and seek forward and
	 * backward.
	 */
	public static final int ACCESS_RANDOM = 1;
	/**
	 * Mode for {@link #open(String, int)}: Read sequentially, with an
	 * occasional forward seek.
	 */
	public static final int ACCESS_STREAMING = 2;
	/**
	 * Mode for {@link #open(String, int)}: Attempt to load contents into
	 * memory, for fast small reads.
	 */
	public static final int ACCESS_BUFFER = 3;

	private static final String TAG = "AssetManager";
	private static final boolean localLOGV = false || false;

	private static final boolean DEBUG_REFS = false;

	private static final Object sSync = new Object();
	/*package*/ static AssetManager sSystem = null;

	private final TypedValue mValue = new TypedValue();
	private final long[] mOffsets = new long[2];

	// For communication with native code.
	private long mObject;
	private int mNObject; // used by the NDK

	private StringBlock mStringBlocks[] = null;

	private int mNumRefs = 1;
	private boolean mOpen = true;
	private HashMap<Integer, RuntimeException> mRefStacks;

	private ArrayList<String> asset_paths = new ArrayList<String>();

	/**
	 * Create a new AssetManager containing only the basic system assets.
	 * Applications will not generally use this method, instead retrieving the
	 * appropriate asset manager with {@link Resources#getAssets}.    Not for
	 * use by applications.
	 * {@hide}
	 */
	public AssetManager() {
		synchronized (this) {
			if (DEBUG_REFS) {
				mNumRefs = 0;
				incRefsLocked(this.hashCode());
			}
			init(android.os.Build.VERSION.RESOURCES_SDK_INT);
			if (localLOGV)
				Log.v(TAG, "New asset manager: " + this);
			//            ensureSystemAssets()
			try {
				Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources("AndroidManifest.xml");
				ArrayList<String> paths = new ArrayList<String>();
				paths.add(null); // reserve first slot for framework-res.apk
				while (resources.hasMoreElements()) {
					String path = resources.nextElement().getPath();
					path = URLDecoder.decode(path, "UTF-8");
					if (path.contains("framework-res.apk")) // needs to be first, so it can be overridden
						paths.set(0, path);
					else
						paths.add(path);
				}
				for (String path : paths) {
					if (path != null) {
						path = path.substring(path.indexOf("file:") + 5, path.indexOf("!/AndroidManifest.xml"));
						asset_paths.add(path);
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "failed to load resources.arsc" + e);
			}
			asset_paths.add(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
			/*String*/ Object[] asset_paths_arr = asset_paths.toArray();
			native_setApkAssets(asset_paths_arr, asset_paths_arr.length);
		}
	}

	private static void ensureSystemAssets() {
		synchronized (sSync) {
			if (sSystem == null) {
				AssetManager system = new AssetManager(true);
				system.makeStringBlocks(false);
				sSystem = system;
			}
		}
	}

	private AssetManager(boolean isSystem) {
		if (DEBUG_REFS) {
			synchronized (this) {
				mNumRefs = 0;
				incRefsLocked(this.hashCode());
			}
		}
		init(android.os.Build.VERSION.RESOURCES_SDK_INT);
		if (localLOGV)
			Log.v(TAG, "New asset manager: " + this);
	}

	/**
	 * Return a global shared asset manager that provides access to only
	 * system assets (no application assets).
	 * {@hide}
	 */
	public static AssetManager getSystem() {
		ensureSystemAssets();
		return sSystem;
	}

	/**
	 * Close this asset manager.
	 */
	public void close() {
		synchronized (this) {
			// System.out.println("Release: num=" + mNumRefs
			//                    + ", released=" + mReleased);
			if (mOpen) {
				mOpen = false;
				decRefsLocked(this.hashCode());
			}
		}
	}

	/**
	 * Retrieve the string value associated with a particular resource
	 * identifier for the current configuration / skin.
	 */
	/*package*/ final CharSequence getResourceText(int id) {
		if (id == 0)
			return "";
		TypedValue value = new TypedValue();
		loadResourceValue(id, (short)0, value, true);
		return value.coerceToString();
	}

	/**
	 * Retrieve the string value associated with a particular resource
	 * identifier for the current configuration / skin.
	 */
	/*package*/ final CharSequence getResourceBagText(int ident, int bagEntryId) {
		TypedValue value = new TypedValue();
		int block = loadResourceBagValue(ident, bagEntryId, value, true);
		if (block >= 0) {
			return value.coerceToString();
		}
		return null;
	}

	/**
	 * Retrieve the string array associated with a particular resource
	 * identifier.
	 * @param id Resource id of the string array
	 */
	/*package*/ final String[] getResourceStringArray(final int id) {
		return getArrayStringResource(id);
	}

	/*package*/ final boolean getResourceValue(int ident,
	                                           int density,
	                                           TypedValue outValue,
	                                           boolean resolveRefs) {
		int block = loadResourceValue(ident, (short)density, outValue, resolveRefs);
		return block >= 0;
	}

	/**
	 * Retrieve the text array associated with a particular resource
	 * identifier.
	 * @param id Resource id of the string array
	 */
	/*package*/ final CharSequence[] getResourceTextArray(final int id) {
		int n = getArraySize(id);
		int[] valueArray = new int[n * STYLE_NUM_ENTRIES];
		retrieveArray(id, valueArray);
		CharSequence[] values = new String[n];
		TypedValue value = new TypedValue();
		for (int i = 0; i < n; i++) {
			value.data = valueArray[i * STYLE_NUM_ENTRIES + STYLE_DATA];
			value.type = valueArray[i * STYLE_NUM_ENTRIES + STYLE_TYPE];
			value.assetCookie = valueArray[i * STYLE_NUM_ENTRIES + STYLE_ASSET_COOKIE];
			if (value.type == TypedValue.TYPE_STRING) {
				values[i] = getPooledString(value.assetCookie, value.data);
			} else {
				values[i] = value.coerceToString();
			}
		}
		return values;
	}

	/*package*/ final boolean getThemeValue(long style, int ident,
	                                        TypedValue outValue, boolean resolveRefs) {
		int block = loadThemeAttributeValue(style, ident, outValue, resolveRefs);
		return block >= 0;
	}

	/*package*/ final void ensureStringBlocks() {
		if (mStringBlocks == null) {
			synchronized (this) {
				if (mStringBlocks == null) {
					makeStringBlocks(true);
				}
			}
		}
	}

	/*package*/ final void makeStringBlocks(boolean copyFromSystem) {
		final int sysNum = copyFromSystem ? sSystem.mStringBlocks.length : 0;
		final int num = getStringBlockCount();
		mStringBlocks = new StringBlock[num];
		if (localLOGV)
			Log.v(TAG, "Making string blocks for " + this + ": " + num);
		for (int i = 0; i < num; i++) {
			if (i < sysNum) {
				mStringBlocks[i] = sSystem.mStringBlocks[i];
			} else {
				mStringBlocks[i] = new StringBlock(getNativeStringBlock(i), true);
			}
		}
	}

	/*package*/ native final CharSequence getPooledString(int block, int id);

	/**
	 * Open an asset using ACCESS_STREAMING mode.  This provides access to
	 * files that have been bundled with an application as assets -- that is,
	 * files placed in to the "assets" directory.
	 *
	 * @param fileName The name of the asset to open.  This name can be
	 *                 hierarchical.
	 *
	 * @see #open(String, int)
	 * @see #list
	 */
	public final InputStream open(String fileName) throws IOException {
		return open(fileName, ACCESS_STREAMING);
	}

	/**
	 * Open an asset using an explicit access mode, returning an InputStream to
	 * read its contents.  This provides access to files that have been bundled
	 * with an application as assets -- that is, files placed in to the
	 * "assets" directory.
	 *
	 * @param fileName The name of the asset to open.  This name can be
	 *                 hierarchical.
	 * @param accessMode Desired access mode for retrieving the data.
	 *
	 * @see #ACCESS_UNKNOWN
	 * @see #ACCESS_STREAMING
	 * @see #ACCESS_RANDOM
	 * @see #ACCESS_BUFFER
	 * @see #open(String)
	 * @see #list
	 */
	public final InputStream open(String fileName, int accessMode) throws IOException {
		long asset = openAsset("assets/" + fileName, accessMode);
		if (asset == 0)
			throw new FileNotFoundException("file: " + fileName);
		return new AssetInputStream(asset);
	}

	public final AssetFileDescriptor openFd(String fileName) throws IOException {
		return openFd_internal("assets/" + fileName, 0);
	}

	/**
	 * Return a String array of all the assets at the given path.
	 *
	 * @param path A relative path within the assets, i.e., "docs/home.html".
	 *
	 * @return String[] Array of strings, one for each asset.  These file
	 *         names are relative to 'path'.  You can open the file by
	 *         concatenating 'path' and a name in the returned string (via
	 *         File) and passing that to open().
	 *
	 * @see #open
	 */
	public native final String[] list(String path)
	    throws IOException;

	/**
	 * {@hide}
	 * Open a non-asset file as an asset using ACCESS_STREAMING mode.  This
	 * provides direct access to all of the files included in an application
	 * package (not only its assets).  Applications should not normally use
	 * this.
	 *
	 * @see #open(String)
	 */
	public final InputStream openNonAsset(String fileName) throws IOException {
		return openNonAsset(0, fileName, ACCESS_STREAMING);
	}

	/**
	 * {@hide}
	 * Open a non-asset file as an asset using a specific access mode.  This
	 * provides direct access to all of the files included in an application
	 * package (not only its assets).  Applications should not normally use
	 * this.
	 *
	 * @see #open(String, int)
	 */
	public final InputStream openNonAsset(String fileName, int accessMode)
	    throws IOException {
		return openNonAsset(0, fileName, accessMode);
	}

	/**
	 * {@hide}
	 * Open a non-asset in a specified package.  Not for use by applications.
	 *
	 * @param cookie Identifier of the package to be opened.
	 * @param fileName Name of the asset to retrieve.
	 */
	public final InputStream openNonAsset(int cookie, String fileName)
	    throws IOException {
		return openNonAsset(cookie, fileName, ACCESS_STREAMING);
	}

	/**
	 * {@hide}
	 * Open a non-asset in a specified package.  Not for use by applications.
	 *
	 * @param cookie Identifier of the package to be opened.
	 * @param fileName Name of the asset to retrieve.
	 * @param accessMode Desired access mode for retrieving the data.
	 */
	public final InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
		long asset = openAsset(fileName, accessMode);
		if (asset == 0)
			throw new FileNotFoundException("file: " + fileName);
		return new AssetInputStream(asset);
	}

	public final AssetFileDescriptor openNonAssetFd(String fileName)
	    throws IOException {
		return openNonAssetFd(0, fileName);
	}

	public final AssetFileDescriptor openNonAssetFd(int cookie,
	                                                String fileName) throws IOException {
		return openFd_internal(fileName, 0);
	}

	private final AssetFileDescriptor openFd_internal(String fileName, int accessMode)
	    throws IOException {
		int asset;
		synchronized (this) {
			if (!mOpen) {
				throw new RuntimeException("Assetmanager has been closed");
			}
			long[] offset = new long[1];
			long[] length = new long[1];
			asset = openAssetFd(fileName, accessMode, offset, length);
			if (asset < 0)
				throw new FileNotFoundException("file: " + fileName + ", error: " + asset);

			FileDescriptor fd = new FileDescriptor();
			fd.setInt$(asset);
			ParcelFileDescriptor pfd = new ParcelFileDescriptor(fd);
			if (pfd != null) {
				AssetFileDescriptor afd = new AssetFileDescriptor(pfd, offset[0], length[0]);
				afd.fileName = fileName;
				return afd;
			}
		}
		throw new FileNotFoundException("file: " + fileName);
	}

	/**
	 * Retrieve a parser for a compiled XML file.
	 *
	 * @param fileName The name of the file to retrieve.
	 */
	public final XmlResourceParser openXmlResourceParser(String fileName)
	    throws /*IO*/ Exception {
		return openXmlResourceParser(0, fileName);
	}

	/**
	 * Retrieve a parser for a compiled XML file.
	 *
	 * @param cookie Identifier of the package to be opened.
	 * @param fileName The name of the file to retrieve.
	 */
	public final XmlResourceParser openXmlResourceParser(int cookie,
	                                                     String fileName) throws IOException {
		XmlBlock block = openXmlBlockAsset(cookie, fileName);
		XmlResourceParser rp = block.newParser();
		block.close();
		return rp;
	}

	/**
	 * {@hide}
	 * Retrieve a non-asset as a compiled XML file.  Not for use by
	 * applications.
	 *
	 * @param fileName The name of the file to retrieve.
	 */
	/*package*/ final XmlBlock openXmlBlockAsset(String fileName)
	    throws IOException {
		return openXmlBlockAsset(0, fileName);
	}

	/**
	 * {@hide}
	 * Retrieve a non-asset as a compiled XML file.  Not for use by
	 * applications.
	 *
	 * @param cookie Identifier of the package to be opened.
	 * @param fileName Name of the asset to retrieve.
	 */
	/*package*/ final XmlBlock openXmlBlockAsset(int cookie, String fileName) throws IOException {
		long xmlBlock;
		synchronized (this) {
			if (!mOpen) {
				throw new RuntimeException("Assetmanager has been closed");
			}
			xmlBlock = openXmlAssetNative(cookie, fileName);
			if (xmlBlock != 0) {
				XmlBlock res = new XmlBlock(this, xmlBlock);
				incRefsLocked(res.hashCode());
				return res;
			}
		}
		throw new FileNotFoundException("Asset XML file: " + fileName + ", errno : " + xmlBlock);
	}

	/*package*/ void xmlBlockGone(int id) {
		synchronized (this) {
			decRefsLocked(id);
		}
	}

	/*package*/ final long createTheme() {
		synchronized (this) {
			if (!mOpen) {
				throw new RuntimeException("Assetmanager has been closed");
			}
			long res = newTheme();
			incRefsLocked(new Long(res).hashCode());
			return res;
		}
	}

	/*package*/ final void releaseTheme(long theme) {
		synchronized (this) {
			// deleteTheme(theme);
			decRefsLocked(new Long(theme).hashCode());
		}
	}

	protected void finalize() throws Throwable {
		try {
			if (DEBUG_REFS && mNumRefs != 0) {
				Log.w(TAG, "AssetManager " + this + " finalized with non-zero refs: " + mNumRefs);
				if (mRefStacks != null) {
					for (RuntimeException e : mRefStacks.values()) {
						Log.w(TAG, "Reference from here", e);
					}
				}
			}
			destroy();
		} finally {
			super.finalize();
		}
	}

	public final class AssetInputStream extends InputStream {
		private AssetInputStream(long asset) {
			mAsset = asset;
			mLength = getAssetLength(asset);
		}
		public final int read() throws IOException {
			return readAssetChar(mAsset);
		}
		public final boolean markSupported() {
			return true;
		}
		public final int available() throws IOException {
			long len = getAssetRemainingLength(mAsset);
			return len > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)len;
		}
		public final void close() throws IOException {
			synchronized (AssetManager.this) {
				if (mAsset != 0) {
					destroyAsset(mAsset);
					mAsset = 0;
					decRefsLocked(hashCode());
				}
			}
		}
		public final void mark(int readlimit) {
			mMarkPos = seekAsset(mAsset, 0, 0);
		}
		public final void reset() throws IOException {
			seekAsset(mAsset, mMarkPos, -1);
		}
		public final int read(byte[] b) throws IOException {
			return readAsset_internal(mAsset, b, 0, b.length);
		}
		public final int read(byte[] b, int off, int len) throws IOException {
			return readAsset_internal(mAsset, b, off, len);
		}
		public final long skip(long n) throws IOException {
			long pos = seekAsset(mAsset, 0, 0);
			if ((pos + n) > mLength) {
				n = mLength - pos;
			}
			if (n > 0) {
				seekAsset(mAsset, n, 0);
			}
			return n;
		}

		protected void finalize() throws Throwable {
			close();
		}

		private long mAsset;
		private long mLength;
		private long mMarkPos;
	}

	private int readAsset_internal(long asset, byte[] b, long offset, long length) throws IOException {
		int ret = readAsset(asset, b, offset, length);
		if (ret < 0)
			throw new IOException();
		if (ret == 0)
			ret = -1;
		return ret;
	}

	/**
	 * Add an additional set of assets to the asset manager.  This can be
	 * either a directory or ZIP file.  Not for use by applications.  Returns
	 * the cookie of the added asset, or 0 on failure.
	 * {@hide}
	 */
	/* this is not particularly efficient, avoid if possible */
	public final void addAssetPath(String path) {
		asset_paths.add(path);
		/*String*/ Object[] asset_paths_arr = asset_paths.toArray();
		native_setApkAssets(asset_paths_arr, asset_paths_arr.length);
	}

	private native final int addAssetPathNative(String path);

	public static void extractFromAPK(String apk_path, String path, String target) throws IOException {
		if (path.endsWith("/")) { // directory
			try (JarFile apk = new JarFile(apk_path)) {
				Enumeration<JarEntry> entries = apk.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (entry.getName().startsWith(path)) {
						extractFromAPK(apk_path, entry.getName(), entry.getName().replace(path, target));
					}
				}
			}
		} else { // single file
			Path file = Paths.get(android.os.Environment.getExternalStorageDirectory().getPath(), target);
			if (!Files.exists(file) || Files.getLastModifiedTime(file).toMillis() < Files.getLastModifiedTime(Paths.get(apk_path)).toMillis()) {
				try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(path)) {
					if (inputStream != null) {
						Files.createDirectories(file.getParent());
						Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		}
	}

	/**
	 * Determine whether the state in this asset manager is up-to-date with
	 * the files on the filesystem.  If false is returned, you need to
	 * instantiate a new AssetManager class to see the new data.
	 * {@hide}
	 */
	public native final boolean isUpToDate();

	/**
	 * Change the locale being used by this asset manager.  Not for use by
	 * applications.
	 * {@hide}
	 */
	public native final void setLocale(String locale);

	/**
	 * Get the locales that this asset manager contains data for.
	 */
	public native final String[] getLocales();

	/**
	 * Change the configuation used when retrieving resources.  Not for use by
	 * applications.
	 * {@hide}
	 */
	public native final void setConfiguration(int mcc, int mnc, String locale,
	                                          int orientation, int touchscreen, int density, int keyboard,
	                                          int keyboardHidden, int navigation, int screenWidth, int screenHeight,
	                                          int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
	                                          int screenLayout, int uiMode, int majorVersion);

	/**
	 * Retrieve the resource identifier for the given resource name.
	 */
	/*package*/ native final int getResourceIdentifier(String name, String type, String defPackage);

	public native final String getResourceName(int resid);
	/*package*/ native final String getResourcePackageName(int resid);
	/*package*/ native final String getResourceTypeName(int resid);
	/*package*/ native final String getResourceEntryName(int resid);

	private native final long openAsset(String fileName, int accessMode);
	private native final int openAssetFd(String fileName, int accessMode, long[] offset, long[] length);

	private native final void destroyAsset(long asset);
	private native final int readAssetChar(long asset);
	private native final int readAsset(long asset, byte[] b, long offset, long length);
	private native final long seekAsset(long asset, long offset, int whence);
	private native final long getAssetLength(long asset);
	private native final long getAssetRemainingLength(long asset);

	/**
	 * Returns true if the resource was found, filling in mRetStringBlock and
	 *  mRetData.
	 */
	private native final int loadResourceValue(int ident, short density, TypedValue outValue,
	                                           boolean resolve);

	/**
	 * Returns true if the resource was found, filling in mRetStringBlock and
	 *  mRetData.
	 */
	private native final int loadResourceBagValue(int ident, int bagEntryId, TypedValue outValue,
	                                              boolean resolve);
	/*package*/ static final int STYLE_NUM_ENTRIES = 7;
	/*package*/ static final int STYLE_TYPE = 0;
	/*package*/ static final int STYLE_DATA = 1;
	/*package*/ static final int STYLE_ASSET_COOKIE = 2;
	/*package*/ static final int STYLE_RESOURCE_ID = 3;
	/*package*/ static final int STYLE_CHANGING_CONFIGURATIONS = 4;
	/*package*/ static final int STYLE_DENSITY = 5;
	/*package*/ static final int STYLE_SOURCE_RESOURCE_ID = 6;

	/*package*/ native final void applyStyle(long theme, long parser,
	                                         int defStyleAttr, int defStyleRes,
	                                         int[] inAttrs, int length,
	                                         long outValuesAddress, long outIndicesAddress);

	/*package*/ native final boolean resolveAttrs(long theme, int defStyleAttr,
	                                              int defStyleRes, int[] inValues,
	                                              int[] inAttrs, int[] outValues,
	                                              int[] outIndices);
	/*package*/ native final boolean retrieveAttributes(long xmlParser,
	                                                    int[] inAttrs, int length,
	                                                    long outValuesAddress, long outIndicesAddress);

	/*package*/ native final int getArraySize(int resource);
	/*package*/ native final int retrieveArray(int resource, int[] outValues);
	private native final int getStringBlockCount();
	private native final int getNativeStringBlock(int block);

	/**
	 * {@hide}
	 */
	public native final String getCookieName(int cookie);

	/**
	 * {@hide}
	 */
	public native static final int getGlobalAssetCount();

	/**
	 * {@hide}
	 */
	public native static final String getAssetAllocations();

	/**
	 * {@hide}
	 */
	public native static final int getGlobalAssetManagerCount();

	private native final long newTheme();
	private native final void deleteTheme(long theme);
	/*package*/ native final void applyThemeStyle(long theme, int styleRes, boolean force);
	/*package*/ native final void copyTheme(long dest, long source);
	/*package*/ native final int loadThemeAttributeValue(long theme, int ident,
	                                                     TypedValue outValue,
	                                                     boolean resolve);
	/*package*/ native static final void dumpTheme(long theme, int priority, String tag, String prefix);

	private native final long openXmlAssetNative(int cookie, String fileName);

	private native final String[] getArrayStringResource(int arrayRes);
	private native final int[] getArrayStringInfo(int arrayRes);
	/*package*/ final int[] getArrayIntResource(int arrayRes) {
		int n = getArraySize(arrayRes);
		int[] valueArray = new int[n * STYLE_NUM_ENTRIES];
		retrieveArray(arrayRes, valueArray);
		int[] values = new int[n];
		TypedValue value = new TypedValue();
		for (int i = 0; i < n; i++) {
			value.data = valueArray[i * STYLE_NUM_ENTRIES + STYLE_DATA];
			value.type = valueArray[i * STYLE_NUM_ENTRIES + STYLE_TYPE];
			value.assetCookie = valueArray[i * STYLE_NUM_ENTRIES + STYLE_ASSET_COOKIE];
			values[i] = value.data;
		}
		return values;
	}

	private native final void init(int sdk_version);
	private /*native*/ final void destroy() {
		Slog.w(TAG, "AssetManager.destroy(): STUB");
	}

	private final void incRefsLocked(int id) {
		if (DEBUG_REFS) {
			if (mRefStacks == null) {
				mRefStacks = new HashMap<Integer, RuntimeException>();
				RuntimeException ex = new RuntimeException();
				ex.fillInStackTrace();
				mRefStacks.put(this.hashCode(), ex);
			}
		}
		mNumRefs++;
	}

	private final void decRefsLocked(int id) {
		if (DEBUG_REFS && mRefStacks != null) {
			mRefStacks.remove(id);
		}
		mNumRefs--;
		// System.out.println("Dec streams: mNumRefs=" + mNumRefs
		//                    + " mReleased=" + mReleased);
		if (mNumRefs == 0) {
			destroy();
		}
	}

	private native final void native_setApkAssets(/*String*/ Object[] paths, int num_assets);
}
