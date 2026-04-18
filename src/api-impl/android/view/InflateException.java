package android.view;

public class InflateException extends RuntimeException {

	public InflateException(String string, Exception e) {
		super(string, e);
	}

	public InflateException(String string) {
		super(string);
	}
}
