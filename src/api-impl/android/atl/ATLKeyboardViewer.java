package android.atl;

import android.app.Activity;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import java.lang.reflect.Constructor;

public class ATLKeyboardViewer extends Activity {
	@Override
	public void onCreate(Bundle savedState) {
		Bundle extras = this.getIntent().getExtras();

		if (extras == null || !extras.containsKey("kb_class")) {
			System.err.println("ATLKeyboardViewer: usage: `-e 'kb_class=com.example.LatinIME'`");
			System.exit(1);
		}

		String kb_class = extras.getString("kb_class");

		InputMethodService ims = null;

		try {
			Class<? extends InputMethodService> cls = Class.forName(kb_class).asSubclass(InputMethodService.class);
			Constructor<? extends InputMethodService> constructor = cls.getConstructor();
			ims = constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			System.err.println("ATLKeyboardViewer: failed to instantiate InputMethodService (kb_class: " + kb_class + ")");
			e.printStackTrace();
			System.exit(1);
		}

		boolean is_layershell = true;

		if (extras.containsKey("layershell") && extras.getString("layershell").equals("off"))
			is_layershell = false;

		ims.launch_keyboard(is_layershell);
	}
}
