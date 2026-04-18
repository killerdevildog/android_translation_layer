package android.os;

import android.content.Context;

public class Binder implements IBinder {

	public void attachInterface(IInterface owner, String descriptor) {}

	public static void flushPendingCommands() {}

	public static long clearCallingIdentity() { return 0; }

	public static void restoreCallingIdentity(long identityToken) {}

	@Override
	public IInterface queryLocalInterface(String descriptor) { return null; }

	@Override
	public boolean transact(int code, Parcel data, Parcel reply, int flags) { return false; }

	public static int getCallingUid() { return Context.this_application.getApplicationInfo().uid; }

	public static int getCallingPid() { return 0; }

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Binder;
	}
}
