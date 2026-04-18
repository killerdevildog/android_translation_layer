package android.atl;

import android.os.Parcel;

public class GVariantIterParcel extends Parcel {

	private long iter;

	public GVariantIterParcel(long iter) {
		this.iter = iter;
	}

	@Override
	public byte readByte() {
		return native_readByte(iter);
	}

	@Override
	public int readInt() {
		return native_readInt(iter);
	}

	@Override
	public String readString() {
		return native_readString(iter);
	}

	@Override
	public void writeByte(byte value) {
		throw new UnsupportedOperationException("parcel is read-only");
	}

	protected static native byte native_readByte(long iter);
	protected static native int native_readInt(long iter);
	protected static native String native_readString(long iter);
}
