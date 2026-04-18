package android.graphics;

import android.content.res.AssetManager;
import android.graphics.fonts.FontVariationAxis;
import java.nio.ByteBuffer;

public class FontFamily {

	public void addFontWeightStyle(String dummy, int dummy2, boolean dummy3) {}

	public boolean addFontFromAssetManager(AssetManager dummy, String dummy2, int dummy3, boolean dummy4, int dummy5, int dummy6, int dummy7, FontVariationAxis[] dummy8) {
		return true;
	}

	public boolean addFontFromBuffer(ByteBuffer dummy, int dummy2, FontVariationAxis[] dummy3, int dummy4, int dummy5) {
		return true;
	}

	public boolean freeze() { return true; }

	public void abortCreation() {}
}
