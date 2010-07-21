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
 * This class wraps a NativeNodeList with List semantics including
 * collection generics, to avoid the ugly casts and array iterations
 * associated with NativeNodeList.  The NativeElementCollection is appropriate
 * when it is known that the NativeNodeList contains only NativeElement objects,
 * for example, as the result of a getNativeElementsByTagName() call.<p>
 * 
 * The class does not check the type of the nodes in the NativeNodeList
 * at construction time; a ClassCastException will occur at runtime
 * if the contained nodes are not all NativeElements.<p>
 * 
 * To avoid this in common use cases, use the provided statics for
 * fetching childNativeElements or childNativeElementsByTagName of a particular
 * node.  This also provides a workaround for the deep-traversal
 * tag name matching behavior of the DOM API when this is not
 * desirable.
 * 
 * Usage:<p>
 * 
 * <code>
 * NativeElementCollection elementCollection =<br>
 *   new NativeElementCollection(someNativeElement.getNativeElementsByTagName("div"));<br>
 * for(NativeElement element : elementCollection){<br>
 *   // operate on element<br>
 * }<br>
 * </code>
 *  
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class NativeElementCollection implements List<NativeElement> {

	private NativeNodeList wrappedNativeNodeList;

	public static NativeElementCollection childNativeElements(NativeNode parent){
		ArrayNativeNodeList scratch = new ArrayNativeNodeList();
		for(NativeNode n : new NativeNodeCollection(parent.getChildNodes())){
			if(n.getNodeType() != NativeNode.ELEMENT_NODE) continue;
			scratch.add((NativeElement)n);
		}
		return new NativeElementCollection(scratch);
	}

	public static NativeElementCollection childNativeElementsByTagName(NativeNode parent, String tagName){
		ArrayNativeNodeList scratch = new ArrayNativeNodeList();
		for(NativeNode n : new NativeNodeCollection(parent.getChildNodes())){
			if(n.getNodeType() != NativeNode.ELEMENT_NODE) continue;
			if(!n.getNodeName().equals(tagName)) continue;
			scratch.add((NativeElement)n);
		}
		return new NativeElementCollection(scratch);
	}
	
	public NativeElementCollection(final NativeNodeList wrappedNativeNodeList){
		this.wrappedNativeNodeList = wrappedNativeNodeList;
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean add(NativeElement arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public void add(int arg0, NativeElement arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(Collection<? extends NativeElement> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(int arg0, Collection<? extends NativeElement> arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Object arg0) {
		for(int i=0;i<wrappedNativeNodeList.getLength();i++){
			if(wrappedNativeNodeList.item(i).equals(arg0)) return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsAll(Collection<?> arg0) {
		for(Object o : arg0){
			if(!contains(o)) return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public NativeElement get(int arg0) {
		return (NativeElement) wrappedNativeNodeList.item(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public int indexOf(Object arg0) {
		for(int i=0;i<wrappedNativeNodeList.getLength();i++){
			if(wrappedNativeNodeList.item(i).equals(arg0)) return i;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		if(wrappedNativeNodeList.getLength()<1) return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<NativeElement> iterator() {
		return new Iterator<NativeElement>(){
			
			private int pos = 0;

			
			public boolean hasNext() {
				if(pos>=wrappedNativeNodeList.getLength()) return false;
				return true;
			}

			
			public NativeElement next() {
				return (NativeElement) wrappedNativeNodeList.item(pos++);
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public int lastIndexOf(Object arg0) {
		for(int i=wrappedNativeNodeList.getLength();i>=0;i--){
			if(wrappedNativeNodeList.item(i).equals(arg0)) return i;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<NativeElement> listIterator() {
		return new ListIterator<NativeElement>(){
			
			private int pos = 0;

			
			public boolean hasNext() {
				if(pos>=wrappedNativeNodeList.getLength()) return false;
				return true;
			}

			
			public NativeElement next() {
				return (NativeElement) wrappedNativeNodeList.item(pos++);
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void add(NativeElement arg0) {
				throw new UnsupportedOperationException();
			}

			
			public boolean hasPrevious() {
				if(pos<=0) return false;
				return true;
			}

			
			public int nextIndex() {
				if(pos>=wrappedNativeNodeList.getLength()) throw new IndexOutOfBoundsException();
				return pos++;
			}

			
			public NativeElement previous() {
				return (NativeElement) wrappedNativeNodeList.item(pos--);
			}

			
			public int previousIndex() {
				if(pos==0) throw new IndexOutOfBoundsException();
				return pos--;
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void set(NativeElement arg0) {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<NativeElement> listIterator(final int arg0) {
		return new ListIterator<NativeElement>(){
			
			private int pos = arg0;

			
			public boolean hasNext() {
				if(pos>=wrappedNativeNodeList.getLength()) return false;
				return true;
			}

			
			public NativeElement next() {
				return (NativeElement) wrappedNativeNodeList.item(pos++);
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void add(NativeElement arg0) {
				throw new UnsupportedOperationException();
			}

			
			public boolean hasPrevious() {
				if(pos<=0) return false;
				return true;
			}

			
			public int nextIndex() {
				if(pos>=wrappedNativeNodeList.getLength()) throw new IndexOutOfBoundsException();
				return pos++;
			}

			
			public NativeElement previous() {
				return (NativeElement) wrappedNativeNodeList.item(pos--);
			}

			
			public int previousIndex() {
				if(pos==0) throw new IndexOutOfBoundsException();
				return pos--;
			}

			
			public void set(NativeElement arg0) {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public NativeElement remove(int arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public NativeElement set(int arg0, NativeElement arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return wrappedNativeNodeList.getLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<NativeElement> subList(int arg0, int arg1) {
		ArrayList<NativeElement> al = new ArrayList<NativeElement>();
		for(int i=arg0;i<arg1;i++)
			al.add((NativeElement) wrappedNativeNodeList.item(i));
		return al;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] toArray() {
		NativeElement[] nodes = new NativeElement[wrappedNativeNodeList.getLength()];
		for(int i=0;i<wrappedNativeNodeList.getLength();i++){
			nodes[i] = (NativeElement) wrappedNativeNodeList.item(i);
		}
		return nodes;
	}

	/**
	 * Casting to arbitrary types is not supported.
	 * @throws UnsupportedOperationException
	 */
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	}
	
}
