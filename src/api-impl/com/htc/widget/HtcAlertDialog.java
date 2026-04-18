package com.htc.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class HtcAlertDialog extends AlertDialog {

	public static class Builder {

		private HtcAlertDialog dialog;

		public Builder(Context context) {
			dialog = new HtcAlertDialog(context);
		}

		public Builder setIcon(int icon) {
			return this;
		}

		public Builder setTitle(int title) {
			dialog.setTitle(dialog.getContext().getText(title));
			return this;
		}

		public Builder setMessage(int message) {
			dialog.setMessage(dialog.getContext().getString(message));
			return this;
		}

		public Builder setPositiveButton(int text, DialogInterface.OnClickListener listener) {
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, dialog.getContext().getText(text), listener);
			return this;
		}

		public Builder setNegativeButton(int text, DialogInterface.OnClickListener listener) {
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, dialog.getContext().getText(text), listener);
			return this;
		}

		public HtcAlertDialog create() {
			return dialog;
		}
	}

	public HtcAlertDialog(Context context) {
		super(context);
	}
}
