/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package android.graphics.drawable;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.res.ColorStateList;
import android.content.res.ComplexColor;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
// import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.util.Log;
import android.util.MathUtils;
// import android.util.PathParser;
import android.util.Xml;
import com.android.internal.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class PathParser {
	static final String LOGTAG = PathParser.class.getSimpleName();
	/**
	 * @param pathData The string representing a path, the same as "d" string in svg file.
	 * @return the generated Path object.
	 */
	public static Path createPathFromPathData(String pathData) {
		Path path = new Path();
		PathDataNode[] nodes = createNodesFromPathData(pathData);
		if (nodes != null) {
			try {
				PathDataNode.nodesToPath(nodes, path);
			} catch (RuntimeException e) {
				throw new RuntimeException("Error in parsing " + pathData, e);
			}
			return path;
		}
		return null;
	}
	/**
	 * @param pathData The string representing a path, the same as "d" string in svg file.
	 * @return an array of the PathDataNode.
	 */
	public static PathDataNode[] createNodesFromPathData(String pathData) {
		if (pathData == null) {
			return null;
		}
		int start = 0;
		int end = 1;
		ArrayList<PathDataNode> list = new ArrayList<PathDataNode>();
		while (end < pathData.length()) {
			end = nextStart(pathData, end);
			String s = pathData.substring(start, end).trim();
			if (s.length() > 0) {
				float[] val = getFloats(s);
				addNode(list, s.charAt(0), val);
			}
			start = end;
			end++;
		}
		if ((end - start) == 1 && start < pathData.length()) {
			addNode(list, pathData.charAt(start), new float[0]);
		}
		return list.toArray(new PathDataNode[list.size()]);
	}
	/**
	 * @param source The array of PathDataNode to be duplicated.
	 * @return a deep copy of the <code>source</code>.
	 */
	public static PathDataNode[] deepCopyNodes(PathDataNode[] source) {
		if (source == null) {
			return null;
		}
		PathDataNode[] copy = new PathParser.PathDataNode[source.length];
		for (int i = 0; i < source.length; i++) {
			copy[i] = new PathDataNode(source[i]);
		}
		return copy;
	}
	/**
	 * @param nodesFrom The source path represented in an array of PathDataNode
	 * @param nodesTo The target path represented in an array of PathDataNode
	 * @return whether the <code>nodesFrom</code> can morph into <code>nodesTo</code>
	 */
	public static boolean canMorph(PathDataNode[] nodesFrom, PathDataNode[] nodesTo) {
		if (nodesFrom == null || nodesTo == null) {
			return false;
		}
		if (nodesFrom.length != nodesTo.length) {
			return false;
		}
		for (int i = 0; i < nodesFrom.length; i++) {
			if (nodesFrom[i].mType != nodesTo[i].mType
			    || nodesFrom[i].mParams.length != nodesTo[i].mParams.length) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Update the target's data to match the source.
	 * Before calling this, make sure canMorph(target, source) is true.
	 *
	 * @param target The target path represented in an array of PathDataNode
	 * @param source The source path represented in an array of PathDataNode
	 */
	public static void updateNodes(PathDataNode[] target, PathDataNode[] source) {
		for (int i = 0; i < source.length; i++) {
			target[i].mType = source[i].mType;
			for (int j = 0; j < source[i].mParams.length; j++) {
				target[i].mParams[j] = source[i].mParams[j];
			}
		}
	}
	private static int nextStart(String s, int end) {
		char c;
		while (end < s.length()) {
			c = s.charAt(end);
			// Note that 'e' or 'E' are not valid path commands, but could be
			// used for floating point numbers' scientific notation.
			// Therefore, when searching for next command, we should ignore 'e'
			// and 'E'.
			if ((((c - 'A') * (c - 'Z') <= 0) || ((c - 'a') * (c - 'z') <= 0))
			    && c != 'e' && c != 'E') {
				return end;
			}
			end++;
		}
		return end;
	}
	private static void addNode(ArrayList<PathDataNode> list, char cmd, float[] val) {
		list.add(new PathDataNode(cmd, val));
	}
	private static class ExtractFloatResult {
		// We need to return the position of the next separator and whether the
		// next float starts with a '-' or a '.'.
		int mEndPosition;
		boolean mEndWithNegOrDot;
	}
	/**
	 * Parse the floats in the string.
	 * This is an optimized version of parseFloat(s.split(",|\\s"));
	 *
	 * @param s the string containing a command and list of floats
	 * @return array of floats
	 */
	private static float[] getFloats(String s) {
		if (s.charAt(0) == 'z' || s.charAt(0) == 'Z') {
			return new float[0];
		}
		try {
			float[] results = new float[s.length()];
			int count = 0;
			int startPosition = 1;
			int endPosition = 0;
			ExtractFloatResult result = new ExtractFloatResult();
			int totalLength = s.length();
			// The startPosition should always be the first character of the
			// current number, and endPosition is the character after the current
			// number.
			while (startPosition < totalLength) {
				extract(s, startPosition, result);
				endPosition = result.mEndPosition;
				if (startPosition < endPosition) {
					results[count++] = Float.parseFloat(
					    s.substring(startPosition, endPosition));
				}
				if (result.mEndWithNegOrDot) {
					// Keep the '-' or '.' sign with next number.
					startPosition = endPosition;
				} else {
					startPosition = endPosition + 1;
				}
			}
			return Arrays.copyOf(results, count);
		} catch (NumberFormatException e) {
			throw new RuntimeException("error in parsing \"" + s + "\"", e);
		}
	}
	/**
	 * Calculate the position of the next comma or space or negative sign
	 * @param s the string to search
	 * @param start the position to start searching
	 * @param result the result of the extraction, including the position of the
	 * the starting position of next number, whether it is ending with a '-'.
	 */
	private static void extract(String s, int start, ExtractFloatResult result) {
		// Now looking for ' ', ',', '.' or '-' from the start.
		int currentIndex = start;
		boolean foundSeparator = false;
		result.mEndWithNegOrDot = false;
		boolean secondDot = false;
		boolean isExponential = false;
		for (; currentIndex < s.length(); currentIndex++) {
			boolean isPrevExponential = isExponential;
			isExponential = false;
			char currentChar = s.charAt(currentIndex);
			switch (currentChar) {
				case ' ':
				case ',':
					foundSeparator = true;
					break;
				case '-':
					// The negative sign following a 'e' or 'E' is not a separator.
					if (currentIndex != start && !isPrevExponential) {
						foundSeparator = true;
						result.mEndWithNegOrDot = true;
					}
					break;
				case '.':
					if (!secondDot) {
						secondDot = true;
					} else {
						// This is the second dot, and it is considered as a separator.
						foundSeparator = true;
						result.mEndWithNegOrDot = true;
					}
					break;
				case 'e':
				case 'E':
					isExponential = true;
					break;
			}
			if (foundSeparator) {
				break;
			}
		}
		// When there is nothing found, then we put the end position to the end
		// of the string.
		result.mEndPosition = currentIndex;
	}
	/**
	 * Each PathDataNode represents one command in the "d" attribute of the svg
	 * file.
	 * An array of PathDataNode can represent the whole "d" attribute.
	 */
	public static class PathDataNode {
		private char mType;
		private float[] mParams;
		private PathDataNode(char type, float[] params) {
			mType = type;
			mParams = params;
		}
		private PathDataNode(PathDataNode n) {
			mType = n.mType;
			mParams = Arrays.copyOf(n.mParams, n.mParams.length);
		}
		/**
		 * Convert an array of PathDataNode to Path.
		 *
		 * @param node The source array of PathDataNode.
		 * @param path The target Path object.
		 */
		public static void nodesToPath(PathDataNode[] node, Path path) {
			float[] current = new float[6];
			char previousCommand = 'm';
			for (int i = 0; i < node.length; i++) {
				addCommand(path, current, previousCommand, node[i].mType, node[i].mParams);
				previousCommand = node[i].mType;
			}
		}
		/**
		 * The current PathDataNode will be interpolated between the
		 * <code>nodeFrom</code> and <code>nodeTo</code> according to the
		 * <code>fraction</code>.
		 *
		 * @param nodeFrom The start value as a PathDataNode.
		 * @param nodeTo The end value as a PathDataNode
		 * @param fraction The fraction to interpolate.
		 */
		public void interpolatePathDataNode(PathDataNode nodeFrom,
		                                    PathDataNode nodeTo, float fraction) {
			for (int i = 0; i < nodeFrom.mParams.length; i++) {
				mParams[i] = nodeFrom.mParams[i] * (1 - fraction)
				           + nodeTo.mParams[i] * fraction;
			}
		}
		private static void addCommand(Path path, float[] current,
		                               char previousCmd, char cmd, float[] val) {
			int incr = 2;
			float currentX = current[0];
			float currentY = current[1];
			float ctrlPointX = current[2];
			float ctrlPointY = current[3];
			float currentSegmentStartX = current[4];
			float currentSegmentStartY = current[5];
			float reflectiveCtrlPointX;
			float reflectiveCtrlPointY;
			switch (cmd) {
				case 'z':
				case 'Z':
					path.close();
					// Path is closed here, but we need to move the pen to the
					// closed position. So we cache the segment's starting position,
					// and restore it here.
					currentX = currentSegmentStartX;
					currentY = currentSegmentStartY;
					ctrlPointX = currentSegmentStartX;
					ctrlPointY = currentSegmentStartY;
					path.moveTo(currentX, currentY);
					break;
				case 'm':
				case 'M':
				case 'l':
				case 'L':
				case 't':
				case 'T':
					incr = 2;
					break;
				case 'h':
				case 'H':
				case 'v':
				case 'V':
					incr = 1;
					break;
				case 'c':
				case 'C':
					incr = 6;
					break;
				case 's':
				case 'S':
				case 'q':
				case 'Q':
					incr = 4;
					break;
				case 'a':
				case 'A':
					incr = 7;
					break;
			}
			for (int k = 0; k < val.length; k += incr) {
				switch (cmd) {
					case 'm': // moveto - Start a new sub-path (relative)
						path.rMoveTo(val[k + 0], val[k + 1]);
						currentX += val[k + 0];
						currentY += val[k + 1];
						currentSegmentStartX = currentX;
						currentSegmentStartY = currentY;
						break;
					case 'M': // moveto - Start a new sub-path
						path.moveTo(val[k + 0], val[k + 1]);
						currentX = val[k + 0];
						currentY = val[k + 1];
						currentSegmentStartX = currentX;
						currentSegmentStartY = currentY;
						break;
					case 'l': // lineto - Draw a line from the current point (relative)
						path.rLineTo(val[k + 0], val[k + 1]);
						currentX += val[k + 0];
						currentY += val[k + 1];
						break;
					case 'L': // lineto - Draw a line from the current point
						path.lineTo(val[k + 0], val[k + 1]);
						currentX = val[k + 0];
						currentY = val[k + 1];
						break;
					case 'h': // horizontal lineto - Draws a horizontal line (relative)
						path.rLineTo(val[k + 0], 0);
						currentX += val[k + 0];
						break;
					case 'H': // horizontal lineto - Draws a horizontal line
						path.lineTo(val[k + 0], currentY);
						currentX = val[k + 0];
						break;
					case 'v': // vertical lineto - Draws a vertical line from the current point (r)
						path.rLineTo(0, val[k + 0]);
						currentY += val[k + 0];
						break;
					case 'V': // vertical lineto - Draws a vertical line from the current point
						path.lineTo(currentX, val[k + 0]);
						currentY = val[k + 0];
						break;
					case 'c': // curveto - Draws a cubic Bézier curve (relative)
						path.rCubicTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3],
						              val[k + 4], val[k + 5]);
						ctrlPointX = currentX + val[k + 2];
						ctrlPointY = currentY + val[k + 3];
						currentX += val[k + 4];
						currentY += val[k + 5];
						break;
					case 'C': // curveto - Draws a cubic Bézier curve
						path.cubicTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3],
						             val[k + 4], val[k + 5]);
						currentX = val[k + 4];
						currentY = val[k + 5];
						ctrlPointX = val[k + 2];
						ctrlPointY = val[k + 3];
						break;
					case 's': // smooth curveto - Draws a cubic Bézier curve (reflective cp)
						reflectiveCtrlPointX = 0;
						reflectiveCtrlPointY = 0;
						if (previousCmd == 'c' || previousCmd == 's'
						    || previousCmd == 'C' || previousCmd == 'S') {
							reflectiveCtrlPointX = currentX - ctrlPointX;
							reflectiveCtrlPointY = currentY - ctrlPointY;
						}
						path.rCubicTo(reflectiveCtrlPointX, reflectiveCtrlPointY,
						              val[k + 0], val[k + 1],
						              val[k + 2], val[k + 3]);
						ctrlPointX = currentX + val[k + 0];
						ctrlPointY = currentY + val[k + 1];
						currentX += val[k + 2];
						currentY += val[k + 3];
						break;
					case 'S': // shorthand/smooth curveto Draws a cubic Bézier curve(reflective cp)
						reflectiveCtrlPointX = currentX;
						reflectiveCtrlPointY = currentY;
						if (previousCmd == 'c' || previousCmd == 's'
						    || previousCmd == 'C' || previousCmd == 'S') {
							reflectiveCtrlPointX = 2 * currentX - ctrlPointX;
							reflectiveCtrlPointY = 2 * currentY - ctrlPointY;
						}
						path.cubicTo(reflectiveCtrlPointX, reflectiveCtrlPointY,
						             val[k + 0], val[k + 1], val[k + 2], val[k + 3]);
						ctrlPointX = val[k + 0];
						ctrlPointY = val[k + 1];
						currentX = val[k + 2];
						currentY = val[k + 3];
						break;
					case 'q': // Draws a quadratic Bézier (relative)
						path.rQuadTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3]);
						ctrlPointX = currentX + val[k + 0];
						ctrlPointY = currentY + val[k + 1];
						currentX += val[k + 2];
						currentY += val[k + 3];
						break;
					case 'Q': // Draws a quadratic Bézier
						path.quadTo(val[k + 0], val[k + 1], val[k + 2], val[k + 3]);
						ctrlPointX = val[k + 0];
						ctrlPointY = val[k + 1];
						currentX = val[k + 2];
						currentY = val[k + 3];
						break;
					case 't': // Draws a quadratic Bézier curve(reflective control point)(relative)
						reflectiveCtrlPointX = 0;
						reflectiveCtrlPointY = 0;
						if (previousCmd == 'q' || previousCmd == 't'
						    || previousCmd == 'Q' || previousCmd == 'T') {
							reflectiveCtrlPointX = currentX - ctrlPointX;
							reflectiveCtrlPointY = currentY - ctrlPointY;
						}
						path.rQuadTo(reflectiveCtrlPointX, reflectiveCtrlPointY,
						             val[k + 0], val[k + 1]);
						ctrlPointX = currentX + reflectiveCtrlPointX;
						ctrlPointY = currentY + reflectiveCtrlPointY;
						currentX += val[k + 0];
						currentY += val[k + 1];
						break;
					case 'T': // Draws a quadratic Bézier curve (reflective control point)
						reflectiveCtrlPointX = currentX;
						reflectiveCtrlPointY = currentY;
						if (previousCmd == 'q' || previousCmd == 't'
						    || previousCmd == 'Q' || previousCmd == 'T') {
							reflectiveCtrlPointX = 2 * currentX - ctrlPointX;
							reflectiveCtrlPointY = 2 * currentY - ctrlPointY;
						}
						path.quadTo(reflectiveCtrlPointX, reflectiveCtrlPointY,
						            val[k + 0], val[k + 1]);
						ctrlPointX = reflectiveCtrlPointX;
						ctrlPointY = reflectiveCtrlPointY;
						currentX = val[k + 0];
						currentY = val[k + 1];
						break;
					case 'a': // Draws an elliptical arc
						// (rx ry x-axis-rotation large-arc-flag sweep-flag x y)
						drawArc(path,
						        currentX,
						        currentY,
						        val[k + 5] + currentX,
						        val[k + 6] + currentY,
						        val[k + 0],
						        val[k + 1],
						        val[k + 2],
						        val[k + 3] != 0,
						        val[k + 4] != 0);
						currentX += val[k + 5];
						currentY += val[k + 6];
						ctrlPointX = currentX;
						ctrlPointY = currentY;
						break;
					case 'A': // Draws an elliptical arc
						drawArc(path,
						        currentX,
						        currentY,
						        val[k + 5],
						        val[k + 6],
						        val[k + 0],
						        val[k + 1],
						        val[k + 2],
						        val[k + 3] != 0,
						        val[k + 4] != 0);
						currentX = val[k + 5];
						currentY = val[k + 6];
						ctrlPointX = currentX;
						ctrlPointY = currentY;
						break;
				}
				previousCmd = cmd;
			}
			current[0] = currentX;
			current[1] = currentY;
			current[2] = ctrlPointX;
			current[3] = ctrlPointY;
			current[4] = currentSegmentStartX;
			current[5] = currentSegmentStartY;
		}
		private static void drawArc(Path p,
		                            float x0,
		                            float y0,
		                            float x1,
		                            float y1,
		                            float a,
		                            float b,
		                            float theta,
		                            boolean isMoreThanHalf,
		                            boolean isPositiveArc) {
			/* Convert rotation angle from degrees to radians */
			double thetaD = Math.toRadians(theta);
			/* Pre-compute rotation matrix entries */
			double cosTheta = Math.cos(thetaD);
			double sinTheta = Math.sin(thetaD);
			/* Transform (x0, y0) and (x1, y1) into unit space */
			/* using (inverse) rotation, followed by (inverse) scale */
			double x0p = (x0 * cosTheta + y0 * sinTheta) / a;
			double y0p = (-x0 * sinTheta + y0 * cosTheta) / b;
			double x1p = (x1 * cosTheta + y1 * sinTheta) / a;
			double y1p = (-x1 * sinTheta + y1 * cosTheta) / b;
			/* Compute differences and averages */
			double dx = x0p - x1p;
			double dy = y0p - y1p;
			double xm = (x0p + x1p) / 2;
			double ym = (y0p + y1p) / 2;
			/* Solve for intersecting unit circles */
			double dsq = dx * dx + dy * dy;
			if (dsq == 0.0) {
				Log.w(LOGTAG, " Points are coincident");
				return; /* Points are coincident */
			}
			double disc = 1.0 / dsq - 1.0 / 4.0;
			if (disc < 0.0) {
				Log.w(LOGTAG, "Points are too far apart " + dsq);
				float adjust = (float)(Math.sqrt(dsq) / 1.99999);
				drawArc(p, x0, y0, x1, y1, a * adjust,
				        b * adjust, theta, isMoreThanHalf, isPositiveArc);
				return; /* Points are too far apart */
			}
			double s = Math.sqrt(disc);
			double sdx = s * dx;
			double sdy = s * dy;
			double cx;
			double cy;
			if (isMoreThanHalf == isPositiveArc) {
				cx = xm - sdy;
				cy = ym + sdx;
			} else {
				cx = xm + sdy;
				cy = ym - sdx;
			}
			double eta0 = Math.atan2((y0p - cy), (x0p - cx));
			double eta1 = Math.atan2((y1p - cy), (x1p - cx));
			double sweep = (eta1 - eta0);
			if (isPositiveArc != (sweep >= 0)) {
				if (sweep > 0) {
					sweep -= 2 * Math.PI;
				} else {
					sweep += 2 * Math.PI;
				}
			}
			cx *= a;
			cy *= b;
			double tcx = cx;
			cx = cx * cosTheta - cy * sinTheta;
			cy = tcx * sinTheta + cy * cosTheta;
			arcToBezier(p, cx, cy, a, b, x0, y0, thetaD, eta0, sweep);
		}
		/**
		 * Converts an arc to cubic Bezier segments and records them in p.
		 *
		 * @param p The target for the cubic Bezier segments
		 * @param cx The x coordinate center of the ellipse
		 * @param cy The y coordinate center of the ellipse
		 * @param a The radius of the ellipse in the horizontal direction
		 * @param b The radius of the ellipse in the vertical direction
		 * @param e1x E(eta1) x coordinate of the starting point of the arc
		 * @param e1y E(eta2) y coordinate of the starting point of the arc
		 * @param theta The angle that the ellipse bounding rectangle makes with horizontal plane
		 * @param start The start angle of the arc on the ellipse
		 * @param sweep The angle (positive or negative) of the sweep of the arc on the ellipse
		 */
		private static void arcToBezier(Path p,
		                                double cx,
		                                double cy,
		                                double a,
		                                double b,
		                                double e1x,
		                                double e1y,
		                                double theta,
		                                double start,
		                                double sweep) {
			// Taken from equations at: http://spaceroots.org/documents/ellipse/node8.html
			// and http://www.spaceroots.org/documents/ellipse/node22.html
			// Maximum of 45 degrees per cubic Bezier segment
			int numSegments = Math.abs((int)Math.ceil(sweep * 4 / Math.PI));
			double eta1 = start;
			double cosTheta = Math.cos(theta);
			double sinTheta = Math.sin(theta);
			double cosEta1 = Math.cos(eta1);
			double sinEta1 = Math.sin(eta1);
			double ep1x = (-a * cosTheta * sinEta1) - (b * sinTheta * cosEta1);
			double ep1y = (-a * sinTheta * sinEta1) + (b * cosTheta * cosEta1);
			double anglePerSegment = sweep / numSegments;
			for (int i = 0; i < numSegments; i++) {
				double eta2 = eta1 + anglePerSegment;
				double sinEta2 = Math.sin(eta2);
				double cosEta2 = Math.cos(eta2);
				double e2x = cx + (a * cosTheta * cosEta2) - (b * sinTheta * sinEta2);
				double e2y = cy + (a * sinTheta * cosEta2) + (b * cosTheta * sinEta2);
				double ep2x = -a * cosTheta * sinEta2 - b * sinTheta * cosEta2;
				double ep2y = -a * sinTheta * sinEta2 + b * cosTheta * cosEta2;
				double tanDiff2 = Math.tan((eta2 - eta1) / 2);
				double alpha =
				    Math.sin(eta2 - eta1) * (Math.sqrt(4 + (3 * tanDiff2 * tanDiff2)) - 1) / 3;
				double q1x = e1x + alpha * ep1x;
				double q1y = e1y + alpha * ep1y;
				double q2x = e2x - alpha * ep2x;
				double q2y = e2y - alpha * ep2y;
				p.cubicTo((float)q1x,
				          (float)q1y,
				          (float)q2x,
				          (float)q2y,
				          (float)e2x,
				          (float)e2y);
				eta1 = eta2;
				e1x = e2x;
				e1y = e2y;
				ep1x = ep2x;
				ep1y = ep2y;
			}
		}
	}
}

