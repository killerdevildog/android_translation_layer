/*
 * Copyright (C) 2006 The Android Open Source Project
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

package android.graphics;

import java.util.HashMap;
import java.util.Locale;

public class Color {
	public static final int BLACK = 0xFF000000;
	public static final int DKGRAY = 0xFF444444;
	public static final int GRAY = 0xFF888888;
	public static final int LTGRAY = 0xFFCCCCCC;
	public static final int WHITE = 0xFFFFFFFF;
	public static final int RED = 0xFFFF0000;
	public static final int GREEN = 0xFF00FF00;
	public static final int BLUE = 0xFF0000FF;
	public static final int YELLOW = 0xFFFFFF00;
	public static final int CYAN = 0xFF00FFFF;
	public static final int MAGENTA = 0xFFFF00FF;
	public static final int TRANSPARENT = 0;

	public static int argb(int alpha, int red, int green, int blue) {
		return (alpha << 24) | (red << 16) | (green << 8) | (blue << 0);
	}

	public static int rgb(int red, int green, int blue) {
		return argb(0xff, red, green, blue);
	}

	/**
	 * Return the alpha component of a color int. This is the same as saying
	 * color >>> 24
	 */
	public static int alpha(int color) {
		return color >>> 24;
	}

	/**
	 * Return the red component of a color int. This is the same as saying
	 * (color >> 16) & 0xFF
	 */
	public static int red(int color) {
		return (color >> 16) & 0xFF;
	}

	/**
	 * Return the green component of a color int. This is the same as saying
	 * (color >> 8) & 0xFF
	 */
	public static int green(int color) {
		return (color >> 8) & 0xFF;
	}

	/**
	 * Return the blue component of a color int. This is the same as saying
	 * color & 0xFF
	 */
	public static int blue(int color) {
		return color & 0xFF;
	}

	/**
	 * Parse the color string, and return the corresponding color-int.
	 * If the string cannot be parsed, throws an IllegalArgumentException
	 * exception. Supported formats are:
	 * #RRGGBB
	 * #AARRGGBB
	 * 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta',
	 * 'yellow', 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey',
	 * 'aqua', 'fuschia', 'lime', 'maroon', 'navy', 'olive', 'purple',
	 * 'silver', 'teal'
	 */
	public static int parseColor(String colorString) {
		if (colorString.charAt(0) == '#') {
			// Use a long to avoid rollovers on #ffXXXXXX
			long color = Long.parseLong(colorString.substring(1), 16);
			if (colorString.length() == 7) {
				// Set the alpha value
				color |= 0x00000000ff000000;
			} else if (colorString.length() != 9) {
				throw new IllegalArgumentException("Unknown color");
			}
			return (int)color;
		} else {
			Integer color = sColorNameMap.get(colorString.toLowerCase(Locale.ROOT));
			if (color != null) {
				return color;
			}
		}
		throw new IllegalArgumentException("Unknown color");
	}

	private static final HashMap<String, Integer> sColorNameMap;
	static {
		sColorNameMap = new HashMap<String, Integer>();
		sColorNameMap.put("black", BLACK);
		sColorNameMap.put("darkgray", DKGRAY);
		sColorNameMap.put("gray", GRAY);
		sColorNameMap.put("lightgray", LTGRAY);
		sColorNameMap.put("white", WHITE);
		sColorNameMap.put("red", RED);
		sColorNameMap.put("green", GREEN);
		sColorNameMap.put("blue", BLUE);
		sColorNameMap.put("yellow", YELLOW);
		sColorNameMap.put("cyan", CYAN);
		sColorNameMap.put("magenta", MAGENTA);
		sColorNameMap.put("aqua", 0x00FFFF);
		sColorNameMap.put("fuchsia", 0xFF00FF);
		sColorNameMap.put("darkgrey", DKGRAY);
		sColorNameMap.put("grey", GRAY);
		sColorNameMap.put("lightgrey", LTGRAY);
		sColorNameMap.put("lime", 0x00FF00);
		sColorNameMap.put("maroon", 0x800000);
		sColorNameMap.put("navy", 0x000080);
		sColorNameMap.put("olive", 0x808000);
		sColorNameMap.put("purple", 0x800080);
		sColorNameMap.put("silver", 0xC0C0C0);
		sColorNameMap.put("teal", 0x008080);
	}

	public static void colorToHSV(int color, float[] hsv) {
		float red = ((color >> 16) & 0xFF) / 255.0f;
		float green = ((color >> 8) & 0xFF) / 255.0f;
		float blue = (color & 0xFF) / 255.0f;
		float min = Math.min(red, Math.min(green, blue));
		float max = Math.max(red, Math.max(green, blue));
		float delta = max - min;

		if (delta == 0) {
			hsv[0] = 6;
		} else if (max == red) {
			hsv[0] = (green - blue) / delta % 6;
		} else if (max == green) {
			hsv[0] = 2 + (blue - red) / delta + 2;
		} else {
			hsv[0] = 4 + (red - green) / delta + 4;
		}
		hsv[0] *= 60;

		if (max == 0) {
			hsv[1] = 0;
		} else {
			hsv[1] = delta / max;
		}
		hsv[2] = max;
	}

	public static int HSVToColor(float[] hsv) {
		float h = hsv[0];
		float s = hsv[1];
		float v_ = hsv[2];
		int hi = (int)Math.floor(h / 60) % 6;
		float f = (h / 60 - (float)Math.floor(h / 60)) * 6;
		int p = (int)(v_ * (1 - s) * 255.f + 0.5f);
		int q = (int)(v_ * (1 - f * s) * 255.f + 0.5f);
		int t = (int)(v_ * (1 - (1 - f) * s) * 255.f + 0.5f);
		int v = (int)(v_ * 255.f + 0.5f);
		switch (hi) {
			case 0:
				return Color.rgb(v, t, p);
			case 1:
				return Color.rgb(q, v, p);
			case 2:
				return Color.rgb(p, v, t);
			case 3:
				return Color.rgb(p, q, v);
			case 4:
				return Color.rgb(t, p, v);
			case 5:
				return Color.rgb(v, p, q);
		}
		return 0;
	}

	public static int HSVToColor(int alpha, float[] hsv) {
		return (alpha << 24) | (HSVToColor(hsv) & 0xffffff);
	}
}
