package android.graphics;

public class Camera {

	public void save() {}

	public void restore() {}

	public void translate(float dx, float dy, float dz) {}

	public void rotateY(float degrees) {}

	public void getMatrix(Matrix matrix) {
		matrix.reset();
	}
}
