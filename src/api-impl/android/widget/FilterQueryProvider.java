package android.widget;

import android.database.Cursor;

public interface FilterQueryProvider {

	public Cursor runQuery(CharSequence constraint);
}
