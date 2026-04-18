package android.os;

import android.content.Context;
import android.os.Parcelable.Creator;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parcel {

	private DataInputStream is;
	private DataOutputStream os;
	private byte[] data;
	private int pos;
	private int size;
	private boolean hasFileDescriptors;

	protected Parcel() {
		data = new byte[0];
		is = new DataInputStream(new InputStream() {
			@Override
			public int read() {
				return readByte() & 0xff;
			}
		});
		os = new DataOutputStream(new OutputStream() {
			@Override
			public void write(int b) {
				writeByte((byte)b);
			}
		});
	}

	public static Parcel obtain() {
		return new Parcel();
	}

	public void recycle() {}

	public void writeByte(byte value) {
		if (pos >= data.length)
			data = Arrays.copyOf(data, data.length * 2 + 8);
		data[pos++] = (byte)value;
		if (pos > size)
			size = pos;
	}

	public byte readByte() {
		if (pos < size)
			return data[pos++];
		else
			return 0;
	}

	public void writeInt(int value) {
		try {
			os.writeInt(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public int readInt() {
		try {
			return is.readInt();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeShort(short value) {
		try {
			os.writeShort(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public short readShort() {
		try {
			return is.readShort();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeLong(long value) {
		try {
			os.writeLong(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public long readLong() {
		try {
			return is.readLong();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeFloat(float value) {
		try {
			os.writeFloat(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public float readFloat() {
		try {
			return is.readFloat();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeDouble(double value) {
		try {
			os.writeDouble(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public double readDouble() {
		try {
			return is.readDouble();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeBoolean(boolean value) {
		try {
			os.writeBoolean(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean readBoolean() {
		try {
			return is.readBoolean();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeIntArray(int[] value) {
		int len = value == null ? -1 : value.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeInt(value[i]);
	}

	public void readIntArray(int[] value) {
		int len = readInt();
		if (value.length != len)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < len; i++)
			value[i] = readInt();
	}

	public int[] createIntArray() {
		int len = readInt();
		if (len == -1)
			return null;
		int[] value = new int[len];
		for (int i = 0; i < len; i++)
			value[i] = readInt();
		return value;
	}

	public void writeLongArray(long[] value) {
		int len = value == null ? -1 : value.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeLong(value[i]);
	}

	public void readLongArray(long[] value) {
		int len = readInt();
		if (value.length != len)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < len; i++)
			value[i] = readLong();
	}

	public long[] createLongArray() {
		int len = readInt();
		if (len == -1)
			return null;
		long[] value = new long[len];
		for (int i = 0; i < len; i++)
			value[i] = readLong();
		return value;
	}

	public void writeByteArray(byte[] value) {
		int len = value == null ? -1 : value.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeByte(value[i]);
	}

	public void writeByteArray(byte[] value, int offset, int length) {
		if (value == null) {
			writeInt(-1);
			return;
		}
		if (offset < 0 || length < 0 || offset + length > value.length)
			throw new ArrayIndexOutOfBoundsException();
		writeInt(length);
		if (Context.pkg.applicationInfo.targetSdkVersion < 11) // Copy AOSP bug to pass CTS test
			offset = 0;
		for (int i = 0; i < length; i++)
			writeByte(value[offset + i]);
	}

	public void readByteArray(byte[] value) {
		int len = readInt();
		if (value.length != len)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < len; i++)
			value[i] = readByte();
	}

	public byte[] createByteArray() {
		int len = readInt();
		if (len == -1)
			return null;
		byte[] value = new byte[len];
		for (int i = 0; i < len; i++)
			value[i] = readByte();
		return value;
	}

	public void writeCharArray(char[] value) {
		int len = value == null ? -1 : value.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeShort((short)value[i]);
	}

	public void readCharArray(char[] value) {
		int len = readInt();
		if (value.length != len)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < len; i++)
			value[i] = (char)readShort();
	}

	public char[] createCharArray() {
		int len = readInt();
		if (len == -1)
			return null;
		char[] value = new char[len];
		for (int i = 0; i < len; i++)
			value[i] = (char)readShort();
		return value;
	}

	public void writeBooleanArray(boolean[] value) {
		int len = value == null ? -1 : value.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeBoolean(value[i]);
	}

	public void readBooleanArray(boolean[] value) {
		int len = readInt();
		if (value.length != len)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < len; i++)
			value[i] = readBoolean();
	}

	public boolean[] createBooleanArray() {
		int len = readInt();
		if (len == -1)
			return null;
		boolean[] value = new boolean[len];
		for (int i = 0; i < len; i++)
			value[i] = readBoolean();
		return value;
	}

	public void writeFloatArray(float[] value) {
		int len = value == null ? -1 : value.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeFloat(value[i]);
	}

	public void readFloatArray(float[] value) {
		int len = readInt();
		if (value.length != len)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < len; i++)
			value[i] = readFloat();
	}

	public float[] createFloatArray() {
		int len = readInt();
		if (len == -1)
			return null;
		float[] value = new float[len];
		for (int i = 0; i < len; i++)
			value[i] = readFloat();
		return value;
	}

	public void writeDoubleArray(double[] value) {
		int len = value == null ? -1 : value.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeDouble(value[i]);
	}

	public void readDoubleArray(double[] value) {
		int len = readInt();
		if (value.length != len)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < len; i++)
			value[i] = readDouble();
	}

	public double[] createDoubleArray() {
		int len = readInt();
		if (len == -1)
			return null;
		double[] value = new double[len];
		for (int i = 0; i < len; i++)
			value[i] = readDouble();
		return value;
	}

	public void writeString(String value) {
		try {
			if (value == null)
				os.writeShort(-1);
			else
				os.writeUTF(value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String readString() {
		try {
			int len = is.readShort();
			if (len == -1)
				return null;
			pos -= 2;
			return is.readUTF();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeBundle(Bundle value) {
		writeMap(value == null ? null : value.mMap);
	}

	public Bundle readBundle(ClassLoader loader) throws ReflectiveOperationException {
		int size = readInt();
		if (size == -1)
			return null;
		Bundle bundle = new Bundle();
		for (int i = 0; i < size; i++) {
			String key = readString();
			Object value = readValue(loader);
			bundle.mMap.put(key, value);
		}
		return bundle;
	}

	public Bundle readBundle() throws ReflectiveOperationException {
		return readBundle(getClass().getClassLoader());
	}

	public void writeInterfaceToken(String value) {
		writeString(value);
	}

	public void enforceInterface(String value) {
		if (!value.equals(readString()))
			throw new SecurityException("Interface mismatch");
	}

	public void writeParcelable(Parcelable p, int flags) {
		writeString(p != null ? p.getClass().getName() : null);
		if (p != null)
			p.writeToParcel(this, flags);
	}

	public Parcelable readParcelable(ClassLoader loader) throws ReflectiveOperationException {
		String className = readString();
		if (className == null)
			return null;
		Parcelable.Creator<?> creator = (Parcelable.Creator<?>)loader.loadClass(className).getField("CREATOR").get(null);
		return (Parcelable)creator.createFromParcel(this);
	}

	public void writeStringArray(String[] strings) {
		int len = strings == null ? -1 : strings.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeString(strings[i]);
	}

	public void readStringArray(String[] strings) {
		int size = readInt();
		if (strings.length != size)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < size; i++)
			strings[i] = readString();
	}

	public String[] createStringArray() {
		int len = readInt();
		if (len == -1)
			return null;
		String[] strings = new String[len];
		for (int i = 0; i < len; i++)
			strings[i] = readString();
		return strings;
	}

	public void writeStringList(List<String> strings) {
		int len = strings == null ? -1 : strings.size();
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeString(strings.get(i));
	}

	public void readStringList(List<String> strings) {
		int size = readInt();
		strings.clear();
		for (int i = 0; i < size; i++)
			strings.add(readString());
	}

	public ArrayList<String> createStringArrayList() {
		int len = readInt();
		if (len == -1)
			return null;
		ArrayList<String> strings = new ArrayList<String>(len);
		for (int i = 0; i < len; i++)
			strings.add(readString());
		return strings;
	}

	public void writeList(List<?> list) {
		int len = list == null ? -1 : list.size();
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeValue(list.get(i));
	}

	public void readList(List<Object> list, ClassLoader loader) throws ReflectiveOperationException {
		int size = readInt();
		list.clear();
		for (int i = 0; i < size; i++)
			list.add(readValue(loader));
	}

	public void writeMap(Map<String, ?> map) {
		if (map == null) {
			writeInt(-1);
			return;
		}
		writeInt(map.size());
		for (Map.Entry<String, ?> entry : map.entrySet()) {
			writeString(entry.getKey());
			writeValue(entry.getValue());
		}
	}

	public void readMap(Map<String, Object> map, ClassLoader loader) throws ReflectiveOperationException {
		int size = readInt();
		map.clear();
		for (int i = 0; i < size; i++) {
			String key = readString();
			Object value = readValue(loader);
			map.put(key, value);
		}
	}

	public HashMap<String, Object> readHashMap(ClassLoader loader) throws ReflectiveOperationException {
		int size = readInt();
		if (size == -1)
			return null;
		HashMap<String, Object> map = new HashMap<>(size);
		for (int i = 0; i < size; i++) {
			String key = readString();
			Object value = readValue(loader);
			map.put(key, value);
		}
		return map;
	}

	public void writeSparseArray(SparseArray<?> array) {
		int len = array == null ? -1 : array.size();
		writeInt(len);
		for (int i = 0; i < len; i++) {
			writeInt(array.keyAt(i));
			writeValue(array.valueAt(i));
		}
	}

	public SparseArray<Object> readSparseArray(ClassLoader loader) throws ReflectiveOperationException {
		int size = readInt();
		if (size == -1)
			return null;
		SparseArray<Object> array = new SparseArray<>(size);
		for (int i = 0; i < size; i++) {
			int key = readInt();
			Object value = readValue(loader);
			array.put(key, value);
		}
		return array;
	}

	public void writeSparseBooleanArray(SparseBooleanArray array) {
		int len = array == null ? -1 : array.size();
		writeInt(len);
		for (int i = 0; i < len; i++) {
			writeInt(array.keyAt(i));
			writeBoolean(array.valueAt(i));
		}
	}

	public SparseBooleanArray readSparseBooleanArray() {
		int size = readInt();
		if (size == -1)
			return null;
		SparseBooleanArray array = new SparseBooleanArray(size);
		for (int i = 0; i < size; i++) {
			int key = readInt();
			boolean value = readBoolean();
			array.put(key, value);
		}
		return array;
	}

	public void writeParcelableArray(Parcelable[] array, int flags) {
		int len = array == null ? -1 : array.length;
		writeInt(len);
		for (int i = 0; i < len; i++)
			writeParcelable(array[i], flags);
	}

	public Parcelable[] readParcelableArray(ClassLoader loader) throws ReflectiveOperationException {
		int size = readInt();
		if (size == -1)
			return null;
		Parcelable[] array = new Parcelable[size];
		for (int i = 0; i < size; i++)
			array[i] = readParcelable(loader);
		return array;
	}

	public void writeTypedArray(Parcelable[] array, int flags) {
		int len = array == null ? -1 : array.length;
		writeInt(len);
		for (int i = 0; i < len; i++) {
			writeBoolean(array[i] != null);
			if (array[i] != null)
				array[i].writeToParcel(this, flags);
		}
	}

	public void readTypedArray(Object[] array, Parcelable.Creator<Parcelable> creator) throws ReflectiveOperationException {
		int size = readInt();
		if (array.length != size)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < size; i++) {
			if (readBoolean())
				array[i] = creator.createFromParcel(this);
			else
				array[i] = null;
		}
	}

	public Object[] createTypedArray(Parcelable.Creator<Parcelable> creator) {
		int size = readInt();
		if (size == -1)
			return null;
		Object[] array = creator.newArray(size);
		for (int i = 0; i < size; i++) {
			if (readBoolean())
				array[i] = creator.createFromParcel(this);
		}
		return array;
	}

	public void writeTypedList(List<Parcelable> list) {
		int len = list == null ? -1 : list.size();
		writeInt(len);
		for (int i = 0; i < len; i++) {
			writeBoolean(list.get(i) != null);
			if (list.get(i) != null)
				list.get(i).writeToParcel(this, 0);
		}
	}

	public void readTypedList(List<Parcelable> list, Parcelable.Creator<Parcelable> creator) throws ReflectiveOperationException {
		int size = readInt();
		list.clear();
		for (int i = 0; i < size; i++) {
			if (readBoolean())
				list.add(creator.createFromParcel(this));
			else
				list.add(null);
		}
	}

	public ArrayList<Parcelable> createTypedArrayList(Parcelable.Creator<Parcelable> creator) throws ReflectiveOperationException {
		int size = readInt();
		if (size == -1)
			return null;
		ArrayList<Parcelable> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			if (readBoolean())
				list.add(creator.createFromParcel(this));
			else
				list.add(null);
		}
		return list;
	}

	public void writeArray(Object[] array) {
		int len = array == null ? -1 : array.length;
		writeInt(len);
		for (int i = 0; i < len; i++) {
			writeValue(array[i]);
		}
	}

	public Object[] readArray(ClassLoader loader) throws ReflectiveOperationException {
		int size = readInt();
		if (size == -1)
			return null;
		Object[] array = new Object[size];
		for (int i = 0; i < size; i++)
			array[i] = readValue(loader);
		return array;
	}

	public ArrayList<Object> readArrayList(ClassLoader loader) throws ReflectiveOperationException {
		int size = readInt();
		if (size == -1)
			return null;
		ArrayList<Object> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
			list.add(readValue(loader));
		return list;
	}

	public void writeSerializable(Serializable serializable) {
		try {
			new ObjectOutputStream(os).writeObject(serializable);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Serializable readSerializable() throws ReflectiveOperationException {
		try {
			return (Serializable) new ObjectInputStream(is).readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeException(Exception e) throws Exception {
		if (!(e instanceof BadParcelableException || e instanceof IllegalArgumentException || e instanceof IllegalStateException || e instanceof NullPointerException || e instanceof SecurityException || e instanceof UnsupportedOperationException))
			throw e;
		writeSerializable(e);
	}

	public void writeNoException() {
		writeSerializable(null);
	}

	public void readException() throws Exception {
		Exception e = (Exception)readSerializable();
		if (e != null)
			throw e;
	}

	public void writeFileDescriptor(FileDescriptor fd) {
		if (fd == null) {
			writeInt(-1);
			return;
		}
		writeInt(fd.getInt$());
		hasFileDescriptors = true;
	}

	public ParcelFileDescriptor readFileDescriptor() {
		if (!hasFileDescriptors)
			return null;
		int fd = readInt();
		if (fd == -1)
			return null;
		FileDescriptor fd_ = new FileDescriptor();
		fd_.setInt$(fd);
		return new ParcelFileDescriptor(fd_);
	}

	public boolean hasFileDescriptors() {
		return hasFileDescriptors;
	}

	public void writeStrongBinder(IBinder binder) {
		writeInt(binder == null ? -1 : 0);
	}

	public IBinder readStrongBinder() {
		if (readInt() == -1)
			return null;
		return new Binder();
	}

	public void writeStrongInterface(IInterface binder) {
		writeInt(binder == null ? -1 : 0);
	}

	public void writeBinderArray(IBinder[] array) {
		int len = array == null ? -1 : array.length;
		writeInt(len);
		for (int i = 0; i < len; i++) {
			writeStrongBinder(array[i]);
		}
	}

	public void readBinderArray(IBinder[] array) {
		int size = readInt();
		if (array.length != size)
			throw new RuntimeException("array length mismatch");
		for (int i = 0; i < size; i++)
			array[i] = readStrongBinder();
	}

	public IBinder[] createBinderArray() {
		int size = readInt();
		if (size == -1)
			return null;
		IBinder[] array = new IBinder[size];
		for (int i = 0; i < size; i++)
			array[i] = readStrongBinder();
		return array;
	}

	public void writeBinderList(List<IBinder> list) {
		int len = list == null ? -1 : list.size();
		writeInt(len);
		for (int i = 0; i < len; i++) {
			writeStrongBinder(list.get(i));
		}
	}

	public void readBinderList(List<IBinder> list) {
		int size = readInt();
		list.clear();
		for (int i = 0; i < size; i++)
			list.add(readStrongBinder());
	}

	public ArrayList<IBinder> createBinderArrayList() {
		int size = readInt();
		if (size == -1)
			return null;
		ArrayList<IBinder> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
			list.add(readStrongBinder());
		return list;
	}

	@SuppressWarnings("unchecked")
	public void writeValue(Object value) {
		if (value == null) {
			writeInt(0);
		} else if (value instanceof Byte) {
			writeInt(1);
			writeByte(((Byte)value).byteValue());
		} else if (value instanceof Short) {
			writeInt(2);
			writeShort(((Short)value).shortValue());
		} else if (value instanceof Integer) {
			writeInt(3);
			writeInt(((Integer)value).intValue());
		} else if (value instanceof Long) {
			writeInt(4);
			writeLong(((Long)value).longValue());
		} else if (value instanceof Float) {
			writeInt(5);
			writeFloat(((Float)value).floatValue());
		} else if (value instanceof Double) {
			writeInt(6);
			writeDouble(((Double)value).doubleValue());
		} else if (value instanceof Boolean) {
			writeInt(7);
			writeBoolean(((Boolean)value).booleanValue());
		} else if (value instanceof String) {
			writeInt(8);
			writeString((String)value);
		} else if (value instanceof byte[]) {
			writeInt(9);
			writeByteArray((byte[])value);
		} else if (value instanceof int[]) {
			writeInt(10);
			writeIntArray((int[])value);
		} else if (value instanceof long[]) {
			writeInt(11);
			writeLongArray((long[])value);
		} else if (value instanceof boolean[]) {
			writeInt(12);
			writeBooleanArray((boolean[])value);
		} else if (value instanceof String[]) {
			writeInt(13);
			writeStringArray((String[])value);
		} else if (value instanceof Bundle) {
			writeInt(14);
			writeBundle((Bundle)value);
		} else if (value instanceof Parcelable) {
			writeInt(15);
			writeParcelable((Parcelable)value, 0);
		} else if (value instanceof Parcelable[]) {
			writeInt(16);
			writeParcelableArray((Parcelable[])value, 0);
		} else if (value instanceof Object[]) {
			writeInt(17);
			writeArray((Object[])value);
		} else if (value instanceof List) {
			writeInt(18);
			writeList((List<?>)value);
		} else if (value instanceof Map) {
			writeInt(19);
			writeMap((Map<String, ?>)value);
		} else if (value instanceof SparseArray) {
			writeInt(20);
			writeSparseArray((SparseArray<?>)value);
		} else if (value instanceof Binder) {
			writeInt(21);
		} else if (value instanceof Serializable) {
			writeInt(22);
			writeSerializable((Serializable)value);
		} else {
			throw new RuntimeException("Unsupported value type: " + value.getClass().getName());
		}
	}

	public Object readValue(ClassLoader loader) throws ReflectiveOperationException {
		int type;
		switch (type = readInt()) {
			case 0:
				return null;
			case 1:
				return readByte();
			case 2:
				return readShort();
			case 3:
				return readInt();
			case 4:
				return readLong();
			case 5:
				return readFloat();
			case 6:
				return readDouble();
			case 7:
				return readBoolean();
			case 8:
				return readString();
			case 9:
				return createByteArray();
			case 10:
				return createIntArray();
			case 11:
				return createLongArray();
			case 12:
				return createBooleanArray();
			case 13:
				return createStringArray();
			case 14:
				return readBundle(loader);
			case 15:
				return readParcelable(loader);
			case 16:
				return readParcelableArray(loader);
			case 17:
				return readArray(loader);
			case 18:
				return readArrayList(loader);
			case 19:
				return readHashMap(loader);
			case 20:
				return readSparseArray(loader);
			case 21:
				return new Binder();
			case 22:
				return readSerializable();
			default:
				throw new RuntimeException("Unsupported value type: " + type);
		}
	}

	public void setDataPosition(int pos) {
		this.pos = pos;
	}

	public int dataPosition() {
		return pos;
	}

	public void setDataSize(int size) {
		this.size = size;
	}

	public int dataSize() {
		return size;
	}

	public int dataAvail() {
		return size - pos;
	}

	public void setDataCapacity(int capacity) {
		if (capacity > data.length)
			data = Arrays.copyOf(data, capacity);
	}

	public int dataCapacity() {
		return data.length;
	}

	public void appendFrom(Parcel source, int start, int size) {
		try {
			os.write(source.data, start, size);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] marshall() {
		return Arrays.copyOf(data, size);
	}

	public void unmarshall(byte[] data, int offset, int length) {
		this.data = data;
		this.pos = offset;
		this.size = offset + length;
	}
}