/**
 * This lets you create a drawable based on an XML vector graphic. It can be
 * defined in an XML file with the <code>&lt;vector></code> element.
 * <p/>
 * The vector drawable has the following elements:
 * <p/>
 * <dt><code>&lt;vector></code></dt>
 * <dl>
 * <dd>Used to define a vector drawable
 * <dl>
 * <dt><code>android:name</code></dt>
 * <dd>Defines the name of this vector drawable.</dd>
 * <dt><code>android:width</code></dt>
 * <dd>Used to define the intrinsic width of the drawable.
 * This support all the dimension units, normally specified with dp.</dd>
 * <dt><code>android:height</code></dt>
 * <dd>Used to define the intrinsic height the drawable.
 * This support all the dimension units, normally specified with dp.</dd>
 * <dt><code>android:viewportWidth</code></dt>
 * <dd>Used to define the width of the viewport space. Viewport is basically
 * the virtual canvas where the paths are drawn on.</dd>
 * <dt><code>android:viewportHeight</code></dt>
 * <dd>Used to define the height of the viewport space. Viewport is basically
 * the virtual canvas where the paths are drawn on.</dd>
 * <dt><code>android:tint</code></dt>
 * <dd>The color to apply to the drawable as a tint. By default, no tint is applied.</dd>
 * <dt><code>android:tintMode</code></dt>
 * <dd>The Porter-Duff blending mode for the tint color. The default value is src_in.</dd>
 * <dt><code>android:autoMirrored</code></dt>
 * <dd>Indicates if the drawable needs to be mirrored when its layout direction is
 * RTL (right-to-left).</dd>
 * <dt><code>android:alpha</code></dt>
 * <dd>The opacity of this drawable.</dd>
 * </dl></dd>
 * </dl>
 *
 * <dl>
 * <dt><code>&lt;group></code></dt>
 * <dd>Defines a group of paths or subgroups, plus transformation information.
 * The transformations are defined in the same coordinates as the viewport.
 * And the transformations are applied in the order of scale, rotate then translate.
 * <dl>
 * <dt><code>android:name</code></dt>
 * <dd>Defines the name of the group.</dd>
 * <dt><code>android:rotation</code></dt>
 * <dd>The degrees of rotation of the group.</dd>
 * <dt><code>android:pivotX</code></dt>
 * <dd>The X coordinate of the pivot for the scale and rotation of the group.
 * This is defined in the viewport space.</dd>
 * <dt><code>android:pivotY</code></dt>
 * <dd>The Y coordinate of the pivot for the scale and rotation of the group.
 * This is defined in the viewport space.</dd>
 * <dt><code>android:scaleX</code></dt>
 * <dd>The amount of scale on the X Coordinate.</dd>
 * <dt><code>android:scaleY</code></dt>
 * <dd>The amount of scale on the Y coordinate.</dd>
 * <dt><code>android:translateX</code></dt>
 * <dd>The amount of translation on the X coordinate.
 * This is defined in the viewport space.</dd>
 * <dt><code>android:translateY</code></dt>
 * <dd>The amount of translation on the Y coordinate.
 * This is defined in the viewport space.</dd>
 * </dl></dd>
 * </dl>
 *
 * <dl>
 * <dt><code>&lt;path></code></dt>
 * <dd>Defines paths to be drawn.
 * <dl>
 * <dt><code>android:name</code></dt>
 * <dd>Defines the name of the path.</dd>
 * <dt><code>android:pathData</code></dt>
 * <dd>Defines path data using exactly same format as "d" attribute
 * in the SVG's path data. This is defined in the viewport space.</dd>
 * <dt><code>android:fillColor</code></dt>
 * <dd>Defines the color to fill the path (none if not present).</dd>
 * <dt><code>android:strokeColor</code></dt>
 * <dd>Defines the color to draw the path outline (none if not present).</dd>
 * <dt><code>android:strokeWidth</code></dt>
 * <dd>The width a path stroke.</dd>
 * <dt><code>android:strokeAlpha</code></dt>
 * <dd>The opacity of a path stroke.</dd>
 * <dt><code>android:fillAlpha</code></dt>
 * <dd>The opacity to fill the path with.</dd>
 * <dt><code>android:trimPathStart</code></dt>
 * <dd>The fraction of the path to trim from the start, in the range from 0 to 1.</dd>
 * <dt><code>android:trimPathEnd</code></dt>
 * <dd>The fraction of the path to trim from the end, in the range from 0 to 1.</dd>
 * <dt><code>android:trimPathOffset</code></dt>
 * <dd>Shift trim region (allows showed region to include the start and end), in the range
 * from 0 to 1.</dd>
 * <dt><code>android:strokeLineCap</code></dt>
 * <dd>Sets the linecap for a stroked path: butt, round, square.</dd>
 * <dt><code>android:strokeLineJoin</code></dt>
 * <dd>Sets the lineJoin for a stroked path: miter,round,bevel.</dd>
 * <dt><code>android:strokeMiterLimit</code></dt>
 * <dd>Sets the Miter limit for a stroked path.</dd>
 * </dl></dd>
 * </dl>
 *
 * <dl>
 * <dt><code>&lt;clip-path></code></dt>
 * <dd>Defines path to be the current clip. Note that the clip path only apply to
 * the current group and its children.
 * <dl>
 * <dt><code>android:name</code></dt>
 * <dd>Defines the name of the clip path.</dd>
 * <dt><code>android:pathData</code></dt>
 * <dd>Defines clip path using the same format as "d" attribute
 * in the SVG's path data.</dd>
 * </dl></dd>
 * </dl>
 * <li>Here is a simple VectorDrawable in this vectordrawable.xml file.
 * <pre>
 * &lt;vector xmlns:android=&quot;http://schemas.android.com/apk/res/android&quot;
 *     android:height=&quot;64dp&quot;
 *     android:width=&quot;64dp&quot;
 *     android:viewportHeight=&quot;600&quot;
 *     android:viewportWidth=&quot;600&quot; &gt;
 *     &lt;group
 *         android:name=&quot;rotationGroup&quot;
 *         android:pivotX=&quot;300.0&quot;
 *         android:pivotY=&quot;300.0&quot;
 *         android:rotation=&quot;45.0&quot; &gt;
 *         &lt;path
 *             android:name=&quot;v&quot;
 *             android:fillColor=&quot;#000000&quot;
 *             android:pathData=&quot;M300,70 l 0,-70 70,70 0,0 -70,70z&quot; /&gt;
 *     &lt;/group&gt;
 * &lt;/vector&gt;
 * </pre></li>
 */

