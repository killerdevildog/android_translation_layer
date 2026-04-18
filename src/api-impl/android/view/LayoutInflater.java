/*
 * portions Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Slog;
import android.util.Xml;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class LayoutInflater {
	private static final String TAG = "LayoutInflater";

	/* pretty print for debugging */
	private int indent = 1;
	private String tabs(int indent) {
		return indent > 0 ? String.format("%" + indent + "s", "").replace(" ", "\t") : "";
	}

	public interface Factory {
	}

	public interface Factory2 {
		public View onCreateView(View parent, String name, Context context, AttributeSet attrs);
	}

	private Factory2 mFactory2;
	private Context context;
	private LinkedList<Factory2> factories = new LinkedList<>();

	public LayoutInflater(Context context) {
		this.context = context;
	}

	public LayoutInflater(LayoutInflater original, Context context) {
		this.context = context;
		this.factories.addAll(original.factories);
		this.mFactory = original.mFactory;
		this.mFactory2 = original.mFactory2;
	}

	private Factory mFactory;

	public final LayoutInflater.Factory getFactory() {
		return mFactory;
	}

	public void setFactory(LayoutInflater.Factory factory) {
		mFactory = factory;
	}

	public void setFactory2(Factory2 factory) {
		this.mFactory2 = factory;
		factories.addFirst(factory);
	}

	public Factory2 getFactory2() {
		return mFactory2;
	}

	public static LayoutInflater from(Context context) {
		return (LayoutInflater)context.getSystemService("layout_inflater");
	}

	public final View createView(String name, String prefix, AttributeSet attrs) throws Exception {
		Slog.v(TAG, tabs(indent) + "createView(" + name + ", " + prefix + ", " + attrs + ");");

		String view_class_name = prefix != null ? prefix + name : name;
		Class view_class = Class.forName(view_class_name);

		Constructor constructor = view_class.getConstructor(Context.class, AttributeSet.class);

		Context context = this.context;
		final TypedArray ta = context.obtainStyledAttributes(attrs, new int[] {com.android.internal.R.attr.theme});
		final int themeResId = ta.getResourceId(0, 0);
		if (themeResId != 0) {
			context = new ContextThemeWrapper(context, themeResId);
		}
		ta.recycle();
		constructor.setAccessible(true);
		View view_instance = (View)constructor.newInstance(context, attrs);

		return view_instance;
	}

	/**
	 * taken as-is
	 */
	protected View onCreateView(String name, AttributeSet attrs) throws Exception {
		try { // FIXME ugly
			return createView(name, "android.view.", attrs);
		} catch (java.lang.ClassNotFoundException e) {
			try {
				return createView(name, "android.widget.", attrs);
			} catch (java.lang.ClassNotFoundException e1) {
				return createView(name, "android.webkit.", attrs);
			}
		}
	}

	/**
	 * taken as-is
	 */
	protected View onCreateView(View parent, String name, AttributeSet attrs) throws Exception {
		return onCreateView(name, attrs);
	}

	View createViewFromTag(View parent, String name, AttributeSet attrs) throws Exception {
		if (name.equals("view")) {
			name = attrs.getAttributeValue(null, "class");
		}

		Slog.v(TAG, tabs(indent) + "createViewFromTag called: parent: " + parent + ", name: " + name);

		View view = null;

		for (Factory2 factory : factories) {
			view = factory.onCreateView(parent, name, context, attrs);
			if (view != null)
				break;
		}
		if (view == null && context instanceof Factory2) {
			view = ((Factory2)context).onCreateView(parent, name, context, attrs);
		}
		if (view == null && -1 == name.indexOf('.')) {
			view = onCreateView(parent, name, attrs);
		} else if (view == null) {
			view = createView(name, null, attrs);
		}

		Slog.v(TAG, tabs(indent) + "createViewFromTag: Created view is: " + view + " | id: " + String.format("%x", view.getId()) + ", id_str: " + view.getIdName());
		return view;
	}

	public View inflate(int resource, ViewGroup root) {
		return inflate(resource, root, root != null);
	}

	public View inflate(int layoutResID, ViewGroup root, boolean attachToRoot) {

		Slog.v(TAG, "inflating view from layout id: " + String.format("%x", layoutResID) + ", id_str: " + Context.this_application.getResources().getResourceName(layoutResID));
		XmlResourceParser xpp = context.getResources().getLayout(layoutResID);

		try {
			return inflate(xpp, root, attachToRoot);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public View inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot) throws Exception {

		final AttributeSet attrs = Xml.asAttributeSet(parser);

		View result = root;

		// Look for the root node.
		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG
		       && type != XmlPullParser.END_DOCUMENT) {
			// Empty
		}

		if (type != XmlPullParser.START_TAG) {
			throw new Exception(parser.getPositionDescription() + ": No start tag found!");
		}

		final String name = parser.getName();

		Slog.v(TAG, tabs(indent) + "**************************");
		Slog.v(TAG, tabs(indent) + "Creating root view: " + name);
		Slog.v(TAG, tabs(indent) + "**************************");

		if (name.equals("merge")) {
			if (root == null || !attachToRoot) {
				throw new Exception("<merge /> can be used only with a valid ViewGroup root and attachToRoot=true");
			}

			rInflate(parser, root, attrs, false);
		} else {
			// Temp is the root view that was found in the xml
			View temp;
			if (name.equals("blink")) {
				throw new Exception("<blink> not supported atm");
				/*temp = new BlinkLayout(mContext, attrs);*/
			} else {
				temp = createViewFromTag(root, name, attrs);
			}

			ViewGroup.LayoutParams params = null;

			if (root != null) {
				Slog.v(TAG, tabs(indent) + "Creating params from root: " + root);

				// Create layout params that match root, if supplied
				try {
					params = root.generateLayoutParams(attrs);
					if (!attachToRoot) {
						// Set the layout params for temp if we are not
						// attaching. (If we are, we use addView, below)
						temp.setLayoutParams(params);
					}
				} catch (RuntimeException e) {
					Slog.w(TAG, "Exception in generateLayoutParams:", e);
				}
			}

			Slog.v(TAG, tabs(indent) + "-----> start inflating children");

			// Inflate all children under temp
			rInflate(parser, temp, attrs, true);

			Slog.v(TAG, tabs(indent) + "-----> done inflating children");

			// We are supposed to attach all the views we found (int temp)
			// to root. Do that now.
			if (root != null && attachToRoot) {
				root.addView(temp, params);
			}

			// Decide whether to return the root that was passed in or the
			// top view found in xml.
			if (root == null || !attachToRoot) {
				result = temp;
			}
		}

		return result;
	}

	void rInflate(XmlPullParser parser, View parent, final AttributeSet attrs, boolean finishInflate) throws Exception {
		final int depth = parser.getDepth();
		int type;

		indent++; // prettyprint for debugging
		while (((type = parser.next()) != XmlPullParser.END_TAG
		        || parser.getDepth() > depth)
		       && type != XmlPullParser.END_DOCUMENT) {

			if (type != XmlPullParser.START_TAG) {
				continue;
			}

			final String name = parser.getName();

			if (name.equals("requestFocus")) {
				throw new Exception("<requestFocus /> not supported atm");
				// parseRequestFocus(parser, parent);
			} else if (name.equals("include")) {
				if (parser.getDepth() == 0) {
					throw new Exception("<include /> cannot be the root element");
				}
				parseInclude(parser, parent, attrs);
			} else if (name.equals("merge")) {
				throw new Exception("<merge /> must be the root element");
			} else if (name.equals("blink")) {
				throw new Exception("<blink> not supported atm");
				/*final View view = new BlinkLayout(mContext, attrs);
				final ViewGroup viewGroup = (ViewGroup) parent;
				final ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
				rInflate(parser, view, attrs, true);
				viewGroup.addView(view, params);*/
			} else {
				final View view = createViewFromTag(parent, name, attrs);
				final ViewGroup viewGroup = (ViewGroup)parent;
				ViewGroup.LayoutParams params = null;
				try {
					params = viewGroup.generateLayoutParams(attrs);
				} catch (RuntimeException e) {
					params = viewGroup.generateDefaultLayoutParams();
				}
				rInflate(parser, view, attrs, true);
				viewGroup.addView(view, params);
			}
		}
		indent--; // prettyprint for debugging

		if (finishInflate)
			parent.onFinishInflate();
	}

	private void parseInclude(XmlPullParser parser, View parent, AttributeSet attrs) throws Exception {
		int type;

		int layout = attrs.getAttributeResourceValue(null, "layout", 0);

		final XmlResourceParser childParser = context.getResources().getLayout(layout);
		final AttributeSet childAttrs = Xml.asAttributeSet(childParser);

		while ((type = childParser.next()) != XmlPullParser.START_TAG
		       && type != XmlPullParser.END_DOCUMENT) {
			// Empty.
		}
		if (type != XmlPullParser.START_TAG) {
			throw new Exception(childParser.getPositionDescription() + ": No start tag found!");
		}

		final String childName = childParser.getName();
		if ("merge".equals(childName)) {
			// The <merge> tag doesn't support android:theme, so
			// nothing special to do here.
			rInflate(childParser, parent, childAttrs, false);
		} else {
			final View view = createViewFromTag(parent, childName, childAttrs);
			final ViewGroup group = (ViewGroup)parent;
			ViewGroup.LayoutParams params = null;
			try {
				params = group.generateLayoutParams(attrs);
			} catch (RuntimeException e) {
				// Ignore, just fail over to child attrs.
			}
			if (params == null) {
				params = group.generateLayoutParams(childAttrs);
			}
			view.setLayoutParams(params);
			// Inflate all children.
			rInflate(childParser, view, childAttrs, true);

			TypedArray ta = context.obtainStyledAttributes(attrs, new int[] {com.android.internal.R.attr.id});
			int id = ta.getResourceId(0, 0);
			if (id != 0)
				view.setId(id);
			ta.recycle();

			group.addView(view);
		}
	}

	public LayoutInflater cloneInContext(Context context) {
		LayoutInflater inflater = new LayoutInflater(context);
		inflater.factories.addAll(factories);
		inflater.mFactory = mFactory;
		inflater.mFactory2 = mFactory2;
		return inflater;
	}

	public Context getContext() {
		return context;
	}
}
