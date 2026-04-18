package android.preference;

public class Preference {
	public interface OnPreferenceChangeListener {
		public boolean onPreferenceChange(Preference preference, Object newValue);
	}
	public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {}
}
