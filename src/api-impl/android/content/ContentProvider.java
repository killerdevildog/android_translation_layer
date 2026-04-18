package android.content;

import android.atl.ATLMediaContentProvider;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public abstract class ContentProvider {

	static Map<String, ContentProvider> providers = new HashMap<String, ContentProvider>();

	static void createContentProviders() {
		for (PackageParser.Provider provider_parsed : Context.pkg.providers) {
			String process_name = provider_parsed.info.processName;
			if (process_name != null && process_name.contains(":")) {
				/* NOTE: even if it doesn't contain `:`, if it's not null we probably
				 * need to check what it's requesting; `:` means it wants us to spawn
				 * a new process, which we currently don't support */
				System.out.println("not creating provider " + provider_parsed.className + ", it wants to be started in a new process (" + process_name + ")");
				continue;
			}
			try {
				String providerName = provider_parsed.className;
				System.out.println("creating " + providerName);
				Class<? extends ContentProvider> providerCls = Class.forName(providerName).asSubclass(ContentProvider.class);
				ContentProvider provider = providerCls.getConstructor().newInstance();
				provider.attachInfo(Context.this_application, provider_parsed.info);
				provider.onCreate();
				providers.put(provider_parsed.info.authority, provider);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		providers.put("media", new ATLMediaContentProvider());
	}

	public boolean onCreate() { return false; }

	public Context getContext() {
		return Context.this_application;
	}

	public abstract Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

	public abstract Uri insert(Uri uri, ContentValues values);

	public abstract int update(Uri uri, ContentValues values, String selection, String[] selectionArgs);

	public abstract int delete(Uri uri, String selection, String[] selectionArgs);

	public abstract String getType(Uri uri);

	public abstract ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException;

	public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
		ParcelFileDescriptor fd = openFile(uri, mode);
		return fd != null ? new AssetFileDescriptor(fd, 0, -1) : null;
	}

	public void attachInfo(Context context, ProviderInfo provider) {}

	public String getCallingPackage() {
		return Context.pkg.packageName;
	}
}
