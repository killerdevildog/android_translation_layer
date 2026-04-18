/*
 * Copyright (C) 2008 The Android Open Source Project
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
package com.android.internal.util;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Predicates contains static methods for creating the standard set of
 * {@code Predicate} objects.
 */
public class Predicates {
	private Predicates() {
	}

	private static <T> List<T> defensiveCopy(T... array) {
		return defensiveCopy(Arrays.asList(array));
	}

	static <T> List<T> defensiveCopy(Iterable<T> iterable) {
		ArrayList<T> list = new ArrayList<>();
		for (T element : iterable) {
			list.add(/*checkNotNull(*/ element /*)*/);
		}
		return list;
	}

	private static String toStringHelper(String methodName, Iterable<?> components) {
		StringBuilder builder = new StringBuilder("Predicates.").append(methodName).append('(');
		boolean first = true;
		for (Object o : components) {
			if (!first) {
				builder.append(',');
			}
			builder.append(o);
			first = false;
		}
		return builder.append(')').toString();
	}

	private static class OrPredicate<T extends /*@Nullable*/ Object>
	    implements Predicate<T>, Serializable {
		private final List<? extends Predicate<? super T>> components;

		private OrPredicate(List<? extends Predicate<? super T>> components) {
			this.components = components;
		}

		@Override
		public boolean apply(/*@ParametricNullness*/ T t) {
			// Avoid using the Iterator to avoid generating garbage (issue 820).
			for (int i = 0; i < components.size(); i++) {
				if (components.get(i).apply(t)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			// add a random number to avoid collisions with AndPredicate
			return components.hashCode() + 0x053c91cf;
		}

		@Override
		public boolean equals(/*@CheckForNull*/ Object obj) {
			if (obj instanceof OrPredicate) {
				OrPredicate<?> that = (OrPredicate<?>)obj;
				return components.equals(that.components);
			}
			return false;
		}

		@Override
		public String toString() {
			return toStringHelper("or", components);
		}

		private static final long serialVersionUID = 0;
	}

	public static <T extends /*@Nullable*/ Object> Predicate<T> or(Predicate<? super T>... components) {
		return new OrPredicate<T>(defensiveCopy(components));
	}

	/**
     * Returns a Predicate that evaluates to true iff the given Predicate
     * evaluates to false.
     */
	public static <T> Predicate<T> not(Predicate<? super T> predicate) {
		return new NotPredicate<T>(predicate);
	}

	private static class NotPredicate<T> implements Predicate<T> {
		private final Predicate<? super T> predicate;
		private NotPredicate(Predicate<? super T> predicate) {
			this.predicate = predicate;
		}
		public boolean apply(T t) {
			return !predicate.apply(t);
		}
	}
}
