package android.text;

import android.graphics.drawable.Drawable;
import org.xml.sax.XMLReader;

public class Html {

	public static interface ImageGetter {
		public Drawable getDrawable(String source);
	}

	public static Spanned fromHtml(String source) {
		return Html.fromHtml(source, 0);
	}

	public static Spanned fromHtml(String source, int flags) {
		return new SpannableString(source.replace("<br/>", "\n")
		                               .replace("<br>", "\n")
		                               .replace("&nbsp;", " "));
		// TODO when JTidy is in use: s/<br \/>//g
	}

	public static Spanned fromHtml(String source, ImageGetter imageGetter, TagHandler tagHandler) {
		return fromHtml(source, 0);
	}

	public static Spanned fromHtml(String source, int flags, ImageGetter imageGetter, TagHandler tagHandler) {
		return fromHtml(source, flags);
	}

	public static String escapeHtml(CharSequence source) {
		StringBuilder out = new StringBuilder(source.length());
		for (int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			if (c == '<' || c == '>' || c == '&' || c == '"' || c == '\'' || c > 0x7F) {
				out.append("&#");
				out.append((int)c);
				out.append(';');
			} else {
				out.append(c);
			}
		}
		return out.toString();
	}

	public static interface TagHandler {
		/**
		 * This method will be called whenn the HTML parser encounters
		 * a tag that it does not know how to interpret.
		 */
		public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader);
	}
}
