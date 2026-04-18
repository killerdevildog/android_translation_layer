package android.text;

public interface Editable extends CharSequence {

	public class Factory {
		public static Factory getInstance() {
			return new Factory();
		}

		public Editable newEditable(CharSequence source) {
			return new SpannableStringBuilder(source);
		}
	}

	public Editable replace(int start, int end, CharSequence source, int destoff, int destlen);

	public Editable replace(int start, int end, CharSequence text);

	public InputFilter[] getFilters();

	public void setFilters(InputFilter[] filters);

	public Editable delete(int start, int end);

	public Editable insert(int where, CharSequence text);

	public void clear();

	public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind);
}
