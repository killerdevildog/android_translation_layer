package android.preference;

import android.app.ListActivity;

public class PreferenceActivity extends ListActivity {
	public void addPreferencesFromResource(int resource) {}
	public Preference findPreference(CharSequence sequence) {
		return new ListPreference();
	}
}
