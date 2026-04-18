package android.view;

import android.content.Context;
import android.util.AttributeSet;

public class TextureView extends View {

	private SurfaceTextureListener surfaceTextureListener;

	public TextureView(Context context) {
		super(context);
	}

	public TextureView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public interface SurfaceTextureListener {}

	public void setSurfaceTextureListener(SurfaceTextureListener surfaceTextureListener) {
		this.surfaceTextureListener = surfaceTextureListener;
	}

	public void setOpaque(boolean opaque) {}

	public SurfaceTextureListener getSurfaceTextureListener() {
		return surfaceTextureListener;
	}

	public boolean isAvailable() {
		return false;
	}
}
