package android.atl;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.res.Resources;
import dalvik.system.PathClassLoader;

/**
 * Only used if MicroG/GMS is not installed, do not use this outside {@link ATLLoadedAppManager}.
 */
final class ATLGStub {
	// These values were extracted from MicroG.
	private static final String GMS_VERSION = "25.09.32";
	private static final String VENDING_VERSION = "40.2.26";
	private static final int GMS_VERSION_CODE = 250932030;
	private static final int VENDING_VERSION_CODE = 84022630;
	private static final int GOOGLE_PLAY_SERVICES_VERSION_INT = 12451000;
	private static final PathClassLoader pathClassLoader = new PathClassLoader(
	    ATLPaths.gstub_jar.getPath(), null, ATLGStub.class.getClassLoader());

	private static PackageParser.Package makePackageBase(String packageName) {
		PackageParser.Package pkg = new PackageParser.Package(packageName);
		pkg.applicationInfo.metaData.putInt(
		    "com.google.android.gms.version",
		    GOOGLE_PLAY_SERVICES_VERSION_INT);
		pkg.applicationInfo.sourceDir = ATLPaths.gstub_jar.getPath();
		ATLSigHelper.addGMSSignatures(pkg);
		pkg.applicationInfo.flags = ApplicationInfo.FLAG_SYSTEM;
		return pkg;
	}

	private static ATLLoadedApp loadFakeGMS() {
		PackageParser.Package pkg = makePackageBase("com.google.android.gms");
		pkg.mVersionName = GMS_VERSION;
		pkg.mVersionCode = GMS_VERSION_CODE;
		return new ATLLoadedApp(Resources.getSystem(), pathClassLoader, pkg);
	}

	private static ATLLoadedApp loadFakeGSF() {
		PackageParser.Package pkg = makePackageBase("com.google.android.gsf");
		return new ATLLoadedApp(Resources.getSystem(), pathClassLoader, pkg);
	}

	private static ATLLoadedApp loadFakeVending() {
		PackageParser.Package pkg = makePackageBase("com.android.vending");
		pkg.mVersionName = VENDING_VERSION;
		pkg.mVersionCode = VENDING_VERSION_CODE;
		return new ATLLoadedApp(Resources.getSystem(), pathClassLoader, pkg);
	}

	static ATLLoadedApp loadFakePlayServices(String packageName) {
		switch (packageName) {
			case "com.google.android.gms":
				return loadFakeGMS();
			case "com.google.android.gsf":
				return loadFakeGSF();
			case "com.android.vending":
				return loadFakeVending();
			default: // Should be unreachable...
				throw new IllegalArgumentException("Invalid package name: " + packageName);
		}
	}
}
