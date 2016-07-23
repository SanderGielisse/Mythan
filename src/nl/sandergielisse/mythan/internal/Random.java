/**
 * Copyright 2016 Alexander Gielisse
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.sandergielisse.mythan.internal;

import java.util.List;
import java.util.Set;

public class Random {

	private static final java.util.Random random = new java.util.Random();

	private static java.util.Random getRandom() {
		return random;
	}

	/**
	 * Returns a random object from the given array.
	 */
	public static <T> T random(T[] array) {
		if (array.length == 0)
			throw new UnsupportedOperationException("Given array can not be empty");

		return array[getRandom().nextInt(array.length)];
	}

	/**
	 * Returns a random object from the given set.
	 */
	public static <T> T random(Set<T> set) {
		if (set.size() == 0)
			throw new UnsupportedOperationException("Given set can not be empty");

		int size = set.size();
		int item = getRandom().nextInt(size);
		int i = 0;
		for (T t : set) {
			if (i == item)
				return t;
			i = i + 1;
		}
		throw new AssertionError();
	}

	/**
	 * Returns a random object from the given list.
	 */
	public static <T> T random(List<T> list) {
		if (list.size() == 0)
			throw new UnsupportedOperationException("Given list can not be empty");

		return list.get(getRandom().nextInt(list.size()));
	}

	/**
	 * Picks a random number X (for which 0 <= X < 1) and returns true if the random number is smaller than the chance.
	 * 
	 * The smaller the given chance, the more unlikely this method will return true.
	 */
	public static boolean success(double chance) {
		return getRandom().nextDouble() <= chance;
	}

	/**
	 * Returns a random double between a given min and max
	 */
	public static double random(double min, double max) {
		if (min >= max)
			throw new IllegalArgumentException("Min (" + min + ") can not be bigger than or equal to max (" + max + ")");
		return min + (max - min) * getRandom().nextDouble();
	}
}
