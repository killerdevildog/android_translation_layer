package android.os.storage;

import android.content.Context;
import android.os.Environment;
import java.io.File;

public class StorageVolume {
	public boolean isPrimary() { return true; }
	public boolean isRemovable() { return false; }
	public boolean isEmulated() { return true; }
	public String getPath() { return Environment.getExternalStorageDirectory().getAbsolutePath(); }
	public File getDirectory() { return Environment.getExternalStorageDirectory(); }
	public String getDescription(Context context) { return "Internal Storage"; }
	public String getState() { return Environment.MEDIA_MOUNTED; }
	public String getUuid() { return null; }
	public String getMediaStoreVolumeName() { return "external_primary"; }
}