public class VectorDrawable extends Drawable {
	private static final String LOGTAG = VectorDrawable.class.getSimpleName();

	/* don't use an intermediary bitmap (for making an SVG) */
	static boolean direct_draw_override = false;

	private static final String SHAPE_CLIP_PATH = "clip-path";
	private static final String SHAPE_GROUP = "group";
	private static final String SHAPE_PATH = "path";
	private static final String SHAPE_VECTOR = "vector";

	private static final int LINECAP_BUTT = 0;
	private static final int LINECAP_ROUND = 1;
	private static final int LINECAP_SQUARE = 2;

	private static final int LINEJOIN_MITER = 0;
	private static final int LINEJOIN_ROUND = 1;
	private static final int LINEJOIN_BEVEL = 2;

	// Cap the bitmap size, such that it won't hurt the performance too much
	// and it won't crash due to a very large scale.
	// The drawable will look blurry above this size.
	private static final int MAX_CACHED_BITMAP_SIZE = 2048;

	private static final boolean DBG_VECTOR_DRAWABLE = false;

	private VectorDrawableState mVectorState;

	private PorterDuffColorFilter mTintFilter;
	private ColorFilter mColorFilter;

	private boolean mMutated;

	// AnimatedVectorDrawable needs to turn off the cache all the time, otherwise,
	// caching the bitmap by default is allowed.
	private boolean mAllowCaching = true;

	// Given the virtual display setup, the dpi can be different than the inflation's dpi.
	// Therefore, we need to scale the values we got from the getDimension*().
	private int mDpiScaledWidth = 0;
	private int mDpiScaledHeight = 0;
	// private Insets mDpiScaleInsets = Insets.NONE;

