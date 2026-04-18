package android.view;

import android.atl.GskCanvas;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RenderNode {

	private long render_node = 0;
	private long transformed_node = 0;
	private List<RenderNode> children = new ArrayList<RenderNode>();
	private List<Long> children_nodes = new ArrayList<Long>();

	private float width = 0.0f;
	private float height = 0.0f;
	private float scaleX = 1.0f;
	private float scaleY = 1.0f;
	private float translationX = 0.0f;
	private float translationY = 0.0f;
	private float elevation = 0.0f;
	private float rotation = 0.0f;
	private float rotationX = 0.0f;
	private float rotationY = 0.0f;
	private float cameraDistance = 0.0f;
	private float pivotX = 0.0f;
	private float pivotY = 0.0f;
	private boolean clipToBounds = true;
	private boolean clipToOutline = false;
	private float alpha = 1.0f;
	private Outline outline = null;

	private native long nativeCreateSnapshot();
	private native long nativeCreateNode(long snapshot);
	private native long nativePatchNode(long node, long old_child, long new_child);
	private native long nativeTransform(long node, float scaleX, float scaleY, float translationX, float translationY, float rotation, float pivotX, float pivotY);
	private native long nativeClip(long node, float left, float top, float right, float bottom);
	private native void nativeUnref(long node);

	public static RenderNode create(String name, View view) {
		return new RenderNode();
	}

	public float getScaleX() {
		return scaleX;
	}

	public boolean setScaleX(float scaleX) {
		boolean changed = this.scaleX != scaleX;
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.scaleX = scaleX;
		return changed;
	}

	public float getScaleY() {
		return scaleY;
	}

	public boolean setScaleY(float scaleY) {
		boolean changed = this.scaleY != scaleY;
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.scaleY = scaleY;
		return changed;
	}

	public float getTranslationX() {
		return translationX;
	}

	public boolean setTranslationX(float translationX) {
		boolean changed = this.translationX != translationX;
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.translationX = translationX;
		return changed;
	}

	public float getTranslationY() {
		return translationY;
	}

	public boolean setTranslationY(float translationY) {
		boolean changed = this.translationY != translationY;
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.translationY = translationY;
		return changed;
	}

	public float getElevation() {
		return elevation;
	}

	public boolean setElevation(float elevation) {
		boolean changed = this.elevation != elevation;
		this.elevation = elevation;
		return changed;
	}

	public float getRotation() {
		return rotation;
	}

	public boolean setRotation(float rotation) {
		boolean changed = this.rotation != rotation;
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.rotation = rotation;
		return changed;
	}

	public float getRotationX() {
		return rotationX;
	}

	public boolean setRotationX(float rotationX) {
		boolean changed = this.rotationX != rotationX;
		this.rotationX = rotationX;
		return changed;
	}

	public float getRotationY() {
		return rotationY;
	}

	public boolean setRotationY(float rotationY) {
		boolean changed = this.rotationY != rotationY;
		this.rotationY = rotationY;
		return changed;
	}

	public float getCameraDistance() {
		return cameraDistance;
	}

	public boolean setCameraDistance(float cameraDistance) {
		boolean changed = this.cameraDistance != cameraDistance;
		this.cameraDistance = cameraDistance;
		return changed;
	}

	public float getPivotX() {
		return pivotX;
	}

	public boolean setPivotX(float pivotX) {
		boolean changed = this.pivotX != pivotX;
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.pivotX = pivotX;
		return changed;
	}

	public float getPivotY() {
		return pivotY;
	}

	public boolean setPivotY(float pivotY) {
		boolean changed = this.pivotY != pivotY;
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.pivotY = pivotY;
		return changed;
	}

	public boolean getClipToOutline() {
		return clipToOutline;
	}

	public boolean setClipToOutline(boolean clipToOutline) {
		boolean changed = this.clipToOutline != clipToOutline;
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.clipToOutline = clipToOutline;
		return changed;
	}

	public boolean setClipToBounds(boolean clipToBounds) {
		boolean changed = this.clipToBounds != clipToBounds;
		this.clipToBounds = clipToBounds;
		return changed;
	}

	public float getAlpha() {
		return alpha;
	}

	public boolean setAlpha(float alpha) {
		boolean changed = this.alpha != alpha;
		this.alpha = alpha;
		return changed;
	}

	public boolean isValid() {
		return render_node != 0;
	}

	public boolean setLeftTopRightBottom(int left, int top, int right, int bottom) {
		float old_width = width;
		float old_height = height;
		width = right - left;
		height = bottom - top;
		return old_width != width || old_height != height || setTranslationX(left) || setTranslationY(top);
	}

	public boolean offsetLeftAndRight(int offset) {
		return setTranslationX(translationX + offset);
	}

	public boolean offsetTopAndBottom(int offset) {
		return setTranslationY(translationY + offset);
	}

	public void discardDisplayList() {
		nativeUnref(render_node);
		nativeUnref(transformed_node);
		render_node = 0;
		transformed_node = 0;
	}

	public boolean setLayerType(int layerType) {
		return false;
	}

	public boolean hasOverlappingRendering() {
		return false;
	}

	public boolean setHasOverlappingRendering(boolean hasOverlappingRendering) {
		return false;
	}

	public boolean setLayerPaint(Paint paint) {
		return false;
	}

	public boolean setOutline(Outline outline) {
		boolean changed = Objects.equals(this.outline, outline);
		if (changed && transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
		this.outline = outline;
		return changed;
	}

	public long getGskNode() {
		long old_render_node = render_node;
		for (int i = 0; i < children.size(); i++) {
			long new_child_node = children.get(i).getGskNode();
			if (new_child_node != children_nodes.get(i)) {
				render_node = nativePatchNode(render_node, children_nodes.get(i), new_child_node);
				children_nodes.set(i, new_child_node);
			}
		}
		if (transformed_node == 0 || old_render_node != render_node) {
			nativeUnref(transformed_node);
			transformed_node = nativeTransform(render_node, scaleX, scaleY, translationX, translationY, rotation, pivotX, pivotY);
			if (clipToOutline) {
				Rect bounds = new Rect();
				outline.getRect(bounds);
				transformed_node = nativeClip(transformed_node, bounds.left, bounds.top, bounds.right, bounds.bottom);
			}
		}
		return transformed_node;
	}

	public DisplayListCanvas start(int width, int height) {
		this.width = width;
		this.height = height;
		children.clear();
		children_nodes.clear();
		return new GskCanvas(nativeCreateSnapshot()) {
			@Override
			public void drawRenderNode(RenderNode node) {
				super.drawRenderNode(node);
				children.add(node);
				children_nodes.add(node.transformed_node);
			}
		};
	}

	public void end(DisplayListCanvas canvas) {
		nativeUnref(render_node);
		render_node = nativeCreateNode(((GskCanvas)canvas).snapshot);
		((GskCanvas)canvas).snapshot = 0;
		if (transformed_node != 0) {
			nativeUnref(transformed_node);
			transformed_node = 0;
		}
	}

	public void getMatrix(Matrix matrix) {
		matrix.reset();
		matrix.setTranslate(translationX, translationY);
		matrix.preRotate(rotation, pivotX, pivotY);
		matrix.preScale(scaleX, scaleY, pivotX, pivotY);
	}
}
