/*
 * Copyright (C) 2009 Solertium Corporation
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

package com.solertium.lwxml.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This class wraps a NativeNodeList with List semantics including collection
 * generics, to avoid the ugly casts and array iterations associated with
 * NativeNodeList.  This is a port of the NodeCollection utility in
 * com.solertium.util.
 * <p>
 * 
 * Usage:<p>
 * 
 * <code>
 * NodeCollection nodeCollection = new NodeCollection(nodeList);<br>
 * for(Node node : nodeCollection){<br>
 *   // operate on node<br>
 * }<br>
 * </code>
 * 
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class NativeNodeCollection implements List<NativeNode> {

	private final NativeNodeList wrappedNodeList;

	public NativeNodeCollection(final NativeNodeList wrappedNodeList) {
		this.wrappedNodeList = wrappedNodeList;
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean add(NativeNode arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void add(int arg0, NativeNode arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(Collection<? extends NativeNode> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(int arg0, Collection<? extends NativeNode> arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Object arg0) {
		for (int i = 0; i < wrappedNodeList.getLength(); i++) {
			if (wrappedNodeList.item(i).equals(arg0))
				return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsAll(Collection<?> arg0) {
		for (Object o : arg0) {
			if (!contains(o))
				return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public NativeNode get(int arg0) {
		return wrappedNodeList.item(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public int indexOf(Object arg0) {
		for (int i = 0; i < wrappedNodeList.getLength(); i++) {
			if (wrappedNodeList.item(i).equals(arg0))
				return i;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		if (wrappedNodeList.getLength() < 1)
			return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<NativeNode> iterator() {
		return new Iterator<NativeNode>() {

			private int pos = 0;

			public boolean hasNext() {
				if (pos >= wrappedNodeList.getLength())
					return false;
				return true;
			}

			public NativeNode next() {
				return wrappedNodeList.item(pos++);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * {@inheritDoc}
	 */
	public int lastIndexOf(Object arg0) {
		for (int i = wrappedNodeList.getLength(); i >= 0; i--) {
			if (wrappedNodeList.item(i).equals(arg0))
				return i;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<NativeNode> listIterator() {
		return new ListIterator<NativeNode>() {

			private int pos = 0;

			public boolean hasNext() {
				if (pos >= wrappedNodeList.getLength())
					return false;
				return true;
			}

			public NativeNode next() {
				return wrappedNodeList.item(pos++);
			}

			/**
			 * Modification of the collection is not supported.
			 * 
			 * @throws UnsupportedOperationException
			 */

			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Modification of the collection is not supported.
			 * 
			 * @throws UnsupportedOperationException
			 */

			public void add(NativeNode arg0) {
				throw new UnsupportedOperationException();
			}

			public boolean hasPrevious() {
				if (pos <= 0)
					return false;
				return true;
			}

			public int nextIndex() {
				if (pos >= wrappedNodeList.getLength())
					throw new IndexOutOfBoundsException();
				return pos++;
			}

			public NativeNode previous() {
				return wrappedNodeList.item(pos--);
			}

			public int previousIndex() {
				if (pos == 0)
					throw new IndexOutOfBoundsException();
				return pos--;
			}

			/**
			 * Modification of the collection is not supported.
			 * 
			 * @throws UnsupportedOperationException
			 */

			public void set(NativeNode arg0) {
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<NativeNode> listIterator(final int arg0) {
		return new ListIterator<NativeNode>() {

			private int pos = arg0;

			/**
			 * {@inheritDoc}
			 */

			public boolean hasNext() {
				if (pos >= wrappedNodeList.getLength())
					return false;
				return true;
			}

			/**
			 * {@inheritDoc}
			 */

			public NativeNode next() {
				return wrappedNodeList.item(pos++);
			}

			/**
			 * Modification of the collection is not supported.
			 * 
			 * @throws UnsupportedOperationException
			 */

			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Modification of the collection is not supported.
			 * 
			 * @throws UnsupportedOperationException
			 */

			public void add(NativeNode arg0) {
				throw new UnsupportedOperationException();
			}

			/**
			 * {@inheritDoc}
			 */

			public boolean hasPrevious() {
				if (pos <= 0)
					return false;
				return true;
			}

			/**
			 * {@inheritDoc}
			 */

			public int nextIndex() {
				if (pos >= wrappedNodeList.getLength())
					throw new IndexOutOfBoundsException();
				return pos++;
			}

			/**
			 * {@inheritDoc}
			 */

			public NativeNode previous() {
				return wrappedNodeList.item(pos--);
			}

			/**
			 * {@inheritDoc}
			 */

			public int previousIndex() {
				if (pos == 0)
					throw new IndexOutOfBoundsException();
				return pos--;
			}

			/**
			 * Modification of the collection is not supported.
			 * 
			 * @throws UnsupportedOperationException
			 */

			public void set(NativeNode arg0) {
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public NativeNode remove(int arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public NativeNode set(int arg0, NativeNode arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return wrappedNodeList.getLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<NativeNode> subList(int arg0, int arg1) {
		ArrayList<NativeNode> al = new ArrayList<NativeNode>();
		for (int i = arg0; i < arg1; i++)
			al.add(wrappedNodeList.item(i));
		return al;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] toArray() {
		NativeNode[] nodes = new NativeNode[wrappedNodeList.getLength()];
		for (int i = 0; i < wrappedNodeList.getLength(); i++) {
			nodes[i] = wrappedNodeList.item(i);
		}
		return nodes;
	}

	/**
	 * Casting to arbitrary types is not supported.
	 * 
	 * @throws UnsupportedOperationException
	 */
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	}

}
