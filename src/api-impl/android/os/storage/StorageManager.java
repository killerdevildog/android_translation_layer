package android.os.storage;

import java.io.File;

public class StorageManager {
	public StorageVolume[] getVolumeList() {
		StorageVolume[] sVolumes = {new StorageVolume()};
		return sVolumes;
	}

	public StorageVolume getStorageVolume(File file) { return null; }
}