	// Temp variable, only for saving "new" operation at the draw() time.
	private final float[] mTmpFloats = new float[9];
	private final Matrix mTmpMatrix = new Matrix();
	private final Rect mTmpBounds = new Rect();

	public VectorDrawable() {
		this(null, null);
	}

	private VectorDrawable(@NonNull VectorDrawableState state, @Nullable Resources res) {
		if (state == null) {
			mVectorState = new VectorDrawableState();
		} else {
			mVectorState = state;
			mTintFilter = updateTintFilter(mTintFilter, state.mTint, state.mTintMode);
		}
		updateDimensionInfo(res, false);
	}

	@Override
	public Drawable mutate() {
		if (!mMutated && super.mutate() == this) {
			mVectorState = new VectorDrawableState(mVectorState);
			mMutated = true;
		}
		return this;
	}

	/**
	 * @hide
	 */
	public void clearMutated() {
		// super.clearMutated();
		mMutated = false;
	}

	Object getTargetByName(String name) {
		return mVectorState.mVPathRenderer.mVGTargetsMap.get(name);
	}

	@Override
	public ConstantState getConstantState() {
		mVectorState.mChangingConfigurations = getChangingConfigurations();
		return mVectorState;
	}

	@Override
	public void draw(Canvas canvas) {
		// We will offset the bounds for drawBitmap, so copyBounds() here instead
		// of getBounds().
		copyBounds(mTmpBounds);
		if (mTmpBounds.width() <= 0 || mTmpBounds.height() <= 0) {
			// Nothing to draw
			return;
		}

		// Color filters always override tint filters.
		final ColorFilter colorFilter = (mColorFilter == null ? mTintFilter : mColorFilter);

		// The imageView can scale the canvas in different ways, in order to
		// avoid blurry scaling, we have to draw into a bitmap with exact pixel
		// size first. This bitmap size is determined by the bounds and the
		// canvas scale.
		canvas.getMatrix(mTmpMatrix);
		mTmpMatrix.getValues(mTmpFloats);
		float canvasScaleX = Math.abs(mTmpFloats[Matrix.MSCALE_X]);
		float canvasScaleY = Math.abs(mTmpFloats[Matrix.MSCALE_Y]);
		int scaledWidth = (int)(mTmpBounds.width() * canvasScaleX);
		int scaledHeight = (int)(mTmpBounds.height() * canvasScaleY);
		scaledWidth = Math.min(MAX_CACHED_BITMAP_SIZE, scaledWidth);
		scaledHeight = Math.min(MAX_CACHED_BITMAP_SIZE, scaledHeight);

		if (scaledWidth <= 0 || scaledHeight <= 0) {
			return;
		}

		final int saveCount = canvas.save();
		canvas.translate(mTmpBounds.left, mTmpBounds.top);

		// Handle RTL mirroring.
		final boolean needMirroring = needMirroring();
		if (needMirroring) {
			canvas.translate(mTmpBounds.width(), 0);
			canvas.scale(-1.0f, 1.0f);
		}

		// At this point, canvas has been translated to the right position.
		// And we use this bound for the destination rect for the drawBitmap, so
		// we offset to (0, 0);
		mTmpBounds.offsetTo(0, 0);

		if (direct_draw_override) {
			mVectorState.mVPathRenderer.draw(canvas, scaledWidth, scaledHeight, null);
		} else {
			mVectorState.createCachedBitmapIfNeeded(scaledWidth, scaledHeight);
			if (!mAllowCaching) {
				mVectorState.updateCachedBitmap(scaledWidth, scaledHeight);
			} else {
				if (!mVectorState.canReuseCache()) {
					mVectorState.updateCachedBitmap(scaledWidth, scaledHeight);
					mVectorState.updateCacheStates();
				}
			}
			mVectorState.drawCachedBitmapWithRootAlpha(canvas, colorFilter, mTmpBounds);
		}
		canvas.restoreToCount(saveCount);
	}

	public int getAlpha() {
		return mVectorState.mVPathRenderer.getRootAlpha();
	}

	@Override
	public void setAlpha(int alpha) {
		if (mVectorState.mVPathRenderer.getRootAlpha() != alpha) {
			mVectorState.mVPathRenderer.setRootAlpha(alpha);
			invalidateSelf();
		}
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		mColorFilter = colorFilter;
		invalidateSelf();
	}

	public ColorFilter getColorFilter() {
		return mColorFilter;
	}

	@Override
	public void setTintList(ColorStateList tint) {
		final VectorDrawableState state = mVectorState;
		if (state.mTint != tint) {
			state.mTint = tint;
			mTintFilter = updateTintFilter(mTintFilter, tint, state.mTintMode);
			invalidateSelf();
		}
	}

	@Override
	public void setTintMode(Mode tintMode) {
		final VectorDrawableState state = mVectorState;
		if (state.mTintMode != tintMode) {
			state.mTintMode = tintMode;
			mTintFilter = updateTintFilter(mTintFilter, state.mTint, tintMode);
			invalidateSelf();
		}
	}

	@Override
	public boolean isStateful() {
		return super.isStateful() || (mVectorState != null && mVectorState.mTint != null && mVectorState.mTint.isStateful());
	}

	@Override
	protected boolean onStateChange(int[] stateSet) {
		final VectorDrawableState state = mVectorState;
		if (state.mTint != null && state.mTintMode != null) {
			mTintFilter = updateTintFilter(mTintFilter, state.mTint, state.mTintMode);
			invalidateSelf();
			return true;
		}
		return false;
	}

	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public int getIntrinsicWidth() {
		return mDpiScaledWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return mDpiScaledHeight;
	}

	/*
	 * Update the VectorDrawable dimension since the res can be in different Dpi now.
	 * Basically, when a new instance is created or getDimension() is called, we should update
	 * the current VectorDrawable's dimension information.
	 * Only after updateStateFromTypedArray() is called, we should called this and update the
	 * constant state's dpi info, i.e. updateConstantStateDensity == true.
	 */
	void updateDimensionInfo(@Nullable Resources res, boolean updateConstantStateDensity) {
		// if (res != null) {
		// 	final int densityDpi = res.getDisplayMetrics().densityDpi;
		// 	final int targetDensity = densityDpi == 0 ? DisplayMetrics.DENSITY_DEFAULT : densityDpi;

		// 	if (updateConstantStateDensity) {
		// 		mVectorState.mVPathRenderer.mTargetDensity = targetDensity;
		// 	} else {
		// 		final int constantStateDensity = mVectorState.mVPathRenderer.mTargetDensity;
		// 		if (targetDensity != constantStateDensity && constantStateDensity != 0) {
		// 			mDpiScaledWidth = Bitmap.scaleFromDensity(
		// 					(int) mVectorState.mVPathRenderer.mBaseWidth, constantStateDensity,
		// 					targetDensity);
		// 			mDpiScaledHeight = Bitmap.scaleFromDensity(
		// 					(int) mVectorState.mVPathRenderer.mBaseHeight,constantStateDensity,
		// 					targetDensity);
		// 			final int left = Bitmap.scaleFromDensity(
		// 					mVectorState.mVPathRenderer.mOpticalInsets.left, constantStateDensity,
		// 					targetDensity);
		// 			final int right = Bitmap.scaleFromDensity(
		// 					mVectorState.mVPathRenderer.mOpticalInsets.right, constantStateDensity,
		// 					targetDensity);
		// 			final int top = Bitmap.scaleFromDensity(
		// 					mVectorState.mVPathRenderer.mOpticalInsets.top, constantStateDensity,
		// 					targetDensity);
		// 			final int bottom = Bitmap.scaleFromDensity(
		// 					mVectorState.mVPathRenderer.mOpticalInsets.bottom, constantStateDensity,
		// 					targetDensity);
		// 			mDpiScaleInsets = Insets.of(left, top, right, bottom);
		// 			return;
		// 		}
		// 	}
		// }
		// For all the other cases, like either res is null, constant state is not initialized or
		// target density is the same as the constant state, we will just use the constant state
		// dimensions.
		mDpiScaledWidth = (int)mVectorState.mVPathRenderer.mBaseWidth;
		mDpiScaledHeight = (int)mVectorState.mVPathRenderer.mBaseHeight;
		// mDpiScaleInsets = mVectorState.mVPathRenderer.mOpticalInsets;
	}

	// @Override
	// public boolean canApplyTheme() {
	// 	return (mVectorState != null && mVectorState.canApplyTheme()) || super.canApplyTheme();
	// }

	// @Override
	// public void applyTheme(Theme t) {
	// 	super.applyTheme(t);

	// 	final VectorDrawableState state = mVectorState;
	// 	if (state == null) {
	// 		return;
	// 	}

	// 	if (state.mThemeAttrs != null) {
	// 		final TypedArray a = t.resolveAttributes(
	// 				state.mThemeAttrs, R.styleable.VectorDrawable);
	// 		try {
	// 			state.mCacheDirty = true;
	// 			updateStateFromTypedArray(a);
	// 			updateDimensionInfo(t.getResources(), true /* update constant state */);
	// 		} catch (XmlPullParserException e) {
	// 			throw new RuntimeException(e);
	// 		} finally {
	// 			a.recycle();
	// 		}
	// 	}

	// 	// Apply theme to contained color state list.
	// 	if (state.mTint != null && state.mTint.canApplyTheme()) {
	// 		state.mTint = state.mTint.obtainForTheme(t);
	// 	}

	// 	final VPathRenderer path = state.mVPathRenderer;
	// 	if (path != null && path.canApplyTheme()) {
	// 		path.applyTheme(t);
	// 	}

	// 	// Update local state.
	// 	mTintFilter = updateTintFilter(mTintFilter, state.mTint, state.mTintMode);
	// }

	/**
	 * The size of a pixel when scaled from the intrinsic dimension to the viewport dimension.
	 * This is used to calculate the path animation accuracy.
	 *
	 * @hide
	 */
	public float getPixelSize() {
		if (mVectorState == null || mVectorState.mVPathRenderer == null
		    || mVectorState.mVPathRenderer.mBaseWidth == 0
		    || mVectorState.mVPathRenderer.mBaseHeight == 0
		    || mVectorState.mVPathRenderer.mViewportHeight == 0
		    || mVectorState.mVPathRenderer.mViewportWidth == 0) {
			return 1; // fall back to 1:1 pixel mapping.
		}
		float intrinsicWidth = mVectorState.mVPathRenderer.mBaseWidth;
		float intrinsicHeight = mVectorState.mVPathRenderer.mBaseHeight;
		float viewportWidth = mVectorState.mVPathRenderer.mViewportWidth;
		float viewportHeight = mVectorState.mVPathRenderer.mViewportHeight;
		float scaleX = viewportWidth / intrinsicWidth;
		float scaleY = viewportHeight / intrinsicHeight;
		return Math.min(scaleX, scaleY);
	}

