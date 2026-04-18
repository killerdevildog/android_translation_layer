package android.view;

import android.graphics.Canvas;

public class DisplayListCanvas extends Canvas {

	public void drawRenderNode(RenderNode node) {}

	@Override
	public boolean isHardwareAccelerated() {
		return true;
	}
}
