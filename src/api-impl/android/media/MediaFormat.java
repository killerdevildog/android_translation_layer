package android.media;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MediaFormat {

	private Map<String, Object> map = new HashMap<>();

	public void setString(String key, String value) {
		map.put(key, value);
	}

	public void setInteger(String key, int value) {
		map.put(key, value);
	}

	public void setByteBuffer(String key, ByteBuffer value) {
		map.put(key, value);
	}

	public void setFloat(String key, float value) {
		map.put(key, value);
	}

	public ByteBuffer getByteBuffer(String name) {
		return (ByteBuffer)map.get(name);
	}

	public int getInteger(String name) {
		return (int)map.get(name);
	}

	public boolean containsKey(String name) {
		return map.containsKey(name);
	}

	public String toString() {
		return map.toString();
	}

	public String getString(String name) {
		return (String)map.get(name);
	}

	public long getLong(String name) {
		return (long)map.get(name);
	}
}