	/** @hide */
	public static VectorDrawable create(Resources resources, int rid) {
		try {
			final XmlPullParser parser = resources.getXml(rid);
			final AttributeSet attrs = Xml.asAttributeSet(parser);
			int type;
			while ((type = parser.next()) != XmlPullParser.START_TAG
			       && type != XmlPullParser.END_DOCUMENT) {
				// Empty loop
			}
			if (type != XmlPullParser.START_TAG) {
				throw new XmlPullParserException("No start tag found");
			}

			final VectorDrawable drawable = new VectorDrawable();
			drawable.inflate(resources, parser, attrs, null);

			return drawable;
		} catch (XmlPullParserException e) {
			Log.e(LOGTAG, "parser error", e);
		} catch (IOException e) {
			Log.e(LOGTAG, "parser error", e);
		}
		return null;
	}

	private static int applyAlpha(int color, float alpha) {
		int alphaBytes = Color.alpha(color);
		color &= 0x00FFFFFF;
		color |= ((int)(alphaBytes * alpha)) << 24;
		return color;
	}

	public void inflate(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme)
	    throws XmlPullParserException, IOException {
		final VectorDrawableState state = mVectorState;
		final VPathRenderer pathRenderer = new VPathRenderer();
		state.mVPathRenderer = pathRenderer;

		final TypedArray a = Resources.obtainAttributes(res, theme, attrs, R.styleable.VectorDrawable);
		updateStateFromTypedArray(a);
		a.recycle();

		state.mCacheDirty = true;
		inflateInternal(res, parser, attrs, theme);

		mTintFilter = updateTintFilter(mTintFilter, state.mTint, state.mTintMode);
		updateDimensionInfo(res, true /* update constant state */);
	}

	private void updateStateFromTypedArray(TypedArray a) throws XmlPullParserException {
		final VectorDrawableState state = mVectorState;
		final VPathRenderer pathRenderer = state.mVPathRenderer;

		// Account for any configuration changes.
		state.mChangingConfigurations |= a.getChangingConfigurations();

		// Extract the theme attributes, if any.
		state.mThemeAttrs = a.extractThemeAttrs();

		final int tintMode = a.getInt(R.styleable.VectorDrawable_tintMode, -1);
		if (tintMode != -1) {
			state.mTintMode = Mode.values()[tintMode];
		}

		final ColorStateList tint = a.getColorStateList(R.styleable.VectorDrawable_tint);
		if (tint != null) {
			state.mTint = tint;
		}

		state.mAutoMirrored = a.getBoolean(
		    R.styleable.VectorDrawable_autoMirrored, state.mAutoMirrored);

		pathRenderer.mViewportWidth = a.getFloat(
		    R.styleable.VectorDrawable_viewportWidth, pathRenderer.mViewportWidth);
		pathRenderer.mViewportHeight = a.getFloat(
		    R.styleable.VectorDrawable_viewportHeight, pathRenderer.mViewportHeight);

		if (pathRenderer.mViewportWidth <= 0) {
			throw new XmlPullParserException(a.getPositionDescription() + "<vector> tag requires viewportWidth > 0");
		} else if (pathRenderer.mViewportHeight <= 0) {
			throw new XmlPullParserException(a.getPositionDescription() + "<vector> tag requires viewportHeight > 0");
		}

		pathRenderer.mBaseWidth = a.getDimension(
		    R.styleable.VectorDrawable_width, pathRenderer.mBaseWidth);
		pathRenderer.mBaseHeight = a.getDimension(
		    R.styleable.VectorDrawable_height, pathRenderer.mBaseHeight);

		if (pathRenderer.mBaseWidth <= 0) {
			throw new XmlPullParserException(a.getPositionDescription() + "<vector> tag requires width > 0");
		} else if (pathRenderer.mBaseHeight <= 0) {
			throw new XmlPullParserException(a.getPositionDescription() + "<vector> tag requires height > 0");
		}

		// final int insetLeft = a.getDimensionPixelSize(
		// 		R.styleable.VectorDrawable_opticalInsetLeft, pathRenderer.mOpticalInsets.left);
		// final int insetTop = a.getDimensionPixelSize(
		// 		R.styleable.VectorDrawable_opticalInsetTop, pathRenderer.mOpticalInsets.top);
		// final int insetRight = a.getDimensionPixelSize(
		// 		R.styleable.VectorDrawable_opticalInsetRight, pathRenderer.mOpticalInsets.right);
		// final int insetBottom = a.getDimensionPixelSize(
		// 		R.styleable.VectorDrawable_opticalInsetBottom, pathRenderer.mOpticalInsets.bottom);
		// pathRenderer.mOpticalInsets = Insets.of(insetLeft, insetTop, insetRight, insetBottom);

		final float alphaInFloat = a.getFloat(R.styleable.VectorDrawable_alpha,
		                                      pathRenderer.getAlpha());
		pathRenderer.setAlpha(alphaInFloat);

		final String name = a.getString(R.styleable.VectorDrawable_name);
		if (name != null) {
			pathRenderer.mRootName = name;
			pathRenderer.mVGTargetsMap.put(name, pathRenderer);
		}
	}

	private void inflateInternal(Resources res, XmlPullParser parser, AttributeSet attrs,
	                             Theme theme) throws XmlPullParserException, IOException {
		final VectorDrawableState state = mVectorState;
		final VPathRenderer pathRenderer = state.mVPathRenderer;
		boolean noPathTag = true;

		// Use a stack to help to build the group tree.
		// The top of the stack is always the current group.
		final Stack<VGroup> groupStack = new Stack<VGroup>();
		groupStack.push(pathRenderer.mRootGroup);

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				final String tagName = parser.getName();
				final VGroup currentGroup = groupStack.peek();

				if (SHAPE_PATH.equals(tagName)) {
					final VFullPath path = new VFullPath();
					path.inflate(res, attrs, theme);
					currentGroup.mChildren.add(path);
					if (path.getPathName() != null) {
						pathRenderer.mVGTargetsMap.put(path.getPathName(), path);
					}
					noPathTag = false;
					state.mChangingConfigurations |= path.mChangingConfigurations;
				} else if (SHAPE_CLIP_PATH.equals(tagName)) {
					final VClipPath path = new VClipPath();
					path.inflate(res, attrs, theme);
					currentGroup.mChildren.add(path);
					if (path.getPathName() != null) {
						pathRenderer.mVGTargetsMap.put(path.getPathName(), path);
					}
					state.mChangingConfigurations |= path.mChangingConfigurations;
				} else if (SHAPE_GROUP.equals(tagName)) {
					VGroup newChildGroup = new VGroup();
					newChildGroup.inflate(res, attrs, theme);
					currentGroup.mChildren.add(newChildGroup);
					groupStack.push(newChildGroup);
					if (newChildGroup.getGroupName() != null) {
						pathRenderer.mVGTargetsMap.put(newChildGroup.getGroupName(),
						                               newChildGroup);
					}
					state.mChangingConfigurations |= newChildGroup.mChangingConfigurations;
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				final String tagName = parser.getName();
				if (SHAPE_GROUP.equals(tagName)) {
					groupStack.pop();
				}
			}
			eventType = parser.next();
		}

		// Print the tree out for debug.
		if (DBG_VECTOR_DRAWABLE) {
			printGroupTree(pathRenderer.mRootGroup, 0);
		}

