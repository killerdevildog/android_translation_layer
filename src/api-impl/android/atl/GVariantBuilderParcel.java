package android.atl;

import android.os.Parcel;

public class GVariantBuilderParcel extends Parcel {

	private long builder;

	public GVariantBuilderParcel(long builder) {
		this.builder = builder;
	}

	@Override
	public void writeByte(byte value) {
		native_writeByte(builder, value);
	}

	@Override
	public void writeInt(int value) {
		native_writeInt(builder, value);
	}

	@Override
	public void writeString(String value) {
		native_writeString(builder, value);
	}

	@Override
	public byte readByte() {
		throw new UnsupportedOperationException("parcel is write-only");
	}

	protected static native void native_writeByte(long builder, byte b);
	protected static native void native_writeInt(long builder, int i);
	protected static native void native_writeString(long builder, String s);
}
