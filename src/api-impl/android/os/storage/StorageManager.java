package android.os.storage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class StorageManager {
	public StorageVolume[] getVolumeList() {
		StorageVolume[] sVolumes = {new StorageVolume()};
		return sVolumes;
	}

	public List<StorageVolume> getStorageVolumes() {
		return Arrays.asList(getVolumeList());
	}

	public StorageVolume getStorageVolume(File file) { return null; }
}
