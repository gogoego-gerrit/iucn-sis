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
package com.solertium.lwxml.shared.utils;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unchecked")
public class ArrayUtils 
{
	private static final int CUTOFF = 10;

	/**
	 * Entry to insertionSort.
	 * @param a an List of Comparable items.
	 */
	public static void insertionSort( List a, Comparator comparator )
	{
		if( a.size() < 1 )
			return; 
	
		insertionSort( a, 0, a.size()-1, comparator );
	}
	
	/**
	 * Quicksort algorithm.
	 * @param a an List of Comparable items.
	 */
	public static void quicksort( List a, Comparator comparator )
	{
		if( a.size() < 1 )
			return; 
	
		quicksort( a, 0, a.size()-1, comparator );
	}
	
	/**
	 * Quicksort algorithm.
	 * @param a an array of Comparable items.
	 */
	public static void quicksort( Comparable [ ] a )
	{
		if( a.length < 1 )
			return;
		
		quicksort( a, 0, a.length - 1 );
	}

	/**
	 * Method to swap to elements in an array.
	 * @param a an array of objects.
	 * @param index1 the index of the first object.
	 * @param index2 the index of the second object.
	 */
	public static final void swapReferences( Object [ ] a, int index1, int index2 )
	{
		Object tmp = a[ index1 ];
		a[ index1 ] = a[ index2 ];
		a[ index2 ] = tmp;
	}
	public static final void swapReferences( List a, int index1, int index2 )
	{
		Object tmp = a.get( index1 );
		a.set( index1, a.get( index2 ) );
		a.set( index2, tmp );
	}

	/**
	 * Internal quicksort method that makes recursive calls.
	 * Uses median-of-three partitioning and a cutoff of 10.
	 * @param a an array of Comparable items.
	 * @param low the left-most index of the subarray.
	 * @param high the right-most index of the subarray.
	 */
	private static void quicksort( Comparable [ ] a, int low, int high )
	{
		if( low + CUTOFF > high )
			insertionSort( a, low, high );
		else
		{
			// Sort low, middle, high
			int middle = ( low + high ) / 2;
			if( a[ middle ].compareTo( a[ low ] ) < 0 )
				swapReferences( a, low, middle );
			if( a[ high ].compareTo( a[ low ] ) < 0 )
				swapReferences( a, low, high );
			if( a[ high ].compareTo( a[ middle ] ) < 0 )
				swapReferences( a, middle, high );

			// Place pivot at position high - 1
			swapReferences( a, middle, high - 1 );
			Comparable pivot = a[ high - 1 ];

			// Begin partitioning
			int i, j;
			for( i = low, j = high - 1; ; )
			{
				while( a[ ++i ].compareTo( pivot ) < 0 );
				while( pivot.compareTo( a[ --j ] ) < 0 );
				if( i >= j )
					break;
				swapReferences( a, i, j );
			}

			// Restore pivot
			swapReferences( a, i, high - 1 );

			quicksort( a, low, i - 1 );    // Sort small elements
			quicksort( a, i + 1, high );   // Sort large elements
		}
	}
	
	/**
	 * Internal quicksort method that makes recursive calls.
	 * Uses a Comparator to perform comparisons - Objects in a need not be Comparable items.
	 * @param a an array of Objects.
	 * @param low the left-most index of the subarray.
	 * @param high the right-most index of the subarray.
	 * @param a comparator that performs a comparison on the Obejcts in a
	 */
	private static void quicksort( List a, int low, int high, Comparator comparator )
	{
		if( low + CUTOFF > high )
			insertionSort( a, low, high, comparator );
		else
		{
			// Sort low, middle, high
			int middle = ( low + high ) / 2;
			if( comparator.compare( a.get( middle ), a.get( low ) ) < 0 )
				swapReferences( a, low, middle );
			if( comparator.compare( a.get( high ), a.get( low ) ) < 0 )
				swapReferences( a, low, high );
			if( comparator.compare( a.get( high ), a.get( middle ) ) < 0 )
				swapReferences( a, middle, high );

			// Place pivot at position high - 1
			swapReferences( a, middle, high - 1 );
			Object pivot = a.get( high - 1 );

			// Begin partitioning
			int i, j;
			for( i = low, j = high - 1; ; )
			{
				while( comparator.compare( a.get( ++i ), pivot ) < 0 );
				while( comparator.compare( pivot, a.get( --j ) ) < 0 );
				if( i >= j )
					break;
				swapReferences( a, i, j );
			}

			// Restore pivot
			swapReferences( a, i, high - 1 );

			quicksort( a, low, i - 1, comparator );    // Sort small elements
			quicksort( a, i + 1, high, comparator );   // Sort large elements
		}
	}

	
	/**
	 * Internal insertion sort routine for subarrays
	 * that is used by quicksort.
	 * @param a an array of Comparable items.
	 * @param low the left-most index of the subarray.
	 * @param n the number of items to sort.
	 */
	private static void insertionSort( Comparable [ ] a, int low, int high )
	{
		for( int p = low + 1; p <= high; p++ )
		{
			Comparable tmp = a[ p ];
			int j;

			for( j = p; j > low && tmp.compareTo( a[ j - 1 ] ) < 0; j-- )
				a[ j ] = a[ j - 1 ];
			a[ j ] = tmp;
		}
	}
	private static void insertionSort( List a, int low, int high, Comparator comparator )
	{
		for( int p = low + 1; p <= high; p++ )
		{
			Object tmp = a.get( p );
			int j;

			for( j = p; j > low && (comparator.compare( tmp, a.get( j - 1 ) ) < 0); j-- )
				a.set( j, a.get( j - 1 ));
			a.set( j, tmp );
		}
	}
}
