package android.content;

import android.atl.ATLLoadedApp;
import android.atl.ATLMediaContentProvider;
import android.atl.ATLProvider;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class ContentProvider {

	static final HashMap<String, ATLProvider> atl_providers = new HashMap<>();

	static void createContentProviders() {
		atl_providers.put("media", new ATLProvider(new ATLMediaContentProvider()));
		ATLLoadedApp primary = ATLLoadedApp.getPrimaryApplication();
		for (PackageParser.Provider provider_parsed : primary.pkg.providers) {
			String process_name = provider_parsed.info.processName;
			if (process_name != null && process_name.contains(":")) {
				/* NOTE: even if it doesn't contain `:`, if it's not null we probably
				 * need to check what it's requesting; `:` means it wants us to spawn
				 * a new process, which we currently don't support */
				System.out.println("not creating provider " + provider_parsed.className + ", it wants to be started in a new process (" + process_name + ")");
				continue;
			}
			atl_providers.put(provider_parsed.info.authority,
			                  new ATLProvider(primary, provider_parsed));
		}
		// getContentProvider() initializes the content providers,
		// we also opportunistically remove the ones that failed to load
		atl_providers.values().removeIf(new Predicate<ATLProvider>() {
			@Override
			public boolean test(ATLProvider atlProvider) {
				return atlProvider.getContentProvider() == null;
			}
		});
	}

	static ContentProvider atl_get_content_provider(String authority) {
		ATLProvider atlProvider = atl_providers.get(authority);
		return atlProvider == null ? null : atlProvider.getContentProvider();
	}

	public boolean onCreate() { return false; }

	public Context getContext() {
		return ATLLoadedApp.getPrimaryApplication().getApplication();
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
		return ATLLoadedApp.getPrimaryApplication().pkg.packageName;
	}
}
