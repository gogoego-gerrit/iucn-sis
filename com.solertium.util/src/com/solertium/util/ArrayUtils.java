/*
 * Copyright (C) 2007-2009 Solertium Corporation
 *
 * This file is part of the open source GoGoEgo project.
 *
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 *
 * 2) The GNU General Public License, version 2 or later
 *    http://www.gnu.org/licenses
 */

package com.solertium.util;

import java.util.ArrayList;
import java.util.Comparator;

public class ArrayUtils {
	private static final int CUTOFF = 10;

	/**
	 * Internal insertion sort routine for subarrays that is used by quicksort.
	 * 
	 * @param a
	 *            an array of Comparable items.
	 * @param low
	 *            the left-most index of the subarray.
	 * @param n
	 *            the number of items to sort.
	 */
	@SuppressWarnings("unchecked")
	private static void insertionSort(final Comparable[] a, final int low,
			final int high) {
		for (int p = low + 1; p <= high; p++) {
			final Comparable tmp = a[p];
			int j;

			for (j = p; (j > low) && (tmp.compareTo(a[j - 1]) < 0); j--)
				a[j] = a[j - 1];
			a[j] = tmp;
		}
	}

	/**
	 * Quicksort algorithm.
	 * 
	 * @param a
	 *            an arraylist of Comparable items.
	 */
	public static void quicksort(final ArrayList<Object> a,
			final Comparator<Object> comparator) {
		ArrayUtils.quicksort(a, 0, a.size() - 1, comparator);
	}

	/**
	 * Internal quicksort method that makes recursive calls. Uses a Comparator
	 * to perform comparisons - Objects in a need not be Comparable items.
	 * 
	 * @param a
	 *            an array of Objects.
	 * @param low
	 *            the left-most index of the subarray.
	 * @param high
	 *            the right-most index of the subarray.
	 * @param a
	 *            comparator that performs a comparison on the Obejcts in a
	 */
	private static void quicksort(final ArrayList<Object> a, final int low,
			final int high, final Comparator<Object> comparator) {
		// Sort low, middle, high
		final int middle = (low + high) / 2;
		if (comparator.compare(a.get(middle), a.get(low)) < 0)
			ArrayUtils.swapReferences(a, low, middle);
		if (comparator.compare(a.get(high), a.get(low)) < 0)
			ArrayUtils.swapReferences(a, low, high);
		if (comparator.compare(a.get(high), a.get(middle)) < 0)
			ArrayUtils.swapReferences(a, middle, high);

		// Place pivot at position high - 1
		ArrayUtils.swapReferences(a, middle, high - 1);
		final Object pivot = a.get(high - 1);

		// Begin partitioning
		int i, j;
		for (i = low, j = high - 1;;) {
			while (comparator.compare(a.get(++i), (pivot)) < 0)
				;
			while (comparator.compare(pivot, a.get(--j)) < 0)
				;
			if (i >= j)
				break;
			ArrayUtils.swapReferences(a, i, j);
		}

		// Restore pivot
		ArrayUtils.swapReferences(a, i, high - 1);

		ArrayUtils.quicksort(a, low, i - 1, comparator); // Sort small
		// elements
		ArrayUtils.quicksort(a, i + 1, high, comparator); // Sort large
		// elements

	}

	/**
	 * Quicksort algorithm.
	 * 
	 * @param a
	 *            an array of Comparable items.
	 */
	@SuppressWarnings("unchecked")
	public static void quicksort(final Comparable[] a) {
		ArrayUtils.quicksort(a, 0, a.length - 1);
	}

	/**
	 * Internal quicksort method that makes recursive calls. Uses
	 * median-of-three partitioning and a cutoff of 10.
	 * 
	 * @param a
	 *            an array of Comparable items.
	 * @param low
	 *            the left-most index of the subarray.
	 * @param high
	 *            the right-most index of the subarray.
	 */
	@SuppressWarnings("unchecked")
	private static void quicksort(final Comparable[] a, final int low,
			final int high) {
		if (low + CUTOFF > high)
			ArrayUtils.insertionSort(a, low, high);
		else {
			// Sort low, middle, high
			final int middle = (low + high) >>> 1;
			if (a[middle].compareTo(a[low]) < 0)
				ArrayUtils.swapReferences(a, low, middle);
			if (a[high].compareTo(a[low]) < 0)
				ArrayUtils.swapReferences(a, low, high);
			if (a[high].compareTo(a[middle]) < 0)
				ArrayUtils.swapReferences(a, middle, high);

			// Place pivot at position high - 1
			ArrayUtils.swapReferences(a, middle, high - 1);
			final Comparable pivot = a[high - 1];

			// Begin partitioning
			int i, j;
			for (i = low, j = high - 1;;) {
				while (a[++i].compareTo(pivot) < 0)
					;
				while (pivot.compareTo(a[--j]) < 0)
					;
				if (i >= j)
					break;
				ArrayUtils.swapReferences(a, i, j);
			}

			// Restore pivot
			ArrayUtils.swapReferences(a, i, high - 1);

			ArrayUtils.quicksort(a, low, i - 1); // Sort small elements
			ArrayUtils.quicksort(a, i + 1, high); // Sort large elements
		}
	}

	public static final void swapReferences(final ArrayList<Object> a,
			final int index1, final int index2) {
		final Object tmp = a.get(index1);
		a.set(index1, a.get(index2));
		a.set(index2, tmp);
	}

	/**
	 * Method to swap to elements in an array.
	 * 
	 * @param a
	 *            an array of objects.
	 * @param index1
	 *            the index of the first object.
	 * @param index2
	 *            the index of the second object.
	 */
	public static final void swapReferences(final Object[] a, final int index1,
			final int index2) {
		final Object tmp = a[index1];
		a[index1] = a[index2];
		a[index2] = tmp;
	}

}