		if (noPathTag) {
			final StringBuffer tag = new StringBuffer();

			if (tag.length() > 0) {
				tag.append(" or ");
			}
			tag.append(SHAPE_PATH);

			throw new XmlPullParserException("no " + tag + " defined");
		}
	}

	private void printGroupTree(VGroup currentGroup, int level) {
		String indent = "";
		for (int i = 0; i < level; i++) {
			indent += "    ";
		}
		// Print the current node
		Log.v(LOGTAG, indent + "current group is :" + currentGroup.getGroupName()
		              + " rotation is " + currentGroup.mRotate);
		Log.v(LOGTAG, indent + "matrix is :" + currentGroup.getLocalMatrix().toString());
		// Then print all the children groups
		for (int i = 0; i < currentGroup.mChildren.size(); i++) {
			Object child = currentGroup.mChildren.get(i);
			if (child instanceof VGroup) {
				printGroupTree((VGroup)child, level + 1);
			}
		}
	}

	@Override
	public int getChangingConfigurations() {
		return super.getChangingConfigurations() | mVectorState.getChangingConfigurations();
	}

	void setAllowCaching(boolean allowCaching) {
		mAllowCaching = allowCaching;
	}

	private boolean needMirroring() {
		// return isAutoMirrored() && getLayoutDirection() == LayoutDirection.RTL;
		return false;
	}

	@Override
	public void setAutoMirrored(boolean mirrored) {
		if (mVectorState.mAutoMirrored != mirrored) {
			mVectorState.mAutoMirrored = mirrored;
			invalidateSelf();
		}
	}

	public boolean isAutoMirrored() {
		return mVectorState.mAutoMirrored;
	}

	private static class VectorDrawableState extends ConstantState {
		int[] mThemeAttrs;
		int mChangingConfigurations;
		VPathRenderer mVPathRenderer;
		ColorStateList mTint = null;
		Mode mTintMode = DEFAULT_TINT_MODE;
		boolean mAutoMirrored;

		Bitmap mCachedBitmap;
		int[] mCachedThemeAttrs;
		ColorStateList mCachedTint;
		Mode mCachedTintMode;
		int mCachedRootAlpha;
		boolean mCachedAutoMirrored;
		boolean mCacheDirty;
		/** Temporary paint object used to draw cached bitmaps. */
		Paint mTempPaint;

		// Deep copy for mutate() or implicitly mutate.
		public VectorDrawableState(VectorDrawableState copy) {
			if (copy != null) {
				mThemeAttrs = copy.mThemeAttrs;
				mChangingConfigurations = copy.mChangingConfigurations;
				mVPathRenderer = new VPathRenderer(copy.mVPathRenderer);
				if (copy.mVPathRenderer.mFillPaint != null) {
					mVPathRenderer.mFillPaint = new Paint(copy.mVPathRenderer.mFillPaint);
				}
				if (copy.mVPathRenderer.mStrokePaint != null) {
					mVPathRenderer.mStrokePaint = new Paint(copy.mVPathRenderer.mStrokePaint);
				}
				mTint = copy.mTint;
				mTintMode = copy.mTintMode;
				mAutoMirrored = copy.mAutoMirrored;
			}
		}

		public void drawCachedBitmapWithRootAlpha(Canvas canvas, ColorFilter filter,
		                                          Rect originalBounds) {
			// The bitmap's size is the same as the bounds.
			final Paint p = getPaint(filter);
			canvas.drawBitmap(mCachedBitmap, null, originalBounds, p);
		}

		public boolean hasTranslucentRoot() {
			return mVPathRenderer.getRootAlpha() < 255;
		}

		/**
		 * @return null when there is no need for alpha paint.
		 */
		public Paint getPaint(ColorFilter filter) {
			if (!hasTranslucentRoot() && filter == null) {
				return null;
			}

			if (mTempPaint == null) {
				mTempPaint = new Paint();
				mTempPaint.setFilterBitmap(true);
			}
			mTempPaint.setAlpha(mVPathRenderer.getRootAlpha());
			mTempPaint.setColorFilter(filter);
			return mTempPaint;
		}

		public void updateCachedBitmap(int width, int height) {
			mCachedBitmap.eraseColor(Color.TRANSPARENT);
			Canvas tmpCanvas = new Canvas(mCachedBitmap);
			mVPathRenderer.draw(tmpCanvas, width, height, null);
		}

		public void createCachedBitmapIfNeeded(int width, int height) {
			if (mCachedBitmap == null || !canReuseBitmap(width, height)) {
				mCachedBitmap = Bitmap.createBitmap(width, height,
				                                    Bitmap.Config.ARGB_8888);
				mCacheDirty = true;
			}
		}

		public boolean canReuseBitmap(int width, int height) {
			if (width == mCachedBitmap.getWidth()
			    && height == mCachedBitmap.getHeight()) {
				return true;
			}
			return false;
		}

		public boolean canReuseCache() {
			if (!mCacheDirty
			    && mCachedThemeAttrs == mThemeAttrs
			    && mCachedTint == mTint
			    && mCachedTintMode == mTintMode
			    && mCachedAutoMirrored == mAutoMirrored
			    && mCachedRootAlpha == mVPathRenderer.getRootAlpha()) {
				return true;
			}
			return false;
		}

		public void updateCacheStates() {
			// Use shallow copy here and shallow comparison in canReuseCache(),
			// likely hit cache miss more, but practically not much difference.
			mCachedThemeAttrs = mThemeAttrs;
			mCachedTint = mTint;
			mCachedTintMode = mTintMode;
			mCachedRootAlpha = mVPathRenderer.getRootAlpha();
			mCachedAutoMirrored = mAutoMirrored;
			mCacheDirty = false;
		}

		// @Override
		// public boolean canApplyTheme() {
		// 	return mThemeAttrs != null
		// 			|| (mVPathRenderer != null && mVPathRenderer.canApplyTheme())
		// 			|| (mTint != null && mTint.canApplyTheme())
		// 			|| super.canApplyTheme();
		// }

		public VectorDrawableState() {
			mVPathRenderer = new VPathRenderer();
		}

		@Override
		public Drawable newDrawable() {
			return new VectorDrawable(this, null);
		}

		@Override
		public Drawable newDrawable(Resources res) {
			return new VectorDrawable(this, res);
		}

		@Override
		public int getChangingConfigurations() {
			return mChangingConfigurations
			     | (mTint != null ? mTint.getChangingConfigurations() : 0);
		}
	}

	private static class VPathRenderer {
		/* Right now the internal data structure is organized as a tree.
		 * Each node can be a group node, or a path.
		 * A group node can have groups or paths as children, but a path node has
		 * no children.
		 * One example can be:
		 *                 Root Group
		 *                /    |     \
		 *           Group    Path    Group
		 *          /     \             |
		 *         Path   Path         Path
		 *
		 */
		// Variables that only used temporarily inside the draw() call, so there
		// is no need for deep copying.
		private final Path mPath;
		private final Path mRenderPath;
		private final Matrix mFinalPathMatrix = new Matrix();

		private Paint mStrokePaint;
		private Paint mFillPaint;
		private PathMeasure mPathMeasure;

		/////////////////////////////////////////////////////
		// Variables below need to be copied (deep copy if applicable) for mutation.
		private int mChangingConfigurations;
		private final VGroup mRootGroup;
		float mBaseWidth = 0;
		float mBaseHeight = 0;
		float mViewportWidth = 0;
		float mViewportHeight = 0;
		// Insets mOpticalInsets = Insets.NONE;
		int mRootAlpha = 0xFF;
		String mRootName = null;

		int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

		final ArrayMap<String, Object> mVGTargetsMap = new ArrayMap<String, Object>();

		public VPathRenderer() {
			mRootGroup = new VGroup();
			mPath = new Path();
			mRenderPath = new Path();
		}

		public void setRootAlpha(int alpha) {
			mRootAlpha = alpha;
		}

		public int getRootAlpha() {
			return mRootAlpha;
		}

		// setAlpha() and getAlpha() are used mostly for animation purpose, since
		// Animator like to use alpha from 0 to 1.
		public void setAlpha(float alpha) {
			setRootAlpha((int)(alpha * 255));
		}

		@SuppressWarnings("unused")
		public float getAlpha() {
			return getRootAlpha() / 255.0f;
		}

		public VPathRenderer(VPathRenderer copy) {
			mRootGroup = new VGroup(copy.mRootGroup, mVGTargetsMap);
			mPath = new Path(copy.mPath);
			mRenderPath = new Path(copy.mRenderPath);
			mBaseWidth = copy.mBaseWidth;
			mBaseHeight = copy.mBaseHeight;
			mViewportWidth = copy.mViewportWidth;
			mViewportHeight = copy.mViewportHeight;
			// mOpticalInsets = copy.mOpticalInsets;
			mChangingConfigurations = copy.mChangingConfigurations;
			mRootAlpha = copy.mRootAlpha;
			mRootName = copy.mRootName;
			mTargetDensity = copy.mTargetDensity;
			if (copy.mRootName != null) {
				mVGTargetsMap.put(copy.mRootName, this);
			}
		}

		public boolean canApplyTheme() {
			// If one of the paths can apply theme, then return true;
			return recursiveCanApplyTheme(mRootGroup);
		}

		private boolean recursiveCanApplyTheme(VGroup currentGroup) {
			// We can do a tree traverse here, if there is one path return true,
			// then we return true for the whole tree.
			final ArrayList<Object> children = currentGroup.mChildren;

			for (int i = 0; i < children.size(); i++) {
				Object child = children.get(i);
				if (child instanceof VGroup) {
					VGroup childGroup = (VGroup)child;
					if (childGroup.canApplyTheme()
					    || recursiveCanApplyTheme(childGroup)) {
						return true;
					}
				} else if (child instanceof VPath) {
					VPath childPath = (VPath)child;
					if (childPath.canApplyTheme()) {
						return true;
					}
				}
			}
			return false;
		}

		public void applyTheme(Theme t) {
			// Apply theme to every path of the tree.
			recursiveApplyTheme(mRootGroup, t);
		}

		private void recursiveApplyTheme(VGroup currentGroup, Theme t) {
			// We can do a tree traverse here, apply theme to all paths which
			// can apply theme.
			final ArrayList<Object> children = currentGroup.mChildren;
			for (int i = 0; i < children.size(); i++) {
				Object child = children.get(i);
				if (child instanceof VGroup) {
					VGroup childGroup = (VGroup)child;
					if (childGroup.canApplyTheme()) {
						childGroup.applyTheme(t);
					}
					recursiveApplyTheme(childGroup, t);
				} else if (child instanceof VPath) {
					VPath childPath = (VPath)child;
					if (childPath.canApplyTheme()) {
						childPath.applyTheme(t);
					}
				}
			}
		}

		private void drawGroupTree(VGroup currentGroup, Matrix currentMatrix,
		                           Canvas canvas, int w, int h, ColorFilter filter) {
			// Calculate current group's matrix by preConcat the parent's and
			// and the current one on the top of the stack.
			// Basically the Mfinal = Mviewport * M0 * M1 * M2;
			// Mi the local matrix at level i of the group tree.
			currentGroup.mStackedMatrix.set(currentMatrix);
			currentGroup.mStackedMatrix.preConcat(currentGroup.mLocalMatrix);

			// Save the current clip information, which is local to this group.
			canvas.save();
			// Draw the group tree in the same order as the XML file.
			for (int i = 0; i < currentGroup.mChildren.size(); i++) {
				Object child = currentGroup.mChildren.get(i);
				if (child instanceof VGroup) {
					VGroup childGroup = (VGroup)child;
					drawGroupTree(childGroup, currentGroup.mStackedMatrix,
					              canvas, w, h, filter);
				} else if (child instanceof VPath) {
					VPath childPath = (VPath)child;
					drawPath(currentGroup, childPath, canvas, w, h, filter);
				}
			}
			canvas.restore();
		}

		public void draw(Canvas canvas, int w, int h, ColorFilter filter) {
			// Travese the tree in pre-order to draw.
			drawGroupTree(mRootGroup, Matrix.IDENTITY_MATRIX, canvas, w, h, filter);
		}

		private void drawPath(VGroup vGroup, VPath vPath, Canvas canvas, int w, int h,
		                      ColorFilter filter) {
			final float scaleX = w / mViewportWidth;
			final float scaleY = h / mViewportHeight;
			final float minScale = Math.min(scaleX, scaleY);
			final Matrix groupStackedMatrix = vGroup.mStackedMatrix;

			mFinalPathMatrix.set(groupStackedMatrix);
			mFinalPathMatrix.postScale(scaleX, scaleY);

			final float matrixScale = getMatrixScale(groupStackedMatrix);
			if (matrixScale == 0) {
				// When either x or y is scaled to 0, we don't need to draw anything.
				return;
			}
			vPath.toPath(mPath);
			final Path path = mPath;

			mRenderPath.reset();

			if (vPath.isClipPath()) {
				mRenderPath.addPath(path, mFinalPathMatrix);
				canvas.clipPath(mRenderPath);
			} else {
				VFullPath fullPath = (VFullPath)vPath;
				if (fullPath.mTrimPathStart != 0.0f || fullPath.mTrimPathEnd != 1.0f) {
					float start = (fullPath.mTrimPathStart + fullPath.mTrimPathOffset) % 1.0f;
					float end = (fullPath.mTrimPathEnd + fullPath.mTrimPathOffset) % 1.0f;

					if (mPathMeasure == null) {
						mPathMeasure = new PathMeasure();
					}
					mPathMeasure.setPath(mPath, false);

					float len = mPathMeasure.getLength();
					start = start * len;
					end = end * len;
					path.reset();
					if (start > end) {
						mPathMeasure.getSegment(start, len, path, true);
						mPathMeasure.getSegment(0f, end, path, true);
					} else {
						mPathMeasure.getSegment(start, end, path, true);
					}
					path.rLineTo(0, 0); // fix bug in measure
				}
				mRenderPath.addPath(path, mFinalPathMatrix);

				if (fullPath.mFillColor != Color.TRANSPARENT) {
					if (mFillPaint == null) {
						mFillPaint = new Paint();
						mFillPaint.setStyle(Paint.Style.FILL);
						mFillPaint.setAntiAlias(true);
					}

					final Paint fillPaint = mFillPaint;
					fillPaint.setColor(applyAlpha(fullPath.mFillColor, fullPath.mFillAlpha));
					fillPaint.setColorFilter(filter);
					canvas.drawPath(mRenderPath, fillPaint);
				}

				if (fullPath.mStrokeColor != Color.TRANSPARENT) {
					if (mStrokePaint == null) {
						mStrokePaint = new Paint();
						mStrokePaint.setStyle(Paint.Style.STROKE);
						mStrokePaint.setAntiAlias(true);
					}

					final Paint strokePaint = mStrokePaint;
					if (fullPath.mStrokeLineJoin != null) {
						strokePaint.setStrokeJoin(fullPath.mStrokeLineJoin);
					}

					if (fullPath.mStrokeLineCap != null) {
						strokePaint.setStrokeCap(fullPath.mStrokeLineCap);
					}

					strokePaint.setStrokeMiter(fullPath.mStrokeMiterlimit);
					strokePaint.setColor(applyAlpha(fullPath.mStrokeColor, fullPath.mStrokeAlpha));
					strokePaint.setColorFilter(filter);
					final float finalStrokeScale = minScale * matrixScale;
					strokePaint.setStrokeWidth(fullPath.mStrokeWidth * finalStrokeScale);
					canvas.drawPath(mRenderPath, strokePaint);
				}
			}
		}

		private float getMatrixScale(Matrix groupStackedMatrix) {
			// Given unit vectors A = (0, 1) and B = (1, 0).
			// After matrix mapping, we got A' and B'. Let theta = the angel b/t A' and B'.
			// Therefore, the final scale we want is min(|A'| * sin(theta), |B'| * sin(theta)),
			// which is (|A'| * |B'| * sin(theta)) / max (|A'|, |B'|);
			// If  max (|A'|, |B'|) = 0, that means either x or y has a scale of 0.
			//
			// For non-skew case, which is most of the cases, matrix scale is computing exactly the
			// scale on x and y axis, and take the minimal of these two.
			// For skew case, an unit square will mapped to a parallelogram. And this function will
			// return the minimal height of the 2 bases.
			float[] unitVectors = new float[] {0, 1, 1, 0};
			groupStackedMatrix.mapVectors(unitVectors);
			float scaleX = MathUtils.mag(unitVectors[0], unitVectors[1]);
			float scaleY = MathUtils.mag(unitVectors[2], unitVectors[3]);
			float crossProduct = MathUtils.cross(unitVectors[0], unitVectors[1],
			                                     unitVectors[2], unitVectors[3]);
			float maxScale = MathUtils.max(scaleX, scaleY);

			float matrixScale = 0;
			if (maxScale > 0) {
				matrixScale = MathUtils.abs(crossProduct) / maxScale;
			}
			if (DBG_VECTOR_DRAWABLE) {
				Log.d(LOGTAG, "Scale x " + scaleX + " y " + scaleY + " final " + matrixScale);
			}
			return matrixScale;
		}
	}

	private static class VGroup {
		// mStackedMatrix is only used temporarily when drawing, it combines all
		// the parents' local matrices with the current one.
		private final Matrix mStackedMatrix = new Matrix();

		/////////////////////////////////////////////////////
		// Variables below need to be copied (deep copy if applicable) for mutation.
		final ArrayList<Object> mChildren = new ArrayList<Object>();

		private float mRotate = 0;
		private float mPivotX = 0;
		private float mPivotY = 0;
		private float mScaleX = 1;
		private float mScaleY = 1;
		private float mTranslateX = 0;
		private float mTranslateY = 0;

		// mLocalMatrix is updated based on the update of transformation information,
		// either parsed from the XML or by animation.
		private final Matrix mLocalMatrix = new Matrix();
		private int mChangingConfigurations;
		private int[] mThemeAttrs;
		private String mGroupName = null;

		public VGroup(VGroup copy, ArrayMap<String, Object> targetsMap) {
			mRotate = copy.mRotate;
			mPivotX = copy.mPivotX;
			mPivotY = copy.mPivotY;
			mScaleX = copy.mScaleX;
			mScaleY = copy.mScaleY;
			mTranslateX = copy.mTranslateX;
			mTranslateY = copy.mTranslateY;
			mThemeAttrs = copy.mThemeAttrs;
			mGroupName = copy.mGroupName;
			mChangingConfigurations = copy.mChangingConfigurations;
			if (mGroupName != null) {
				targetsMap.put(mGroupName, this);
			}

			mLocalMatrix.set(copy.mLocalMatrix);

			final ArrayList<Object> children = copy.mChildren;
			for (int i = 0; i < children.size(); i++) {
				Object copyChild = children.get(i);
				if (copyChild instanceof VGroup) {
					VGroup copyGroup = (VGroup)copyChild;
					mChildren.add(new VGroup(copyGroup, targetsMap));
				} else {
					VPath newPath = null;
					if (copyChild instanceof VFullPath) {
						newPath = new VFullPath((VFullPath)copyChild);
					} else if (copyChild instanceof VClipPath) {
						newPath = new VClipPath((VClipPath)copyChild);
					} else {
						throw new IllegalStateException("Unknown object in the tree!");
					}
					mChildren.add(newPath);
					if (newPath.mPathName != null) {
						targetsMap.put(newPath.mPathName, newPath);
					}
				}
			}
		}

		public VGroup() {
		}

		public String getGroupName() {
			return mGroupName;
		}

		public Matrix getLocalMatrix() {
			return mLocalMatrix;
		}

		public void inflate(Resources res, AttributeSet attrs, Theme theme) {
			final TypedArray a = Resources.obtainAttributes(res, theme, attrs,
			                                                R.styleable.VectorDrawableGroup);
			updateStateFromTypedArray(a);
			a.recycle();
		}

		private void updateStateFromTypedArray(TypedArray a) {
			// Account for any configuration changes.
			mChangingConfigurations |= a.getChangingConfigurations();

			// Extract the theme attributes, if any.
			mThemeAttrs = a.extractThemeAttrs();

			mRotate = a.getFloat(R.styleable.VectorDrawableGroup_rotation, mRotate);
			mPivotX = a.getFloat(R.styleable.VectorDrawableGroup_pivotX, mPivotX);
			mPivotY = a.getFloat(R.styleable.VectorDrawableGroup_pivotY, mPivotY);
			mScaleX = a.getFloat(R.styleable.VectorDrawableGroup_scaleX, mScaleX);
			mScaleY = a.getFloat(R.styleable.VectorDrawableGroup_scaleY, mScaleY);
			mTranslateX = a.getFloat(R.styleable.VectorDrawableGroup_translateX, mTranslateX);
			mTranslateY = a.getFloat(R.styleable.VectorDrawableGroup_translateY, mTranslateY);

			final String groupName = a.getString(R.styleable.VectorDrawableGroup_name);
			if (groupName != null) {
				mGroupName = groupName;
			}

			updateLocalMatrix();
		}

		public boolean canApplyTheme() {
			return mThemeAttrs != null;
		}

		public void applyTheme(Theme t) {
			if (mThemeAttrs == null) {
				return;
			}

			final TypedArray a = t.resolveAttributes(mThemeAttrs, R.styleable.VectorDrawableGroup);
			updateStateFromTypedArray(a);
			a.recycle();
		}

		private void updateLocalMatrix() {
			// The order we apply is the same as the
			// RenderNode.cpp::applyViewPropertyTransforms().
			mLocalMatrix.reset();
			mLocalMatrix.postTranslate(-mPivotX, -mPivotY);
			mLocalMatrix.postScale(mScaleX, mScaleY);
			mLocalMatrix.postRotate(mRotate, 0, 0);
			mLocalMatrix.postTranslate(mTranslateX + mPivotX, mTranslateY + mPivotY);
		}

		/* Setters and Getters, used by animator from AnimatedVectorDrawable. */
		@SuppressWarnings("unused")
		public float getRotation() {
			return mRotate;
		}

		@SuppressWarnings("unused")
		public void setRotation(float rotation) {
			if (rotation != mRotate) {
				mRotate = rotation;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getPivotX() {
			return mPivotX;
		}

		@SuppressWarnings("unused")
		public void setPivotX(float pivotX) {
			if (pivotX != mPivotX) {
				mPivotX = pivotX;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getPivotY() {
			return mPivotY;
		}

		@SuppressWarnings("unused")
		public void setPivotY(float pivotY) {
			if (pivotY != mPivotY) {
				mPivotY = pivotY;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getScaleX() {
			return mScaleX;
		}

		@SuppressWarnings("unused")
		public void setScaleX(float scaleX) {
			if (scaleX != mScaleX) {
				mScaleX = scaleX;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getScaleY() {
			return mScaleY;
		}

		@SuppressWarnings("unused")
		public void setScaleY(float scaleY) {
			if (scaleY != mScaleY) {
				mScaleY = scaleY;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getTranslateX() {
			return mTranslateX;
		}

		@SuppressWarnings("unused")
		public void setTranslateX(float translateX) {
			if (translateX != mTranslateX) {
				mTranslateX = translateX;
				updateLocalMatrix();
			}
		}

		@SuppressWarnings("unused")
		public float getTranslateY() {
			return mTranslateY;
		}

		@SuppressWarnings("unused")
		public void setTranslateY(float translateY) {
			if (translateY != mTranslateY) {
				mTranslateY = translateY;
				updateLocalMatrix();
			}
		}
	}

	/**
	 * Common Path information for clip path and normal path.
	 */
	private static class VPath {
		protected PathParser.PathDataNode[] mNodes = null;
		String mPathName;
		int mChangingConfigurations;

		public VPath() {
			// Empty constructor.
		}

		public VPath(VPath copy) {
			mPathName = copy.mPathName;
			mChangingConfigurations = copy.mChangingConfigurations;
			mNodes = PathParser.deepCopyNodes(copy.mNodes);
		}

		public void toPath(Path path) {
			path.reset();
			if (mNodes != null) {
				PathParser.PathDataNode.nodesToPath(mNodes, path);
			}
		}

		public String getPathName() {
			return mPathName;
		}

		public boolean canApplyTheme() {
			return false;
		}

		public void applyTheme(Theme t) {
		}

		public boolean isClipPath() {
			return false;
		}

		/* Setters and Getters, used by animator from AnimatedVectorDrawable. */
		@SuppressWarnings("unused")
		public PathParser.PathDataNode[] getPathData() {
			return mNodes;
		}

		@SuppressWarnings("unused")
		public void setPathData(PathParser.PathDataNode[] nodes) {
			if (!PathParser.canMorph(mNodes, nodes)) {
				// This should not happen in the middle of animation.
				mNodes = PathParser.deepCopyNodes(nodes);
			} else {
				PathParser.updateNodes(mNodes, nodes);
			}
		}
	}

	/**
	 * Clip path, which only has name and pathData.
	 */
	private static class VClipPath extends VPath {
		public VClipPath() {
			// Empty constructor.
		}

		public VClipPath(VClipPath copy) {
			super(copy);
		}

		public void inflate(Resources r, AttributeSet attrs, Theme theme) {
			final TypedArray a = Resources.obtainAttributes(r, theme, attrs,
			                                                R.styleable.VectorDrawableClipPath);
			updateStateFromTypedArray(a);
			a.recycle();
		}

		private void updateStateFromTypedArray(TypedArray a) {
			// Account for any configuration changes.
			mChangingConfigurations |= a.getChangingConfigurations();

			final String pathName = a.getString(R.styleable.VectorDrawableClipPath_name);
			if (pathName != null) {
				mPathName = pathName;
			}

			final String pathData = a.getString(R.styleable.VectorDrawableClipPath_pathData);
			if (pathData != null) {
				mNodes = PathParser.createNodesFromPathData(pathData);
			}
		}

		@Override
		public boolean isClipPath() {
			return true;
		}
	}

	/**
	 * Normal path, which contains all the fill / paint information.
	 */
	private static class VFullPath extends VPath {
		/////////////////////////////////////////////////////
		// Variables below need to be copied (deep copy if applicable) for mutation.
		private int[] mThemeAttrs;

		int mStrokeColor = Color.TRANSPARENT;
		float mStrokeWidth = 0;

		int mFillColor = Color.TRANSPARENT;
		float mStrokeAlpha = 1.0f;
		int mFillRule;
		float mFillAlpha = 1.0f;
		float mTrimPathStart = 0;
		float mTrimPathEnd = 1;
		float mTrimPathOffset = 0;

		Paint.Cap mStrokeLineCap = Paint.Cap.BUTT;
		Paint.Join mStrokeLineJoin = Paint.Join.MITER;
		float mStrokeMiterlimit = 4;

		public VFullPath() {
			// Empty constructor.
		}

		public VFullPath(VFullPath copy) {
			super(copy);
			mThemeAttrs = copy.mThemeAttrs;

			mStrokeColor = copy.mStrokeColor;
			mStrokeWidth = copy.mStrokeWidth;
			mStrokeAlpha = copy.mStrokeAlpha;
			mFillColor = copy.mFillColor;
			mFillRule = copy.mFillRule;
			mFillAlpha = copy.mFillAlpha;
			mTrimPathStart = copy.mTrimPathStart;
			mTrimPathEnd = copy.mTrimPathEnd;
			mTrimPathOffset = copy.mTrimPathOffset;

			mStrokeLineCap = copy.mStrokeLineCap;
			mStrokeLineJoin = copy.mStrokeLineJoin;
			mStrokeMiterlimit = copy.mStrokeMiterlimit;
		}

		private Paint.Cap getStrokeLineCap(int id, Paint.Cap defValue) {
			switch (id) {
				case LINECAP_BUTT:
					return Paint.Cap.BUTT;
				case LINECAP_ROUND:
					return Paint.Cap.ROUND;
				case LINECAP_SQUARE:
					return Paint.Cap.SQUARE;
				default:
					return defValue;
			}
		}

		private Paint.Join getStrokeLineJoin(int id, Paint.Join defValue) {
			switch (id) {
				case LINEJOIN_MITER:
					return Paint.Join.MITER;
				case LINEJOIN_ROUND:
					return Paint.Join.ROUND;
				case LINEJOIN_BEVEL:
					return Paint.Join.BEVEL;
				default:
					return defValue;
			}
		}

		@Override
		public boolean canApplyTheme() {
			return mThemeAttrs != null;
		}

		public void inflate(Resources r, AttributeSet attrs, Theme theme) {
			final TypedArray a = Resources.obtainAttributes(r, theme, attrs,
			                                                R.styleable.VectorDrawablePath);
			updateStateFromTypedArray(a);
			a.recycle();
		}

		private void updateStateFromTypedArray(TypedArray a) {
			// Account for any configuration changes.
			mChangingConfigurations |= a.getChangingConfigurations();

			// Extract the theme attributes, if any.
			mThemeAttrs = a.extractThemeAttrs();

			final String pathName = a.getString(R.styleable.VectorDrawablePath_name);
			if (pathName != null) {
				mPathName = pathName;
			}

			final String pathData = a.getString(R.styleable.VectorDrawablePath_pathData);
			if (pathData != null) {
				mNodes = PathParser.createNodesFromPathData(pathData);
			}

			ComplexColor fillColor = a.getComplexColor(R.styleable.VectorDrawablePath_fillColor);
			if (fillColor != null)
				mFillColor = fillColor.getDefaultColor();
			mFillAlpha = a.getFloat(R.styleable.VectorDrawablePath_fillAlpha,
			                        mFillAlpha);
			mStrokeLineCap = getStrokeLineCap(a.getInt(
							      R.styleable.VectorDrawablePath_strokeLineCap, -1),
			                                  mStrokeLineCap);
			mStrokeLineJoin = getStrokeLineJoin(a.getInt(
								R.styleable.VectorDrawablePath_strokeLineJoin, -1),
			                                    mStrokeLineJoin);
			mStrokeMiterlimit = a.getFloat(
			    R.styleable.VectorDrawablePath_strokeMiterLimit, mStrokeMiterlimit);
			ComplexColor strokeColor = a.getComplexColor(R.styleable.VectorDrawablePath_strokeColor);
			if (strokeColor != null)
				mStrokeColor = strokeColor.getDefaultColor();
			mStrokeAlpha = a.getFloat(R.styleable.VectorDrawablePath_strokeAlpha,
			                          mStrokeAlpha);
			mStrokeWidth = a.getFloat(R.styleable.VectorDrawablePath_strokeWidth,
			                          mStrokeWidth);
			mTrimPathEnd = a.getFloat(R.styleable.VectorDrawablePath_trimPathEnd,
			                          mTrimPathEnd);
			mTrimPathOffset = a.getFloat(
			    R.styleable.VectorDrawablePath_trimPathOffset, mTrimPathOffset);
			mTrimPathStart = a.getFloat(
			    R.styleable.VectorDrawablePath_trimPathStart, mTrimPathStart);
		}

		@Override
		public void applyTheme(Theme t) {
			if (mThemeAttrs == null) {
				return;
			}

			final TypedArray a = t.resolveAttributes(mThemeAttrs, R.styleable.VectorDrawablePath);
			updateStateFromTypedArray(a);
			a.recycle();
		}

		/* Setters and Getters, used by animator from AnimatedVectorDrawable. */
		@SuppressWarnings("unused")
		int getStrokeColor() {
			return mStrokeColor;
		}

		@SuppressWarnings("unused")
		void setStrokeColor(int strokeColor) {
			mStrokeColor = strokeColor;
		}

		@SuppressWarnings("unused")
		float getStrokeWidth() {
			return mStrokeWidth;
		}

		@SuppressWarnings("unused")
		void setStrokeWidth(float strokeWidth) {
			mStrokeWidth = strokeWidth;
		}

		@SuppressWarnings("unused")
		float getStrokeAlpha() {
			return mStrokeAlpha;
		}

		@SuppressWarnings("unused")
		void setStrokeAlpha(float strokeAlpha) {
			mStrokeAlpha = strokeAlpha;
		}

		@SuppressWarnings("unused")
		int getFillColor() {
			return mFillColor;
		}

		@SuppressWarnings("unused")
		void setFillColor(int fillColor) {
			mFillColor = fillColor;
		}

		@SuppressWarnings("unused")
		float getFillAlpha() {
			return mFillAlpha;
		}

		@SuppressWarnings("unused")
		void setFillAlpha(float fillAlpha) {
			mFillAlpha = fillAlpha;
		}

		@SuppressWarnings("unused")
		float getTrimPathStart() {
			return mTrimPathStart;
		}

		@SuppressWarnings("unused")
		void setTrimPathStart(float trimPathStart) {
			mTrimPathStart = trimPathStart;
		}

		@SuppressWarnings("unused")
		float getTrimPathEnd() {
			return mTrimPathEnd;
		}

		@SuppressWarnings("unused")
		void setTrimPathEnd(float trimPathEnd) {
			mTrimPathEnd = trimPathEnd;
		}

		@SuppressWarnings("unused")
		float getTrimPathOffset() {
			return mTrimPathOffset;
		}

		@SuppressWarnings("unused")
		void setTrimPathOffset(float trimPathOffset) {
			mTrimPathOffset = trimPathOffset;
		}
	}
}
